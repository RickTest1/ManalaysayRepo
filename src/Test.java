package util;

import java.sql.*;

public class Test {
    public static void main(String[] args) {
        System.out.println("Testing connection with password 'admin'...");

        String url = "jdbc:mysql://localhost:3306/mysql"; // Connect to mysql database first
        String user = "root";
        String password = "admin";

        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL driver loaded successfully");

            // Test connection to mysql database (not our app database)
            Connection conn = DriverManager.getConnection(url + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", user, password);
            System.out.println("✅ Connected to MySQL server successfully!");

            // Check if our database exists
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'aoopdatabase_payroll'");

            if (rs.next()) {
                System.out.println("✅ Database 'aoopdatabase_payroll' exists");

                // Now test connection to our specific database
                conn.close();
                String appUrl = "jdbc:mysql://localhost:3306/aoopdatabase_payroll?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                Connection appConn = DriverManager.getConnection(appUrl, user, password);
                System.out.println("✅ Connected to aoopdatabase_payroll successfully!");

                // Test if credentials table exists
                Statement appStmt = appConn.createStatement();
                ResultSet credRs = appStmt.executeQuery("SELECT COUNT(*) FROM credentials");
                if (credRs.next()) {
                    System.out.println("✅ Credentials table exists with " + credRs.getInt(1) + " records");
                }

                appConn.close();
                System.out.println("\n🎉 Everything looks good! Your login should work now.");

            } else {
                System.out.println("❌ Database 'aoopdatabase_payroll' does NOT exist");
                System.out.println("📝 Creating database now...");

                // Create the database
                stmt.executeUpdate("CREATE DATABASE aoopdatabase_payroll");
                System.out.println("✅ Database created successfully!");
                System.out.println("⚠️  You still need to run the SQL script to create tables and insert data");
            }

            conn.close();

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL driver not found!");
            System.out.println("   Make sure mysql-connector-java.jar is in your classpath");
            System.out.println("   Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ SQL Error: " + e.getMessage());
            System.out.println("   Error Code: " + e.getErrorCode());
            System.out.println("   SQL State: " + e.getSQLState());

            if (e.getMessage().contains("Access denied")) {
                System.out.println("\n💡 Possible solutions:");
                System.out.println("   1. Check if password is really 'admin'");
                System.out.println("   2. Try empty password: PASSWORD = \"\"");
                System.out.println("   3. Try password 'root': PASSWORD = \"root\"");
                System.out.println("   4. Check MySQL Workbench connection settings");
            }
        }
    }
}