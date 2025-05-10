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
            SELECT
                c.id,
                c.level_id,
                t.name as teacher_name,
                COALESCE(student_counts.students_count, 0) as students_count,
                COALESCE(course_times.times, '[]') as course_times
            FROM
                course c
                JOIN teacher t ON c.teacher_id = t.id
                LEFT JOIN (
                    SELECT
                        course_id,
                        COUNT(*) AS students_count
                    FROM student_course
                    GROUP BY course_id
                ) AS student_counts ON student_counts.course_id = c.id
                LEFT JOIN (
                    SELECT
                        course_id,
                        JSON_ARRAYAGG(JSON_OBJECT('day', day,'time', time)) AS times
                    FROM course_time
                    GROUP BY course_id
                ) AS course_times ON course_times.course_id = c.id
            WHERE
                c.status = 'تعمل'
            order by
            	JSON_UNQUOTE(course_times.times);
        """);

        return getResultSetValues(resultSet);
    }
    public static Map<String, Object> getCourse(String id) throws SQLException {
        Connection connection = DBConnection.getInstance();

        PreparedStatement statement = connection.prepareStatement("""
            select
            	distinct c.id,
            	t.name as teacher_name,
            	c.level_id,
            	JSON_ARRAYAGG(JSON_OBJECT('student_id', s.id ,'student_name', s.name, 'year_of_birth', s.year_of_birth)) over (partition by c.id) as students,
            	JSON_ARRAYAGG(JSON_OBJECT('time', ct.`time` ,'day', ct.`day` , 'place', ct.place)) over (partition by c.id, s.id, ct.id) as times,
            	JSON_ARRAYAGG(JSON_OBJECT('note', cn.note  ,'date', cn.`date`)) over (partition by c.id, s.id, ct.id) as notes
            from
            	course c
            join student_course sc on
            	c.id = sc.course_id
            join student s on
            	sc.student_id = s.id
            join teacher t on\s
            	c.teacher_id = t.id
            left join course_time ct on\s
            	c.id  = ct.course_id\s
            left join course_note cn on
            	c.id = cn.course_id\s
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
