package org.hrprocessor;


import org.hrprocessor.model.Absence;
import org.hrprocessor.model.AbsenceResult;
import org.hrprocessor.model.Employee;
import org.hrprocessor.service.AbsenceProcessorService;
import org.hrprocessor.service.ExcelReaderService;
import org.hrprocessor.service.ExcelWriterService;

import java.time.LocalDate;
import java.util.List;

public class HRProcessorApp {

    public static void main(String[] args) {

        ExcelReaderService excelReader = new ExcelReaderService();
        AbsenceProcessorService processor = new AbsenceProcessorService();
        ExcelWriterService excelWriter = new ExcelWriterService();


        try {
            String employeesFile = "data/input/HR_RO_SMARTDISPO_WS.xlsx";
            String absencesFile = "data/input/Lista absente HR Central.xlsx";
            String outputFile = "data/output/HR_RO_SMARTDISPO_ABSENCE.xlsx";

            List<Employee> employees = excelReader.readEmployees(employeesFile);
            List<Absence> absences = excelReader.readAbsences(absencesFile);

            List<AbsenceResult> results = processor.processAbsences(absences, employees);

            excelWriter.writeAbsenceResults(results, outputFile);

        } catch (Exception e) {
            System.err.println("Error during processing: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
