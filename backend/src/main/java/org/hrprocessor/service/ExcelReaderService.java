package org.hrprocessor.service;


import org.apache.poi.util.IOUtils;
import org.hrprocessor.model.Employee;
import org.hrprocessor.model.Absence;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelReaderService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReaderService.class);

    static {
        IOUtils.setByteArrayMaxOverride(700_000_000);
        logger.info("Apache POI byte array max override set to 700MB");
    }

    public List<Employee> readEmployees(String filePath) throws IOException {
        logger.info("Reading employees from: {}", filePath);
        List<Employee> employees = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            logger.debug("Sheet found: {} with {} rows", sheet.getSheetName(), sheet.getLastRowNum());


            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !isRowEmpty(row)) {
                    try {
                        Employee employee = new Employee();
                        employee.setUserId(getCellValueAsString(row.getCell(0)));
                        employee.setLastName(getCellValueAsString(row.getCell(1)));
                        employee.setFirstName(getCellValueAsString(row.getCell(2)));
                        employee.setEmail(getCellValueAsString(row.getCell(3)));
                        employee.setWeeklyWorkingHours(getCellValueAsInt(row.getCell(4)));


                        if (!employee.getUserId().isEmpty() && !employee.getEmail().isEmpty()) {
                            employees.add(employee);
                            logger.debug("Employee added: {} {}", employee.getFirstName(), employee.getLastName());
                        }
                    } catch (Exception e) {
                        logger.error("Error processing employee at row {}: {}", i, e.getMessage());

                    }

                }
            }
        }
        logger.info("Total employees read: {}", employees.size());
        return employees;
    }

    public List<Absence> readAbsences(String filePath) throws IOException {
        logger.info("Reading absences from: {}", filePath);
        List<Absence> absences = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            logger.debug("Sheet found: {} with {} rows", sheet.getSheetName(), sheet.getLastRowNum());

            int processedCount = 0;
            int errorCount = 0;
            int excludedCount = 0;

            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && !isRowEmpty(row)) {
                    try {
                        String approvalStatus = getCellValueAsString(row.getCell(16));

                        if (!"APPROVED".equalsIgnoreCase(approvalStatus.trim()) && !"PENDING".equalsIgnoreCase(approvalStatus.trim())) {
                            logger.debug("Skipping non-approved absence at row {}: status = {}", i, approvalStatus);
                            continue;
                        }


                        String timeType = getCellValueAsString(row.getCell(7)).toLowerCase().trim();
                        if ("working time".equals(timeType) || "break".equals(timeType)) {
                            excludedCount++;
                            logger.debug("Excluding {} entry at row {}: time type = {}", approvalStatus, i, timeType);
                            continue;
                        }

                        Absence absence = new Absence();
                        absence.setFirstName(getCellValueAsString(row.getCell(3)));
                        absence.setLastName(getCellValueAsString(row.getCell(5)));
                        absence.setStartDate(parseDate(row.getCell(8)));
                        absence.setEndDate(parseDate(row.getCell(10)));

                        if (absence.getFirstName().isEmpty() || absence.getLastName().isEmpty()) {
                            throw new RuntimeException("Missing employee name");
                        }

                        absences.add(absence);
                        processedCount++;
                        logger.debug("Absence added: {} {} ({} - {})", absence.getFirstName(), absence.getLastName(), absence.getStartDate(), absence.getEndDate());

                    } catch (RuntimeException e) {
                        errorCount++;
                        logger.error("Error processing absence at row {}: {}", i, e.getMessage());
                    }
                }
            }

            logger.info("Total absences read: {}, excluded (working time/break): {}, errors: {}",
                    processedCount, excludedCount, errorCount);
        }

        return absences;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC) {
            double numValue = cell.getNumericCellValue();
            return (int) Math.round((numValue));
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                double numValue = Double.parseDouble(cell.getStringCellValue());
                return (int) Math.round(numValue);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) {
            throw new RuntimeException("Date cell is missing - absence record incomplete");
        }

        try {
            LocalDate date = extractDateFromCell(cell);
            validateDate(date);
            return date;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid date format in cell: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Could not parse date from cell: " + e.getMessage());
        }
    }


    private LocalDate extractDateFromCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue().trim();

            if (dateStr.isEmpty()) {
                throw new IllegalArgumentException("Date string is empty");
            }
            if (dateStr.contains("T")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("T"));
            }

            if (dateStr.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                String[] parts = dateStr.split("\\.");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return LocalDate.of(year, month, day);
            }
            return LocalDate.parse(dateStr);
        }
        throw new IllegalArgumentException("Unsupported cell type for date");
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("Date cannot be null");
        }

        if (date.getYear() < 1900) {
            throw new RuntimeException("Date year " + date.getYear() + " is too far in the past (before 1900)");
        }

        if (date.getYear() > 2100) {
            throw new RuntimeException("Date year " + date.getYear() + " is too far in the future (after 2100)");
        }

        LocalDate now = LocalDate.now();

        if (date.isBefore(now.minusYears(5))) {
            throw new RuntimeException("Absence date " + date + " is more than 5 years old - likely data error");
        }

        if (date.isAfter(now.plusYears(2))) {
            throw new RuntimeException("Absence date " + date + " is more than 2 years in future - exceeds planning horizon");
        }

        if (date.isBefore(now.minusMonths(18))) {
            logger.warn("Absence date {} is more than 18 months old - verify accuracy", date);
        }

        if (date.getMonthValue() < 1 || date.getMonthValue() > 12) {
            throw new RuntimeException("Invalid month " + date.getMonthValue() + " in date " + date);
        }

        if (date.getDayOfMonth() < 1 || date.getDayOfMonth() > 31) {
            throw new RuntimeException("Invalid day " + date.getDayOfMonth() + " in date " + date);
        }

        if (date.getMonthValue() == 2 && date.getDayOfMonth() > 29) {
            throw new RuntimeException("February cannot have " + date.getDayOfMonth() + " days in date " + date);
        }

        if (date.getMonthValue() == 2 && date.getDayOfMonth() == 29 && !date.isLeapYear()) {
            throw new RuntimeException("February 29 is not valid in non-leap year " + date.getYear());
        }

        if ((date.getMonthValue() == 4 || date.getMonthValue() == 6 || date.getMonthValue() == 9 || date.getMonthValue() == 11) && date.getDayOfMonth() > 30) {
            throw new RuntimeException("Month " + date.getMonthValue() + " cannot have " + date.getDayOfMonth() + " days");
        }
    }
}
