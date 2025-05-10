package com.example.tajweed_apis.dtos;
import java.util.List;

public class StudentAbsenceRequest {
    private String date;
    private List<Integer> studentIds;

    public List<Integer> getStudentIds() {
        return studentIds;
    }

    public String getDate() {
        return date;
    }
}
