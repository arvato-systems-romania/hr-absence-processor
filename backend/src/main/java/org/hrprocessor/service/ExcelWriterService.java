package org.hrprocessor.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hrprocessor.model.AbsenceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelWriterService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelWriterService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int LARGE_FILE_THRESHOLD = 10000;


    static {
        IOUtils.setByteArrayMaxOverride(700_000_000);
        logger.info("Apache POI byte array max override set to 700MB for writing");
    }
    public void writeAbsenceResults(List<AbsenceResult> results, String outputPath) throws IOException {
        logger.info("Writing {} absence results to: {}", results.size(), outputPath);

        if (results.size() > LARGE_FILE_THRESHOLD) {
            writeWithStreamingWorkbook(results, outputPath);
        } else {
            writeWithStandardWorkbook(results, outputPath);
        }
    }

    private void writeWithStandardWorkbook(List<AbsenceResult> results, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            createHeaderRow(sheet);

            for (int i = 0; i < results.size(); i++) {
                AbsenceResult result = results.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(result.getUserId());
                row.createCell(1).setCellValue(result.getEmail());

                String fromDate = result.getAbsentFrom() != null ?
                        result.getAbsentFrom().format(DATE_FORMATTER) : "";
                String untilDate = result.getAbsentUntil() != null ?
                        result.getAbsentUntil().format(DATE_FORMATTER) : "";

                row.createCell(2).setCellValue(fromDate);
                row.createCell(3).setCellValue(untilDate);
            }

            autoSizeColumn(sheet);

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    private void writeWithStreamingWorkbook(List<AbsenceResult> results, String outputPath) throws IOException {
        logger.info("Using streaming workbook for large file with {} results", results.size());

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(1000)) {
            Sheet sheet = workbook.createSheet("Sheet1");
            createHeaderRow(sheet);

            for (int i = 0; i < results.size(); i++) {
                AbsenceResult result = results.get(i);
                Row row = sheet.createRow(i + 1);

                row.createCell(0).setCellValue(result.getUserId());
                row.createCell(1).setCellValue(result.getEmail());

                String fromDate = result.getAbsentFrom() != null ?
                        result.getAbsentFrom().format(DATE_FORMATTER) : "";
                String untilDate = result.getAbsentUntil() != null ?
                        result.getAbsentUntil().format(DATE_FORMATTER) : "";

                row.createCell(2).setCellValue(fromDate);
                row.createCell(3).setCellValue(untilDate);


                if (i > 0 && i % 5000 == 0) {
                    logger.info("Written {} of {} rows", i, results.size());
                }
            }



            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            workbook.dispose();
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        headerRow.createCell(0).setCellValue("USER-ID");
        headerRow.createCell(1).setCellValue("email");
        headerRow.createCell(2).setCellValue("absent from");
        headerRow.createCell(3).setCellValue("absent until");

        CellStyle headerStyle=sheet.getWorkbook().createCellStyle();
        Font headerFont=sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
    }

    private void autoSizeColumn(Sheet sheet) {
        for(int i=0;i<4;i++)
        {
            sheet.autoSizeColumn(i);
        }
    }
}
