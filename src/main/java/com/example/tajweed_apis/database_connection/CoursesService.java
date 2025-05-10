package com.example.tajweed_apis.database_connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursesService {
    public static List<Map<String, Object>> getCourses() throws SQLException {
        Connection connection = DBConnection.getInstance();
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("""
            SELECT ct.day,
                   JSON_ARRAYAGG(
                       JSON_OBJECT(
                           'course_id', c.id,
                           'level_id', c.level_id,
                           'day', ct.day,
                           'time', ct.time,
                           'teacher_name', t.name,
                           'students_count', COALESCE(student_counts.students_count, 0)
                       )
                   ) as courses_list
            FROM course c
                     JOIN teacher t ON c.teacher_id = t.id
                     LEFT JOIN (SELECT course_id,
                                       COUNT(*) AS students_count
                                FROM student_course
                                GROUP BY course_id) AS student_counts ON student_counts.course_id = c.id
                     LEFT JOIN course_time ct ON ct.course_id = c.id
            WHERE c.status = 'تعمل'
            group by ct.day;
        """);

        return getResultSetValues(resultSet);
    }
    public static List<Map<String, Object>> getTeachers() throws SQLException {
        Connection connection = DBConnection.getInstance();
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("""
            select 
                t.id, 
                t.name, 
                t.year_of_birth, 
                count(*) as count_of_courses
            from course c
                     join teacher t on c.teacher_id = t.id
            where c.status = "تعمل"
            group by c.teacher_id
            order by count_of_courses desc ;
        """);

        return getResultSetValues(resultSet);
    }
    public static Map<String, Object> getCourseDetails(String id) throws SQLException {
        Connection connection = DBConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("""
            select
            	distinct c.id,
            	t.name as teacher_name,
            	c.level_id,
            	JSON_ARRAYAGG(JSON_OBJECT('student_id', s.id ,'student_name', s.name, 'year_of_birth', s.year_of_birth)) over (partition by c.id order by c.id) as students,
            	JSON_ARRAYAGG(JSON_OBJECT('time', ct.`time` ,'day', ct.`day` , 'place', ct.place)) over (partition by c.id, s.id, ct.id) as times,
            	JSON_ARRAYAGG(JSON_OBJECT('note', cn.note  ,'date', cn.`date`)) over (partition by c.id, s.id, ct.id) as notes
            from
            	course c
            join student_course sc on
            	c.id = sc.course_id
            join student s on
            	sc.student_id = s.id
            join teacher t on
            	c.teacher_id = t.id
            left join course_time ct on
            	c.id  = ct.course_id
            left join course_note cn on
            	c.id = cn.course_id
            where c.id = ?;
        """);

        statement.setString(1, id);

        ResultSet resultSet = statement.executeQuery();

        return getResultSetValues(resultSet).get(0);
    }
    public static boolean addStudentAbsence(List<Integer> studentIds, String date) throws SQLException {
        Connection connection = DBConnection.getInstance();

        StringBuilder addingQuery = new StringBuilder();
        addingQuery.append("insert into student_absence(student_id, date) values ");

        for(Integer studentId : studentIds){
            addingQuery.append(String.format("(%d, '%s'), ", studentId, date));
        }

        addingQuery.delete(addingQuery.length() - 2, addingQuery.length());

        System.out.println("addingQuery" + addingQuery);

        Statement statement = connection.createStatement();

        return statement.execute(addingQuery.toString());
    }

    private static List<Map<String, Object>> getResultSetValues(ResultSet rs) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();


        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String column = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                map.put(column, value);
            }

            list.add(map);
        }

        return list;
    }
}
