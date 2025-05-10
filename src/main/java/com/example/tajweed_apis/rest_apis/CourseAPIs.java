package com.example.tajweed_apis.rest_apis;

import com.example.tajweed_apis.database_connection.CoursesService;
import com.example.tajweed_apis.dtos.StudentAbsenceRequest;
import com.google.gson.Gson;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Replace with your Angular app's URL
public class CourseAPIs {

    @GetMapping("/dashboard")
    public ResponseEntity<String> getDashboardInfo() {
        try {
            List<Map<String, Object>> courses = CoursesService.getCourses();
            List<Map<String, Object>> teachers = CoursesService.getTeachers();
            Gson gson = new Gson();

            Map<String, List<Map<String, Object>>> response = new HashMap<>();
            response.put("courses", courses);
            response.put("teachers", teachers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(gson.toJson(response));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/courses/{id}")
    public ResponseEntity<String> getCourseDetails(@PathVariable String id) {
        try {
            Map<String, Object> courseDetails = CoursesService.getCourseDetails(id);
            Gson gson = new Gson();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(gson.toJson(courseDetails));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/student_absence")
    public ResponseEntity<Boolean> addStudentAbsence(@RequestBody StudentAbsenceRequest request){
        try {
            CoursesService.addStudentAbsence(request.getStudentIds(), request.getDate());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity
                    .ok()
                    .headers(headers).body(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
