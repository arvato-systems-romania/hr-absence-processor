package org.hrprocessor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hrprocessor.model.Employee;
import org.hrprocessor.model.Absence;
import org.hrprocessor.model.AbsenceResult;
import org.hrprocessor.service.ExcelReaderService;
import org.hrprocessor.service.AbsenceProcessorService;
import org.hrprocessor.service.ExcelWriterService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HRProcessorIntegrationTest {

    private ExcelReaderService excelReader;
    private AbsenceProcessorService processor;
    private ExcelWriterService excelWriter;

    @TempDir
    Path tempDir;

    private Path employeesTestFile;
    private Path absencesTestFile;
    private Path outputTestFile;

    @BeforeEach
    void setUp() throws IOException {
        excelReader = new ExcelReaderService();
        processor = new AbsenceProcessorService();
        excelWriter = new ExcelWriterService();

        employeesTestFile = tempDir.resolve("HR_RO_SMARTDISPO_WS.xlsx");
        absencesTestFile = tempDir.resolve("Lista absente HR Central - Test.xlsx");
        outputTestFile = tempDir.resolve("HR_RO_SMARTDISPO_ABSENCE.xlsx");


        copyResourceToFile("testdata/HR_RO_SMARTDISPO_WS.xlsx", employeesTestFile);
        copyResourceToFile("testdata/Lista absente HR Central - Test.xlsx", absencesTestFile);
    }

    @Test
    void testCompleteWorkflow() throws IOException {

        List<Employee> employees = excelReader.readEmployees(employeesTestFile.toString());

        assertFalse(employees.isEmpty(), "Should read at least some employees");
        assertTrue(employees.size() >= 4, "Should have at least 4 employees");

        Employee firstEmployee = findEmployeeByName(employees, "Vasile", "Test1");
        assertNotNull(firstEmployee, "Should find Vasile Test1");
        assertEquals("test001", firstEmployee.getUserId());
        assertEquals("vasile.test1@bertelsmann.de", firstEmployee.getEmail());

        List<Absence> absences = excelReader.readAbsences(absencesTestFile.toString());


        assertFalse(absences.isEmpty(), "Should read at least some absences");
        assertTrue(absences.size() >= 10, "Should have at least 10 approved absences");

        Absence firstAbsence = findAbsenceByName(absences, "Vasile", "Test1");
        assertNotNull(firstAbsence, "Should find absence for Vasile Test1");
        assertNotNull(firstAbsence.getStartDate());
        assertNotNull(firstAbsence.getEndDate());


        List<AbsenceResult> results = processor.processAbsences(absences, employees);


        assertFalse(results.isEmpty(), "Should have processed results");
        assertTrue(results.size() <= absences.size(), "Results should not exceed absences");
        assertTrue(results.size() >= 10, "Should have at least 10 matched results");


        AbsenceResult testResult = findResultByUserId(results, "test001");
        assertNotNull(testResult, "Should find result for test001");
        assertEquals("test001", testResult.getUserId());
        assertEquals("vasile.test1@bertelsmann.de", testResult.getEmail());
        assertNotNull(testResult.getAbsentFrom());
        assertNotNull(testResult.getAbsentUntil());


        excelWriter.writeAbsenceResults(results, outputTestFile.toString());

        assertTrue(Files.exists(outputTestFile), "Output file should be created");
        assertTrue(Files.size(outputTestFile) > 0, "Output file should not be empty");


        verifyOutputFileFormat(results.size());
    }

    @Test
    void testDataIntegrity() throws IOException {
        List<Employee> employees = excelReader.readEmployees(employeesTestFile.toString());
        List<Absence> absences = excelReader.readAbsences(absencesTestFile.toString());
        List<AbsenceResult> results = processor.processAbsences(absences, employees);

        long vasileAbsences = results.stream()
                .filter(r -> "test001".equals(r.getUserId()))
                .count();

        assertTrue(vasileAbsences > 0, "Vasile Test1 should have absences");

        for (AbsenceResult result : results) {
            assertNotNull(result.getAbsentFrom(), "Start date should not be null");
            assertNotNull(result.getAbsentUntil(), "End date should not be null");
            assertFalse(result.getAbsentFrom().isAfter(result.getAbsentUntil()),
                    "Start date should not be after end date");
        }


        assertEquals(results.size(), results.stream()
                .map(r -> r.getEmail() + "|" + r.getAbsentFrom() + "|" + r.getAbsentUntil())
                .distinct()
                .count(), "Should not have duplicate absence entries");
    }

    @Test
    void testFileFormats() throws IOException {
        List<Employee> employees = excelReader.readEmployees(employeesTestFile.toString());
        List<Absence> absences = excelReader.readAbsences(absencesTestFile.toString());


        assertNotNull(findEmployeeByName(employees, "Vasile", "Test1"));
        assertNotNull(findEmployeeByName(employees, "Bond", "Vagabond"));
        assertNotNull(findEmployeeByName(employees, "Mihai", "Eminescu"));
        assertNotNull(findEmployeeByName(employees, "Alibaba", "Husein"));

        for (Absence absence : absences) {
            assertTrue(absence.getStartDate().getYear() == 2025,
                    "All absences should be from 2025");
            assertTrue(absence.getStartDate().isAfter(LocalDate.of(2024, 12, 31)),
                    "Start date should be in 2025");
            assertTrue(absence.getEndDate().isBefore(LocalDate.of(2026, 1, 1)),
                    "End date should be in 2025");
        }
    }

    private Employee findEmployeeByName(List<Employee> employees, String firstName, String lastName) {
        return employees.stream()
                .filter(e -> firstName.equals(e.getFirstName()) && lastName.equals(e.getLastName()))
                .findFirst()
                .orElse(null);
    }

    private Absence findAbsenceByName(List<Absence> absences, String firstName, String lastName) {
        return absences.stream()
                .filter(a -> firstName.equals(a.getFirstName()) && lastName.equals(a.getLastName()))
                .findFirst()
                .orElse(null);
    }

    private AbsenceResult findResultByUserId(List<AbsenceResult> results, String userId) {
        return results.stream()
                .filter(r -> userId.equals(r.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private void copyResourceToFile(String resourcePath, Path targetPath) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void verifyOutputFileFormat(int expectedRows) throws IOException {
        List<AbsenceResult> readBackResults = readOutputFile();
        assertEquals(expectedRows, readBackResults.size(),
                "Read back should have same number of rows");

        for (AbsenceResult result : readBackResults) {
            assertNotNull(result.getUserId(), "USER-ID should not be null");
            assertNotNull(result.getEmail(), "Email should not be null");
            assertNotNull(result.getAbsentFrom(), "Absent from should not be null");
            assertNotNull(result.getAbsentUntil(), "Absent until should not be null");
        }
    }

    private List<AbsenceResult> readOutputFile() throws IOException {

        List<AbsenceResult> results = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(outputTestFile.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);


            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    AbsenceResult result = new AbsenceResult();
                    result.setUserId(getCellValueAsString(row.getCell(0)));
                    result.setEmail(getCellValueAsString(row.getCell(1)));

                    String fromDate = getCellValueAsString(row.getCell(2));
                    String untilDate = getCellValueAsString(row.getCell(3));

                    if (!fromDate.isEmpty()) {
                        result.setAbsentFrom(LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    }
                    if (!untilDate.isEmpty()) {
                        result.setAbsentUntil(LocalDate.parse(untilDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    }

                    results.add(result);
                }
            }
        }

        return results;
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }
}