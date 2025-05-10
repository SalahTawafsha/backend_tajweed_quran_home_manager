package com.example.tajweed_apis.database_connection;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DBConnection {

    private static final String url = "jdbc:mysql://localhost:3306/tajweed";

    private static final String username = "root";

    private static final String password = "root";

    private static Connection dbConnection;
    public static Connection getInstance() throws SQLException {
        if(dbConnection == null){
            dbConnection = DriverManager.getConnection(url, username, password);
        }

        return dbConnection;
    }
}

