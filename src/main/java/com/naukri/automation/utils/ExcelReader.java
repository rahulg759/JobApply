package com.naukri.automation.utils;

import com.naukri.automation.models.JobFilterCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    private static final Logger log = LogManager.getLogger(ExcelReader.class);

    private ExcelReader() {
    }

    public static List<JobFilterCriteria> readFilters() {
        List<JobFilterCriteria> filters = new ArrayList<>();

        try (InputStream is = ExcelReader.class.getClassLoader().getResourceAsStream("filters.xlsx")) {
            if (is == null) {
                log.warn("filters.xlsx not found on classpath, skipping Excel-based filtering");
                return filters;
            }

            try (Workbook workbook = new XSSFWorkbook(is)) {
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet.getPhysicalNumberOfRows() <= 1) {
                    log.info("filters.xlsx has only header row or is empty");
                    return filters;
                }

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String salary = getCellValue(row.getCell(0));
                    String location = getCellValue(row.getCell(1));
                    String experience = getCellValue(row.getCell(2));

                    if (salary.isEmpty() && location.isEmpty() && experience.isEmpty()) {
                        continue;
                    }

                    filters.add(new JobFilterCriteria(salary, location, experience));
                    log.debug("Read filter row {}: salary='{}', location='{}', experience='{}'",
                            i, salary, location, experience);
                }
            }

            log.info("Loaded {} filter criteria from filters.xlsx", filters.size());
        } catch (Exception e) {
            log.warn("Failed to read filters.xlsx: {}", e.getMessage());
        }

        return filters;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((int) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
