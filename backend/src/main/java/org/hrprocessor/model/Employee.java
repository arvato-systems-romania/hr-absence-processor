package org.hrprocessor.model;

public class Employee {
    private String userId;
    private String lastName;
    private String firstName;
    private String email;
    private int weeklyWorkingHours;



    public Employee()
    {

    }


    public String getUserId() {
        return userId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setWeeklyWorkingHours(int weeklyWorkingHours) {
        this.weeklyWorkingHours = weeklyWorkingHours;
    }

    public String getEmail() {
        return email;
    }

    public int getWeeklyWorkingHours() {
        return weeklyWorkingHours;
    }

    public Employee(String userId, String lastName, String firstName, String email, int weeklyWorkingHours)
    {
        this.userId=userId;
        this.lastName=lastName;
        this.firstName=firstName;
        this.email=email;
        this.weeklyWorkingHours=weeklyWorkingHours;
    }

    @Override
    public String toString() {
        return "Employee{"+
                "userId='"+userId+'\''+
                "lasrName='"+lastName+'\''+
                "firstName='"+firstName+'\''+
                "emani='"+email+'\''+
                '}';
    }
}
