package org.hrprocessor.service;

import org.hrprocessor.model.Absence;
import org.hrprocessor.model.AbsenceResult;
import org.hrprocessor.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbsenceProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(AbsenceProcessorService.class);

    public List<AbsenceResult> processAbsences(List<Absence> absences, List<Employee> employees)
    {
        logger.info("Processing {} absences with {} employees", absences.size(), employees.size());


        List<AbsenceResult> results=new ArrayList<>();
        Map<String,Employee> employeeMap=createEmployeeMap(employees);

        int matchedCount = 0;
        int unmatchedCount = 0;


        for(Absence absence:absences)
        {
            String nameKey=createNameKey(absence.getFirstName(),absence.getLastName());
            Employee employee=employeeMap.get(nameKey);

            if(employee!=null)
            {
                AbsenceResult result=new AbsenceResult();
                result.setUserId(employee.getUserId());
                result.setEmail(employee.getEmail());
                result.setAbsentFrom(absence.getStartDate());
                result.setAbsentUntil(absence.getEndDate());

                results.add(result);
                matchedCount++;

                logger.debug("Matched: {} {} -> {} ({})",
                        absence.getFirstName(), absence.getLastName(),
                        employee.getUserId(), employee.getEmail());
            }
            else {
                unmatchedCount++;
                logger.error("No match found for: {} {}", absence.getFirstName(), absence.getLastName());

            }
        }
        return results;
    }

    private Map<String, Employee> createEmployeeMap(List<Employee> employees) {
        logger.debug("Creating employee map from {} employees", employees.size());

        Map<String,Employee> map=new HashMap<>();

        for(Employee employee:employees)
        {
            String nameKey=createNameKey(employee.getFirstName(),employee.getLastName());
            map.put(nameKey,employee);
        }
        logger.debug("Employee map created with {} entries", map.size());

        return map;
    }

    private String createNameKey(String firstName, String lastName) {
        String key = (firstName + " " + lastName).toLowerCase().trim();
        logger.trace("Created name key: '{}' from '{}' '{}'", key, firstName, lastName);
        return key;
    }


}
