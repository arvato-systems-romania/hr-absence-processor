package org.hrprocessor;

import org.hrprocessor.model.Absence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AbsenceTest {

    private Absence absence;

    @BeforeEach
    void setUp() {
        absence = new Absence();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(absence);
        assertNull(absence.getFirstName());
        assertNull(absence.getLastName());
        assertNull(absence.getStartDate());
        assertNull(absence.getEndDate());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 5);

        Absence abs = new Absence("Ion", "Popescu", startDate, endDate);

        assertEquals("Ion", abs.getFirstName());
        assertEquals("Popescu", abs.getLastName());



        assertEquals(startDate, abs.getStartDate());
        assertEquals(endDate, abs.getEndDate());
    }

    @Test
    void testSettersAndGetters() {
        LocalDate startDate = LocalDate.of(2025, 8, 10);
        LocalDate endDate = LocalDate.of(2025, 8, 15);

        absence.setFirstName("Maria");
        absence.setLastName("Ionescu");
        absence.setStartDate(startDate);
        absence.setEndDate(endDate);

        assertEquals("Maria", absence.getFirstName());
        assertEquals("Ionescu", absence.getLastName());
        assertEquals(startDate, absence.getStartDate());
        assertEquals(endDate, absence.getEndDate());
    }

    @Test
    void testToString() {
        absence.setFirstName("Ana");
        absence.setLastName("Marin");
        absence.setStartDate(LocalDate.of(2025, 6, 1));
        absence.setEndDate(LocalDate.of(2025, 6, 3));

        String result = absence.toString();

        assertTrue(result.contains("Ana"));
        assertTrue(result.contains("Marin"));
        assertTrue(result.contains("2025-06-01"));
        assertTrue(result.contains("2025-06-03"));
    }
}