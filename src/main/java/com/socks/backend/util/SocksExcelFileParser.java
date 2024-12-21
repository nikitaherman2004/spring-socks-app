package com.socks.backend.util;

import com.socks.backend.dto.SocksDto;
import com.socks.backend.exception.ApplicationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@UtilityClass
public class SocksExcelFileParser {

    public static List<SocksDto> parse(MultipartFile file) {
        List<SocksDto> socksDtoList = new ArrayList<>();

        try(InputStream inputStream  = file.getInputStream()) {
            parseFile(inputStream, socksDtoList);

            return socksDtoList;
        } catch (IOException exception) {
            log.error(
                    "The method will throw an exception when trying to parse the file, message - {}",
                    exception.getMessage()
            );

            throw new ApplicationException(
                    "Не удалось разобрать Excel-файл. На сервере произошла ошибка",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private static void parseFile(InputStream inputStream, List<SocksDto> socksDtoList) throws IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);

            parseSheet(sheet, socksDtoList);
        }
    }

    private static void parseSheet(Sheet sheet, List<SocksDto> socksDtoList) {
        Iterator<Row> rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            int numberOfCells = row.getPhysicalNumberOfCells();

            if (numberOfCells < 3) {
                throw throwApplicationExceptionBadRequest();
            }

            SocksDto socksDto = parseRow(row);
            socksDtoList.add(socksDto);
        }
    }

    private static SocksDto parseRow(Row row) {
        SocksDto socksDto = new SocksDto();

        socksDto.setColor(getColorCell(row));
        socksDto.setCottonPercent(getCottonPercentCell(row));
        socksDto.setCount(getCountCell(row));

        return socksDto;
    }

    private static Integer getCountCell(Row row) {
        Cell countCell = row.getCell(2);

        if (!countCell.getCellType().equals(CellType.NUMERIC)) {
            throw throwApplicationExceptionBadRequest();
        }

        return (int) countCell.getNumericCellValue();
    }

    private static Double getCottonPercentCell(Row row) {
        Cell cottonPercentCell = row.getCell(1);

        if (!cottonPercentCell.getCellType().equals(CellType.NUMERIC)) {
            throw throwApplicationExceptionBadRequest();
        }

        return cottonPercentCell.getNumericCellValue();
    }

    private static String getColorCell(Row row) {
        Cell colorCell = row.getCell(0);

        if (!colorCell.getCellType().equals(CellType.STRING)) {
            throw throwApplicationExceptionBadRequest();
        }

        return colorCell.getStringCellValue();
    }

    private ApplicationException throwApplicationExceptionBadRequest() {
        return new ApplicationException(
                "Невозможно разобрать файл. Возможно, файл содержит некорректные данные",
                HttpStatus.BAD_REQUEST
        );
    }
}
