package org.hrprocessor;

import org.hrprocessor.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(employee);
        assertNull(employee.getUserId());
        assertNull(employee.getFirstName());
        assertNull(employee.getLastName());
        assertNull(employee.getEmail());
        assertEquals(0, employee.getWeeklyWorkingHours());
    }

    @Test
    void testParameterizedConstructor() {
        Employee emp = new Employee("test001", "Popescu", "Ion", "ion.popescu@test.com", 40);

        assertEquals("test001", emp.getUserId());
        assertEquals("Popescu", emp.getLastName());
        assertEquals("Ion", emp.getFirstName());
        assertEquals("ion.popescu@test.com", emp.getEmail());
        assertEquals(40, emp.getWeeklyWorkingHours());
    }

    @Test
    void testSettersAndGetters() {
        employee.setUserId("test123");
        employee.setFirstName("Maria");
        employee.setLastName("Ionescu");
        employee.setEmail("maria.ionescu@test.com");
        employee.setWeeklyWorkingHours(35);

        assertEquals("test123", employee.getUserId());
        assertEquals("Maria", employee.getFirstName());
        assertEquals("Ionescu", employee.getLastName());
        assertEquals("maria.ionescu@test.com", employee.getEmail());
        assertEquals(35, employee.getWeeklyWorkingHours());
    }

    @Test
    void testToString() {
        employee.setUserId("test456");
        employee.setFirstName("Ana");
        employee.setLastName("Marin");
        employee.setEmail("ana.marin@test.com");

        String result = employee.toString();

        assertTrue(result.contains("test456"));
        assertTrue(result.contains("Ana"));
        assertTrue(result.contains("Marin"));
        assertTrue(result.contains("ana.marin@test.com"));
    }
}