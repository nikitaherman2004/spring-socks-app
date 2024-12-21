package com.socks.backend;

import com.socks.backend.dto.SocksDto;
import com.socks.backend.entity.Socks;
import com.socks.backend.exception.ApplicationException;
import com.socks.backend.mapper.SocksMapper;
import com.socks.backend.repository.SocksRepository;
import com.socks.backend.service.SocksService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SocksServiceTest {

    @Mock
    private SocksMapper socksMapper;

    @InjectMocks
    private SocksService socksService;

    @Mock
    private SocksRepository socksRepository;

    @Test
    void testUpdateSocksCount_ValidCount() {
        Socks existingSocks = new Socks(1L, "Green", 450, 21.5);
        SocksDto socksDto = new SocksDto(null, "White", 120, 11.5);

        Integer updatedCount = 100;

        socksService.updateSocksCount(existingSocks, socksDto, updatedCount);

        Integer expectedCount = 100;
        assertEquals(expectedCount, existingSocks.getCount());

        verify(socksRepository, times(1)).save(existingSocks);
    }

    @Test
    void testUpdateSocksCount_NegativeCount() {
        Socks existingSocks = new Socks(1L, "Green", 120, 21.5);
        SocksDto socksDto = new SocksDto(null, "Green", 450, 21.5);

        Integer updatedCount = existingSocks.getCount() - socksDto.getCount();

        ApplicationException applicationException = assertThrows(
                ApplicationException.class,
                () -> socksService.updateSocksCount(existingSocks, socksDto, updatedCount)
        );

        String expectedMessage = "Невозможно уменьшить количество носков с цветом Green и процентом хлопка 21,50 " +
                "из-за нехватки носков на складе";

        assertEquals(
                expectedMessage,
                applicationException.getMessage()
        );

        assertEquals(HttpStatus.BAD_REQUEST, applicationException.getStatus());

        verify(socksRepository, never()).save(any());
    }

    @Test
    public void testUpdateSocks_Success() {
        Long socksId = 1L;

        Socks existingSocks = new Socks(socksId, "Green", 5,70.0);
        SocksDto socksDto = new SocksDto(null, "Red", 10, 80.0);

        when(socksRepository.findById(socksId))
                .thenReturn(Optional.of(existingSocks));

        when(socksRepository.save(any(Socks.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        socksService.updateSocks(socksId, socksDto);

        verify(socksRepository, times(1)).findById(socksId);
        verify(socksRepository, times(1)).save(existingSocks);

        assertEquals(socksDto.getCount(), existingSocks.getCount());
        assertEquals(socksDto.getColor(), existingSocks.getColor());
        assertEquals(socksDto.getCottonPercent(), existingSocks.getCottonPercent());
    }

    @Test
    public void testUpdateSocks_NotFound() {
        Long socksId = 1L;

        SocksDto socksDto = new SocksDto(null, "Red", 20, 45.0);

        when(socksRepository.findById(socksId)).thenReturn(Optional.empty());

        ApplicationException applicationException = assertThrows(
                ApplicationException.class,
                () -> socksService.updateSocks(socksId, socksDto)
        );

        assertEquals(HttpStatus.NOT_FOUND, applicationException.getStatus());

        assertEquals(
                "Носки с цветом Red и процентом хлопка 45,00 не найдены",
                applicationException.getMessage()
        );

        verify(socksRepository, times(1)).findById(socksId);
        verify(socksRepository, never()).save(any(Socks.class));
    }

    @Test
    public void testReduceSocksCount_Success() {
        Socks existingSocks = new Socks(1L, "Red", 10, 80.0);
        SocksDto socksDto = new SocksDto(null, "Red", 5, 80.0);

        when(socksRepository.findByColorAndCottonPercent("Red", 80.0))
                .thenReturn(Optional.of(existingSocks));

        when(socksRepository.save(any(Socks.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        socksService.reduceSocksCount(socksDto);

        verify(socksRepository, times(1))
                .findByColorAndCottonPercent("Red", 80.0);

        verify(socksRepository, times(1))
                .save(existingSocks);

        Integer expectedCount = 5;
        assertEquals(expectedCount, existingSocks.getCount());
    }

    @Test
    public void testReduceSocksCount_NotFound() {
        SocksDto socksDto = new SocksDto(null, "Red", 5 , 80.0);

        when(socksRepository.findByColorAndCottonPercent("Red", 80.0))
                .thenReturn(Optional.empty());

        ApplicationException applicationException = assertThrows(
                ApplicationException.class,
                () -> socksService.reduceSocksCount(socksDto)
        );

        assertEquals(HttpStatus.NOT_FOUND, applicationException.getStatus());

        assertEquals(
                "Носки с цветом Red и процентом хлопка 80,00 не найдены",
                applicationException.getMessage()
        );

        verify(socksRepository, times(1))
                .findByColorAndCottonPercent("Red", 80.0);

        verify(socksRepository, never()).save(any(Socks.class));
    }

    @Test
    public void testReduceSocksCount_NegativeCount() {
        SocksDto socksDto = new SocksDto(null, "Red", 15, 80.0);
        Socks existingSocks = new Socks(1L, "Red", 10 , 80.0);

        when(socksRepository.findByColorAndCottonPercent("Red", 80.0))
                .thenReturn(Optional.of(existingSocks));

        ApplicationException applicationException = assertThrows(
                ApplicationException.class,
                () -> socksService.reduceSocksCount(socksDto)
        );

        assertEquals(HttpStatus.BAD_REQUEST, applicationException.getStatus());
        assertEquals(
                "Невозможно уменьшить количество носков с цветом Red и процентом " +
                        "хлопка 80,00 из-за нехватки носков на складе",
                applicationException.getMessage()
        );

        verify(socksRepository, times(1))
                .findByColorAndCottonPercent("Red", 80.0);

        verify(socksRepository, never()).save(any(Socks.class));
    }

    @Test
    public void testIncreaseCountOrCreateSocks_IncreaseExisting() {
        Socks existingSocks = new Socks(1L, "Red", 10, 80.0);
        SocksDto socksDto = new SocksDto(null, "Red", 5, 80.0);

        when(socksRepository.findByColorAndCottonPercent("Red", 80.0))
                .thenReturn(Optional.of(existingSocks));

        when(socksRepository.save(any(Socks.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        socksService.increaseCountOrCreateSocks(socksDto);

        verify(socksRepository, times(1))
                .findByColorAndCottonPercent("Red", 80.0);

        verify(socksRepository, times(1))
                .save(existingSocks);

        verify(socksMapper, never())
                .toEntity(any(SocksDto.class));
    }

    @Test
    public void testIncreaseCountOrCreateSocks_CreateNew() {
        Socks newSocks = new Socks(null, "Blue", 5, 90.0);
        SocksDto socksDto = new SocksDto(null, "Blue", 5, 90.0);

        when(socksRepository.findByColorAndCottonPercent("Blue", 90.0))
                .thenReturn(Optional.empty());

        when(socksRepository.save(newSocks))
                .thenReturn(newSocks);

        socksService.increaseCountOrCreateSocks(socksDto);

        verify(socksRepository, times(1))
                .findByColorAndCottonPercent("Blue", 90.0);

        verify(socksRepository, times(1))
                .save(newSocks);
    }
}