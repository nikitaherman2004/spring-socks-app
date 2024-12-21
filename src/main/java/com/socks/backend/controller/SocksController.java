package com.socks.backend.controller;

import com.socks.backend.dto.SocksDto;
import com.socks.backend.dto.SocksFilter;
import com.socks.backend.dto.SocksSortFiler;
import com.socks.backend.service.SocksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "socks")
@Tag(name = "Партии носков", description = "API для управления партиями носков на складе")
public class SocksController {

    private final SocksService socksService;

    // !!!!!!!!!
    // В ТЗ написано - Возвращает количество носков, соответствующих критериям. для GET /api/socks
    // Но при этом снизу написано к доп требованиям - Возможность сортировки результата по цвету или проценту хлопка.
    // Поэтому я сделал отдельный метод для получения общего количества носков по фильтру и получения списка носков
    // по фильтру с сортировкой как раз-таки и пагинацией
    @GetMapping
    @Operation(summary = "Получение всех носков по заданным критериям")
    public Page<SocksDto> getSocksByFiler(@ModelAttribute SocksSortFiler socksFilter, Pageable pageable) {
        return socksService.getSocksByFilter(socksFilter, pageable);
    }

    @GetMapping("/count")
    @Operation(summary = "Получение общего количества носков по заданным критериям")
    public Integer getCountSocksByFilter(@ModelAttribute SocksFilter socksFilter) {
        return socksService.getCountSocksByFilter(socksFilter);
    }

    @PostMapping("/income")
    @Operation(summary = "Регистрация новой партии носков")
    public void increaseCountOrCreateSocks(@RequestBody SocksDto socksDto) {
        socksService.increaseCountOrCreateSocks(socksDto);
    }

    @PostMapping("/outcome")
    @Operation(summary = "Отпуск существующих носков со склада")
    public void reduceSocksCount(@RequestBody SocksDto socksDto) {
        socksService.reduceSocksCount(socksDto);
    }

    @PostMapping("/batch")
    @Operation(summary = "Регистрация партий носков, взятые из Excel-файла (.xlsx)")
    public void importSocksFromExcel(@RequestBody MultipartFile file) {
        socksService.importSocksFromExcel(file);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление партии носков")
    public void updateSocks(@PathVariable Long id, @RequestBody SocksDto socksDto) {
        socksService.updateSocks(id, socksDto);
    }
}
