package com.socks.backend.service;

import com.socks.backend.builder.SpecificationBuilder;
import com.socks.backend.dto.SocksDto;
import com.socks.backend.dto.SocksFilter;
import com.socks.backend.dto.SocksSortFiler;
import com.socks.backend.entity.Socks;
import com.socks.backend.exception.ApplicationException;
import com.socks.backend.mapper.SocksMapper;
import com.socks.backend.repository.SocksRepository;
import com.socks.backend.util.SocksExcelFileParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.by;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocksService {

    private final SocksMapper socksMapper = SocksMapper.INSTANCE;

    private final SpecificationBuilder builder = new SpecificationBuilder();

    private final SocksRepository socksRepository;

    public Page<SocksDto> getSocksByFilter(SocksSortFiler socksFilter, Pageable pageable) {
        log.info("Запрос на получения носков с заданными критериями - {}", socksFilter);

        Specification<Socks> specification = createSpecificationFromFilter(socksFilter);

        Sort sortBy = by(
                socksFilter.getOrderBy(),
                socksFilter.getSortBy().getValue()
        );

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortBy);

        return socksRepository.findAll(specification, pageRequest)
                .map(socksMapper::toDto);
    }

    public Integer getCountSocksByFilter(SocksFilter socksFilter) {
        log.info("Запрос на получения количества носков с задаными критериями- {}", socksFilter);

        Specification<Socks> specification = createSpecificationFromFilter(socksFilter);

        List<Socks> socksList = socksRepository.findAll(specification);

        return socksList.stream()
                .map((Socks::getCount))
                .reduce(Integer::sum)
                .orElse(0);
    }

    private Specification<Socks> createSpecificationFromFilter(SocksFilter socksFilter) {
        return builder
                .andEqual(socksFilter.getColor(), "color")
                .andEqual(socksFilter.getEqualCount(), "count")
                .andLessThen(socksFilter.getLessThenCount(), "count")
                .andGreaterThan(socksFilter.getMoreThenCount(), "count")
                .andEqual(socksFilter.getEqualCottonPercent(), "cottonPercent")
                .andBetween(
                        socksFilter.getMoreCottonPercent(),
                        socksFilter.getLessCottonPercent(), "cottonPercent"
                ).build();
    }

    public void saveSocks(Socks socks) {
        socksRepository.save(socks);
    }

    public Optional<Socks> findSocksByColorAndCottonPercent(String color, Double cottonPercent) {
        return socksRepository.findByColorAndCottonPercent(color, cottonPercent);
    }

    public Optional<Socks> findSocksById(Long id) {
        return socksRepository.findById(id);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS, rollbackOn = ApplicationException.class)
    public void increaseCountOrCreateSocks(SocksDto socksDto) {
        log.info(
                "Запрос на новую партию носков в количестве {} с {} процентным содержанием хлопка {} цвет",
                socksDto.getCount(), socksDto.getCottonPercent(), socksDto.getColor()
        );

        Optional<Socks> optionalSocks = findSocksByColorAndCottonPercent(
                socksDto.getColor(),
                socksDto.getCottonPercent()
        );

        if (optionalSocks.isPresent()) {
            Socks existingSocks = optionalSocks.get();

            Integer updatedCount = existingSocks.getCount() + socksDto.getCount();
            updateSocksCount(existingSocks, socksDto, updatedCount);
        } else {
            saveSocks(socksMapper.toEntity(socksDto));
        }
    }

    public void reduceSocksCount(SocksDto socksDto) {
        log.info(
                "Запрос на отпуск партии носков в количестве {} с {} процентным содержанием хлопка {} цвет",
                socksDto.getCount(), socksDto.getCottonPercent(), socksDto.getColor()
        );

        Optional<Socks> optionalSocks = findSocksByColorAndCottonPercent(
                socksDto.getColor(),
                socksDto.getCottonPercent()
        );

        if (optionalSocks.isEmpty()) {
            throw throwApplicationException(
                    "Носки с цветом %s и процентом хлопка %.2f не найдены",
                    HttpStatus.NOT_FOUND,
                    socksDto
            );
        }

        Socks existingSocks = optionalSocks.get();
        Integer updatedCount = existingSocks.getCount() - socksDto.getCount();

        updateSocksCount(optionalSocks.get(), socksDto, updatedCount);
    }

    public void updateSocksCount(Socks existingSocks, SocksDto socksDto, Integer updatedCount) {
        if (updatedCount < 0) {
            throw throwApplicationException(
                    "Невозможно уменьшить количество носков с цветом %s и процентом хлопка %.2f " +
                            "из-за нехватки носков на складе",
                    HttpStatus.BAD_REQUEST,
                    socksDto
            );
        }

        existingSocks.setCount(updatedCount);
        saveSocks(existingSocks);
    }

    @Transactional
    public void importSocksFromExcel(MultipartFile file) {
        List<SocksDto> socksDtoList = SocksExcelFileParser.parse(file);

        socksDtoList.forEach(this::increaseCountOrCreateSocks);
    }

    public void updateSocks(Long id, SocksDto socksDto) {
        log.info(
                "Обновление партии носков с идентификатором {} на новую партию " +
                        "в количесте {} с {} процентным содержанием хлопка {} цвет",
                id, socksDto.getCount(), socksDto.getCottonPercent(), socksDto.getColor()
        );

        Optional<Socks> optionalSocks = findSocksById(id);

        if (optionalSocks.isEmpty()) {
            throw throwApplicationException(
                    "Носки с цветом %s и процентом хлопка %.2f не найдены",
                    HttpStatus.NOT_FOUND,
                    socksDto
            );
        }
        Socks storedSocks = optionalSocks.get();

        storedSocks.setCount(socksDto.getCount());
        storedSocks.setColor(socksDto.getColor());
        storedSocks.setCottonPercent(socksDto.getCottonPercent());

        saveSocks(storedSocks);
    }

    private ApplicationException throwApplicationException(String message, HttpStatus status, SocksDto socksDto) {
        return new ApplicationException(
                message,
                status,
                socksDto.getColor(),
                socksDto.getCottonPercent()
        );
    }
}
