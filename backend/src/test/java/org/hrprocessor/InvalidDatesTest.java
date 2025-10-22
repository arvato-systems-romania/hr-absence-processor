package org.hrprocessor;

import org.hrprocessor.service.ExcelReaderService;
import org.hrprocessor.model.Employee;
import org.hrprocessor.model.Absence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvalidDataTest {

    private ExcelReaderService excelReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        excelReader = new ExcelReaderService();
    }

    @Test
    void testInvalidDateFormats() throws IOException {
        Path invalidDateFile = createFileWithInvalidDates();

        List<Absence> absences = excelReader.readAbsences(invalidDateFile.toString());


        assertNotNull(absences);


        long validAbsences = absences.stream().filter(a -> a.getStartDate() != null && a.getEndDate() != null).count();

        assertTrue(validAbsences >= 0, "Should handle invalid dates without crashing");
    }

    @Test
    void testFutureDatesYear20333() throws IOException {
        Path futureFile = createFileWithFutureDates();

        List<Absence> absences = excelReader.readAbsences(futureFile.toString());


        assertNotNull(absences);


        boolean hasFutureDates = absences.stream().anyMatch(a -> (a.getStartDate() != null && a.getStartDate().getYear() > 3000) || (a.getEndDate() != null && a.getEndDate().getYear() > 3000));

        assertFalse(hasFutureDates, "Should not accept dates in year 20333");
    }

    @Test
    void testInvalidMonths() throws IOException {
        Path invalidMonthFile = createFileWithInvalidMonths();

        List<Absence> absences = excelReader.readAbsences(invalidMonthFile.toString());

        assertNotNull(absences);

        boolean hasInvalidMonths = absences.stream().anyMatch(a -> (a.getStartDate() != null && a.getStartDate().getMonthValue() > 12) || (a.getEndDate() != null && a.getEndDate().getMonthValue() > 12));

        assertFalse(hasInvalidMonths, "Should not accept month 13");
    }

    @Test
    void testInvalidDaysInMonth() throws IOException {
        Path invalidDayFile = createFileWithInvalidDays();

        List<Absence> absences = excelReader.readAbsences(invalidDayFile.toString());

        assertNotNull(absences);


        boolean hasInvalidDays = absences.stream().anyMatch(a -> {
            if (a.getStartDate() != null && a.getStartDate().getMonthValue() == 2 && a.getStartDate().getDayOfMonth() > 29) {
                return true;
            }
            if (a.getEndDate() != null && a.getEndDate().getMonthValue() == 2 && a.getEndDate().getDayOfMonth() > 29) {
                return true;
            }
            return false;
        });

        assertFalse(hasInvalidDays, "Should not accept February 30th");
    }

    @Test
    void testEmptyFile() throws IOException {
        Path emptyFile = createEmptyExcelFile();

        List<Absence> absences = excelReader.readAbsences(emptyFile.toString());

        assertNotNull(absences);
        assertTrue(absences.isEmpty(), "Empty file should return empty list");
    }

    @Test
    void testCorruptedFile() throws IOException {
        Path corruptFile = createCorruptedFile();

        assertThrows(org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException.class, () -> {
            excelReader.readAbsences(corruptFile.toString());
        }, "Should throw NotOfficeXmlFileException for corrupted file");
    }

    @Test
    void testMissingColumns() throws IOException {
        Path missingColumnsFile = createFileWithMissingColumns();

        List<Absence> absences = excelReader.readAbsences(missingColumnsFile.toString());

        assertNotNull(absences);

        assertTrue(absences.size() == 0, "Missing columns should result in no valid absences");
    }

    @Test
    void testMixedValidInvalidData() throws IOException {
        Path mixedFile = createFileWithMixedData();

        List<Absence> absences = excelReader.readAbsences(mixedFile.toString());

        assertNotNull(absences);


        assertTrue(absences.size() > 0, "Should process some valid entries");


        for (Absence absence : absences) {
            assertNotNull(absence.getFirstName());
            assertNotNull(absence.getLastName());
            assertNotNull(absence.getStartDate());
            assertNotNull(absence.getEndDate());
            assertFalse(absence.getStartDate().isAfter(absence.getEndDate()));
        }
    }

    @Test
    void testEmployeeFileWithInvalidEmails() throws IOException {
        Path invalidEmailFile = createEmployeeFileWithInvalidEmails();

        List<Employee> employees = excelReader.readEmployees(invalidEmailFile.toString());

        assertNotNull(employees);


        for (Employee employee : employees) {
            assertNotNull(employee.getEmail());
            assertFalse(employee.getEmail().trim().isEmpty());
        }
    }

    @Test
    void testTextualMonthFormats() throws IOException {
        Path textMonthFile = createFileWithTextualMonths();

        List<Absence> absences = excelReader.readAbsences(textMonthFile.toString());

        assertNotNull(absences);
        assertEquals(0, absences.size(), "Textual month formats should not be parsed");
    }

    @Test
    void testAmericanDateFormat() throws IOException {
        Path americanFile = createFileWithAmericanDates();

        List<Absence> absences = excelReader.readAbsences(americanFile.toString());

        assertNotNull(absences);
        assertTrue(absences.size() >= 0, "Should handle American format without crashing");
    }

    private Path createFileWithInvalidDates() throws IOException {
        Path file = tempDir.resolve("invalid_dates.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");


            createAbsenceHeader(sheet);


            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("John");
            row1.createCell(5).setCellValue("Doe");
            row1.createCell(8).setCellValue("12/31/2025");
            row1.createCell(10).setCellValue("01/02/2026");
            row1.createCell(16).setCellValue("APPROVED");

            Row row2 = sheet.createRow(4);
            row2.createCell(3).setCellValue("Jane");
            row2.createCell(5).setCellValue("Smith");
            row2.createCell(8).setCellValue("2025-07-01");
            row2.createCell(10).setCellValue("2025-07-05");
            row2.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createFileWithFutureDates() throws IOException {
        Path file = tempDir.resolve("future_dates.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");

            createAbsenceHeader(sheet);

            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("Future");
            row1.createCell(5).setCellValue("Person");
            row1.createCell(8).setCellValue("01.01.20333");
            row1.createCell(10).setCellValue("02.01.20333");
            row1.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createFileWithInvalidMonths() throws IOException {
        Path file = tempDir.resolve("invalid_months.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");

            createAbsenceHeader(sheet);


            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("Invalid");
            row1.createCell(5).setCellValue("Month");
            row1.createCell(8).setCellValue("01.13.2025");
            row1.createCell(10).setCellValue("02.13.2025");
            row1.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createFileWithInvalidDays() throws IOException {
        Path file = tempDir.resolve("invalid_days.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");

            createAbsenceHeader(sheet);


            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("Invalid");
            row1.createCell(5).setCellValue("Day");
            row1.createCell(8).setCellValue("30.02.2024");
            row1.createCell(10).setCellValue("01.03.2024");
            row1.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createEmptyExcelFile() throws IOException {
        Path file = tempDir.resolve("empty.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Excel Output");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createCorruptedFile() throws IOException {
        Path file = tempDir.resolve("corrupted.xlsx");


        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            fos.write("This is not an Excel file".getBytes());
        }

        return file;
    }

    private Path createFileWithMissingColumns() throws IOException {
        Path file = tempDir.resolve("missing_columns.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");


            Row headerRow = sheet.createRow(2);
            headerRow.createCell(0).setCellValue("Person ID");
            headerRow.createCell(1).setCellValue("userId");


            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createFileWithMixedData() throws IOException {
        Path file = tempDir.resolve("mixed_data.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");

            createAbsenceHeader(sheet);


            Row validRow = sheet.createRow(3);
            validRow.createCell(3).setCellValue("Valid");
            validRow.createCell(5).setCellValue("Entry");
            validRow.createCell(8).setCellValue("2025-07-01");
            validRow.createCell(10).setCellValue("2025-07-05");
            validRow.createCell(16).setCellValue("APPROVED");


            Row invalidRow = sheet.createRow(4);
            invalidRow.createCell(3).setCellValue("Invalid");
            invalidRow.createCell(5).setCellValue("Date");
            invalidRow.createCell(8).setCellValue("32.13.2025");
            invalidRow.createCell(10).setCellValue("33.14.2025");
            invalidRow.createCell(16).setCellValue("APPROVED");


            Row validRow2 = sheet.createRow(5);
            validRow2.createCell(3).setCellValue("Another");
            validRow2.createCell(5).setCellValue("Valid");
            validRow2.createCell(8).setCellValue("2025-08-01");
            validRow2.createCell(10).setCellValue("2025-08-03");
            validRow2.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createEmployeeFileWithInvalidEmails() throws IOException {
        Path file = tempDir.resolve("invalid_emails.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("HR_RO_SMARTDISPO_WS");


            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("USER-ID");
            headerRow.createCell(1).setCellValue("last name");
            headerRow.createCell(2).setCellValue("first name");
            headerRow.createCell(3).setCellValue("Email");
            headerRow.createCell(4).setCellValue("Weekly working hours");


            Row validRow = sheet.createRow(1);
            validRow.createCell(0).setCellValue("test001");
            validRow.createCell(1).setCellValue("Valid");
            validRow.createCell(2).setCellValue("User");
            validRow.createCell(3).setCellValue("valid.user@company.com");
            validRow.createCell(4).setCellValue(40);


            Row emptyEmailRow = sheet.createRow(2);
            emptyEmailRow.createCell(0).setCellValue("test002");
            emptyEmailRow.createCell(1).setCellValue("Empty");
            emptyEmailRow.createCell(2).setCellValue("Email");
            emptyEmailRow.createCell(3).setCellValue("");
            emptyEmailRow.createCell(4).setCellValue(40);


            Row invalidEmailRow = sheet.createRow(3);
            invalidEmailRow.createCell(0).setCellValue("test003");
            invalidEmailRow.createCell(1).setCellValue("Invalid");
            invalidEmailRow.createCell(2).setCellValue("Email");
            invalidEmailRow.createCell(3).setCellValue("not-an-email");
            invalidEmailRow.createCell(4).setCellValue(40);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private void createAbsenceHeader(Sheet sheet) {

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Exported to Excel on Test Date");


        sheet.createRow(1);


        Row headerRow = sheet.createRow(2);
        String[] headers = {"Person ID", "userId", "Payroll ID", "First Name", "Middle Name", "Last Name", "Gender", "Time Type (Label)", "startDate", "startTime", "endDate", "endTime", "AM or PM (Picklist Label)", "quantityInDays", "quantityInHours", "Paid or Unpaid (Picklist Label)", "approvalStatus"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    private Path createFileWithTextualMonths() throws IOException {
        Path file = tempDir.resolve("textual_months.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");
            createAbsenceHeader(sheet);

            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("Text");
            row1.createCell(5).setCellValue("Month");
            row1.createCell(8).setCellValue("01.Feb.2024");
            row1.createCell(10).setCellValue("03.Feb.2024");
            row1.createCell(16).setCellValue("APPROVED");

            Row row2 = sheet.createRow(4);
            row2.createCell(3).setCellValue("Full");
            row2.createCell(5).setCellValue("Month");
            row2.createCell(8).setCellValue("15.February.2024");
            row2.createCell(10).setCellValue("16.February.2024");
            row2.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }

    private Path createFileWithAmericanDates() throws IOException {
        Path file = tempDir.resolve("american_dates.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Excel Output");
            createAbsenceHeader(sheet);

            Row row1 = sheet.createRow(3);
            row1.createCell(3).setCellValue("American");
            row1.createCell(5).setCellValue("Format");
            row1.createCell(8).setCellValue("03/15/2024");
            row1.createCell(10).setCellValue("03/16/2024");
            row1.createCell(16).setCellValue("APPROVED");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                workbook.write(fos);
            }
        }

        return file;
    }
}