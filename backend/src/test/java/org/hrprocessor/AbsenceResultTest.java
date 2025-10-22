package org.hrprocessor;

import org.hrprocessor.model.AbsenceResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AbsenceResultTest {

    private AbsenceResult result;

    @BeforeEach
    void setUp() {
        result = new AbsenceResult();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(result);
        assertNull(result.getUserId());
        assertNull(result.getEmail());
        assertNull(result.getAbsentFrom());
        assertNull(result.getAbsentUntil());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDate from = LocalDate.of(2025, 7, 1);
        LocalDate until = LocalDate.of(2025, 7, 5);

        AbsenceResult res = new AbsenceResult("test001", "test@email.com", from, until);

        assertEquals("test001", res.getUserId());
        assertEquals("test@email.com", res.getEmail());
        assertEquals(from, res.getAbsentFrom());
        assertEquals(until, res.getAbsentUntil());
    }

    @Test
    void testFormattedDates() {
        result.setAbsentFrom(LocalDate.of(2025, 7, 3));
        result.setAbsentUntil(LocalDate.of(2025, 12, 25));

        assertEquals("03.07.2025", result.getFormattedAbsentFrom());
        assertEquals("25.12.2025", result.getFormattedAbsentUntil());
    }

    @Test
    void testFormattedDatesWithNull() {
        assertEquals("", result.getFormattedAbsentFrom());
        assertEquals("", result.getFormattedAbsentUntil());
    }

    @Test
    void testSettersAndGetters() {
        LocalDate from = LocalDate.of(2025, 8, 10);
        LocalDate until = LocalDate.of(2025, 8, 15);

        result.setUserId("test123");
        result.setEmail("user@test.com");
        result.setAbsentFrom(from);
        result.setAbsentUntil(until);

        assertEquals("test123", result.getUserId());
        assertEquals("user@test.com", result.getEmail());
        assertEquals(from, result.getAbsentFrom());
        assertEquals(until, result.getAbsentUntil());
    }
}