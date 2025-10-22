package org.hrprocessor.controller;

import org.hrprocessor.model.AbsenceResult;
import org.hrprocessor.model.Employee;
import org.hrprocessor.model.Absence;
import org.hrprocessor.service.ExcelReaderService;
import org.hrprocessor.service.AbsenceProcessorService;
import org.hrprocessor.service.ExcelWriterService;
import org.hrprocessor.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/api/hr-processor")
@CrossOrigin(origins = "http://localhost:3000")
public class HRProcessorController {

    private static final Logger logger = LoggerFactory.getLogger(HRProcessorController.class);

    private final ExcelReaderService excelReader = new ExcelReaderService();
    private final AbsenceProcessorService processor = new AbsenceProcessorService();
    private final ExcelWriterService excelWriter = new ExcelWriterService();
    private final FileStorageService fileStorage = new FileStorageService();

    @PostMapping("/process")
    public ResponseEntity<byte[]> processFiles(@RequestParam("absencesFile") MultipartFile absencesFile, @RequestParam(value = "format", defaultValue = "excel") String format) {
        logger.info("Processing absence file: {} in format: {}", absencesFile.getOriginalFilename(), format);

        try {
            if (!fileStorage.persistentEmployeesFileExists()) {
                return ResponseEntity.badRequest().build();
            }
            Path tempDir = Files.createTempDirectory("hr-processor");
            Path absencesPath = tempDir.resolve("absences.xlsx");
            Path outputPath = tempDir.resolve("output" + (format.equals("csv") ? "csv" : "xlsx"));

            absencesFile.transferTo(absencesPath.toFile());

            List<Employee> employees = excelReader.readEmployees(fileStorage.getPersistentEmployeesFile().toString());
            List<Absence> absences = excelReader.readAbsences(absencesPath.toString());
            List<AbsenceResult> results = processor.processAbsences(absences, employees);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName;
            MediaType mediaType;
            byte[] fileData;


            if (format.equals("csv")) {
                fileName = "HR_RO_SMARTDISPO_ABSENCE_" + timestamp + ".csv";
                mediaType = MediaType.parseMediaType("text/csv");
                fileData = generateCSV(results).getBytes();
            } else {
                excelWriter.writeAbsenceResults(results, outputPath.toString());
                fileName = "HR_RO_SMARTDISPO_ABSENCE_" + timestamp + ".xlsx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                fileData = Files.readAllBytes(outputPath);
            }

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").contentType(mediaType).body(fileData);
        } catch (Exception e) {
            logger.error("Error procesing files", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateCSV(List<AbsenceResult> results) {
        StringBuilder csv = new StringBuilder();
        csv.append("USER-ID,email,absent from,absent until\n");

        for (AbsenceResult result : results) {
            csv.append(result.getUserId()).append(",").append(result.getEmail()).append(",").append(result.getFormattedAbsentFrom()).append(",").append(result.getFormattedAbsentUntil()).append("\n");
        }

        return csv.toString();
    }

    @GetMapping("/current-employees")
    public ResponseEntity<Resource> getCurrentEmployees() {
        try {
            if (!fileStorage.persistentEmployeesFileExists()) {
                return ResponseEntity.notFound().build();
            }

            File file = fileStorage.getPersistentEmployeesAsFile();
            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);

        } catch (Exception e) {
            logger.error("Error retrieving current employees file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-employees")
    public ResponseEntity<String> updateEmployees(@RequestParam("employeesFile") MultipartFile employeesFile) {
        try {
            fileStorage.updatePersistentEmployeesFile(employeesFile);
            return ResponseEntity.ok("Employees file updated successfully");
        } catch (Exception e) {
            logger.error("Error updating employees file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/employees-status")
    public ResponseEntity<Object> getEmployeesStatus() {
        boolean exists = fileStorage.persistentEmployeesFileExists();
        if (exists) {
            File file = fileStorage.getPersistentEmployeesAsFile();
            return ResponseEntity.ok(new Object() {
                public final boolean exists = true;
                public final String name = file.getName();
                public final long size = file.length();
                public final long lastModified = file.lastModified();
            });
        } else {
            return ResponseEntity.ok(new Object() {
                public final boolean exists = false;
            });
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("HR Processor API is running");
    }
}