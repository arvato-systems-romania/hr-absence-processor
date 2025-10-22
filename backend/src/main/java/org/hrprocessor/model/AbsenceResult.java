package org.hrprocessor.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AbsenceResult {

    private String userId;
    private  String email;
    private LocalDate absentFrom;
    private LocalDate absentUntil;


    public AbsenceResult()
    {

    }
    public AbsenceResult(String userId, String email, LocalDate absentFrom, LocalDate absentUntil) {
        this.userId = userId;
        this.email = email;
        this.absentFrom = absentFrom;
        this.absentUntil = absentUntil;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAbsentUntil(LocalDate absentUntil) {
        this.absentUntil = absentUntil;
    }

    public void setAbsentFrom(LocalDate absentFrom) {
        this.absentFrom = absentFrom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getAbsentFrom() {
        return absentFrom;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDate getAbsentUntil() {
        return absentUntil;
    }

    @Override
    public String toString() {
        return "AbsenceResult{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", absentFrom=" + absentFrom +
                ", absentUntil=" + absentUntil +
                '}';
    }


    public String getFormattedAbsentFrom()
    {
        return absentFrom !=null? absentFrom.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")):"";
    }
    public String getFormattedAbsentUntil()
    {
        return absentUntil !=null? absentUntil.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")):"";
    }
}
