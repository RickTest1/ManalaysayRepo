package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static Connection testConnection;
    
    // Database configuration constants
    private static final String DATABASE_NAME = "aoopdatabase_payroll";
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Connection properties for better performance and compatibility
    private static final String CONNECTION_PROPERTIES =
            "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2";

    // Error message constants
    private static final String DRIVER_NOT_FOUND_ERROR =
            "❌ MySQL JDBC Driver not found!\n" +
                    "💡 Solution: Make sure mysql-connector-java.jar is in your classpath\n" +
                    "📝 Download from: https://dev.mysql.com/downloads/connector/j/\n" +
                    "🔧 Add the JAR file to your project's lib folder and include it in your build path";

    private static final String CONNECTION_FAILED_ERROR =
            "❌ Failed to connect to MySQL database!\n" +
                    "🔍 Common solutions:\n" +
                    "   1. ✅ Verify MySQL server is running on %s:%s\n" +
                    "   2. 🔑 Check username ('%s') and password ('%s') are correct\n" +
                    "   3. 🗄️  Ensure database '%s' exists\n" +
                    "   4. 🌐 Confirm MySQL is accepting connections on port %s\n" +
                    "   5. 🔧 Try running the SQL setup script first\n" +
                    "   6. ⚙️  Check MySQL Workbench connection settings";

    private static final String DATABASE_NOT_FOUND_ERROR =
            "❌ Database '%s' does not exist!\n" +
                    "📝 To fix this:\n" +
                    "   1. Open MySQL Workbench\n" +
                    "   2. Run the provided SQL setup script: aoopdatabase_payroll.sql\n" +
                    "   3. Or manually create the database: CREATE DATABASE %s;\n" +
                    "   4. Restart the application";

    private static final String ACCESS_DENIED_ERROR =
            "🚫 Access denied for user '%s'@'%s'!\n" +
                    "🔑 Authentication solutions:\n" +
                    "   1. Verify password is correct (current: '%s')\n" +
                    "   2. Try these common passwords:\n" +
                    "      - Empty password: \"\"\n" +
                    "      - Default: \"root\"\n" +
                    "      - Your custom password\n" +
                    "   3. Reset MySQL root password if needed\n" +
                    "   4. Check MySQL user permissions: GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';";

    private static final String NETWORK_ERROR =
            "🌐 Network connection error!\n" +
                    "📡 Network troubleshooting:\n" +
                    "   1. ✅ Verify MySQL server is running\n" +
                    "   2. 🔌 Check if port %s is open and not blocked by firewall\n" +
                    "   3. 🏠 Try connecting to 127.0.0.1 instead of localhost\n" +
                    "   4. 🔄 Restart MySQL service\n" +
                    "   5. 💻 Check if another application is using port %s";

    /**
     * Get database connection with enhanced error handling
     * @return Connection object
     * @throws SQLException if connection fails with detailed error information
     */
    public static void setTestConnection(Connection conn) {
        testConnection = conn;
    }
    
    public static Connection getConnection() throws SQLException {
        // Use injected connection for tests if available
        if (testConnection != null) {
            return testConnection;
        }
        
        try {
            // Load MySQL JDBC Driver with better error handling
            loadJDBCDriver();

            // Create connection properties
            Properties props = createConnectionProperties();

            // Attempt to establish connection
            Connection conn = DriverManager.getConnection(URL + CONNECTION_PROPERTIES, props);

            // Validate connection
            if (conn == null || !conn.isValid(5)) {
                throw new SQLException("Connection established but validation failed");
            }

            LOGGER.info("✅ Database connection established successfully to: " + URL);
            return conn;

        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found", e);
            throw new SQLException(DRIVER_NOT_FOUND_ERROR, e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            throw new SQLException(generateDetailedErrorMessage(e), e);
        }
    }
    
    
    /**
     * Load JDBC driver with specific error handling
     */
    private static void loadJDBCDriver() throws ClassNotFoundException {
        try {
            Class.forName(DRIVER);
            LOGGER.info("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("❌ MySQL JDBC Driver not found: " + DRIVER);
            throw e;
        }
    }

    /**
     * Create optimized connection properties
     */
    private static Properties createConnectionProperties() {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("autoReconnect", "true");
        props.setProperty("failOverReadOnly", "false");
        props.setProperty("maxReconnects", "3");
        props.setProperty("initialTimeout", "2");
        props.setProperty("connectTimeout", "10000");
        props.setProperty("socketTimeout", "30000");
        return props;
    }

    /**
     * Generate detailed error message based on SQLException type
     */
    private static String generateDetailedErrorMessage(SQLException e) {
        String errorCode = String.valueOf(e.getErrorCode());
        String sqlState = e.getSQLState();
        String message = e.getMessage().toLowerCase();

        // Access denied errors
        if (errorCode.equals("1045") || message.contains("access denied")) {
            return String.format(ACCESS_DENIED_ERROR, USER, HOST, PASSWORD);
        }

        // Database doesn't exist
        if (errorCode.equals("1049") || message.contains("unknown database")) {
            return String.format(DATABASE_NOT_FOUND_ERROR, DATABASE_NAME, DATABASE_NAME);
        }

        // Connection refused / network errors
        if (message.contains("connection refused") || message.contains("communications link failure")) {
            return String.format(NETWORK_ERROR, PORT, PORT);
        }

        // Timeout errors
        if (message.contains("timeout")) {
            return "⏱️ Connection timeout!\n" +
                    "🔧 Solutions:\n" +
                    "   1. Check if MySQL server is responding\n" +
                    "   2. Increase connection timeout settings\n" +
                    "   3. Verify network connectivity\n" +
                    "   4. Check server load and performance";
        }

        // Generic connection error with detailed info
        return String.format(CONNECTION_FAILED_ERROR, HOST, PORT, USER, PASSWORD, DATABASE_NAME, PORT) +
                "\n\n🔍 Technical Details:\n" +
                "   Error Code: " + errorCode + "\n" +
                "   SQL State: " + sqlState + "\n" +
                "   Message: " + e.getMessage();
    }

    /**
     * Test database connection with comprehensive diagnostics
     * @return ConnectionTestResult with detailed information
     */
    public static ConnectionTestResult testConnectionDetailed() {
        ConnectionTestResult result = new ConnectionTestResult();

        try {
            // Test 1: JDBC Driver
            result.addTest("JDBC Driver Loading", () -> {
                Class.forName(DRIVER);
                return "✅ MySQL JDBC Driver loaded successfully";
            });

            // Test 2: Basic Connection
            result.addTest("Database Connection", () -> {
                try (Connection conn = getConnection()) {
                    return "✅ Connection established successfully";
                }
            });

            // Test 3: Database Existence
            result.addTest("Database Verification", () -> {
                try (Connection conn = getConnection()) {
                    return "✅ Database '" + DATABASE_NAME + "' exists and is accessible";
                }
            });

            // Test 4: Table Structure
            result.addTest("Table Structure Check", () -> {
                try (Connection conn = getConnection()) {
                    return verifyTableStructure(conn);
                }
            });

            // Test 5: Sample Data
            result.addTest("Sample Data Verification", () -> {
                try (Connection conn = getConnection()) {
                    return verifySampleData(conn);
                }
            });

            result.setOverallSuccess(true);
            LOGGER.info("✅ Comprehensive database test completed successfully");

        } catch (Exception e) {
            result.setOverallSuccess(false);
            result.setOverallError("❌ Database test failed: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Database test failed", e);
        }

        return result;
    }

    /**
     * Simple connection test for backward compatibility
     */
    public static boolean testConnection() {
        return testConnectionDetailed().isOverallSuccess();
    }

    /**
     * Verify table structure exists
     */
    private static String verifyTableStructure(Connection conn) throws SQLException {
        String[] requiredTables = {"employees", "credentials", "attendance", "leave_request"};
        StringBuilder result = new StringBuilder();

        for (String table : requiredTables) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + table + "'")) {

                if (rs.next()) {
                    result.append("✅ Table '").append(table).append("' exists\n");
                } else {
                    result.append("❌ Table '").append(table).append("' missing\n");
                }
            }
        }

        return result.toString().trim();
    }

    /**
     * Verify sample data exists
     */
    private static String verifySampleData(Connection conn) throws SQLException {
        StringBuilder result = new StringBuilder();

        // Check employees count
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM employees")) {

            if (rs.next()) {
                int count = rs.getInt("count");
                result.append("✅ Employees table has ").append(count).append(" records\n");

                if (count == 0) {
                    result.append("⚠️  No employee data found - run the SQL setup script\n");
                }
            }
        }

        // Check credentials count
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM credentials")) {

            if (rs.next()) {
                int count = rs.getInt("count");
                result.append("✅ Credentials table has ").append(count).append(" records");
            }
        }

        return result.toString().trim();
    }

    /**
     * Verify database schema and data integrity
     */
    public static void verifyDatabase() {
        LOGGER.info("🔍 Starting database verification...");

        String[] queries = {
                "SELECT COUNT(*) as employee_count FROM employees",
                "SELECT COUNT(*) as attendance_count FROM attendance",
                "SELECT COUNT(*) as credentials_count FROM credentials",
                "SELECT COUNT(*) as leave_request_count FROM leave_request"
        };

        try (Connection conn = getConnection()) {
            for (String query : queries) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    if (rs.next()) {
                        String tableName = query.split("as ")[1].split(" ")[0];
                        int count = rs.getInt(1);
                        LOGGER.info(String.format("✅ %s: %d records", tableName, count));
                    }
                }
            }
            LOGGER.info("✅ Database verification completed successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Database verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Close a connection safely with better logging
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    LOGGER.fine("✅ Database connection closed successfully");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "⚠️ Error closing database connection", e);
            }
        }
    }

    /**
     * Get comprehensive database metadata information
     */
    public static void printDatabaseInfo() {
        try (Connection conn = getConnection()) {
            var metaData = conn.getMetaData();

            LOGGER.info("=== DATABASE INFORMATION ===");
            LOGGER.info("Database Product: " + metaData.getDatabaseProductName());
            LOGGER.info("Database Version: " + metaData.getDatabaseProductVersion());
            LOGGER.info("Driver Name: " + metaData.getDriverName());
            LOGGER.info("Driver Version: " + metaData.getDriverVersion());
            LOGGER.info("URL: " + metaData.getURL());
            LOGGER.info("Username: " + metaData.getUserName());
            LOGGER.info("Max Connections: " + metaData.getMaxConnections());
            LOGGER.info("Transaction Isolation: " + metaData.getDefaultTransactionIsolation());
            LOGGER.info("==============================");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to get database information", e);
        }
    }

    /**
     * Attempt to create database if it doesn't exist
     */
    public static boolean createDatabaseIfNotExists() {
        String serverUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/" + CONNECTION_PROPERTIES;

        try (Connection conn = DriverManager.getConnection(serverUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Check if database exists
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + DATABASE_NAME + "'");
            if (!rs.next()) {
                // Database doesn't exist, create it
                stmt.executeUpdate("CREATE DATABASE " + DATABASE_NAME);
                LOGGER.info("✅ Database '" + DATABASE_NAME + "' created successfully");
                return true;
            } else {
                LOGGER.info("ℹ️ Database '" + DATABASE_NAME + "' already exists");
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to create database", e);
            return false;
        }
    }

    /**
     * Get connection configuration summary
     */
    public static String getConnectionInfo() {
        return String.format(
                "📊 Connection Configuration:\n" +
                        "   🏠 Host: %s\n" +
                        "   🔌 Port: %s\n" +
                        "   🗄️ Database: %s\n" +
                        "   👤 User: %s\n" +
                        "   🔑 Password: %s\n" +
                        "   📡 URL: %s",
                HOST, PORT, DATABASE_NAME, USER,
                PASSWORD.replaceAll(".", "*"), // Mask password
                URL
        );
    }

    /**
     * Inner class to hold comprehensive test results
     */
    public static class ConnectionTestResult {
        private boolean overallSuccess = false;
        private String overallError = "";
        private java.util.List<TestResult> testResults = new java.util.ArrayList<>();

        public void addTest(String testName, TestRunner runner) {
            try {
                String result = runner.run();
                testResults.add(new TestResult(testName, true, result));
            } catch (Exception e) {
                testResults.add(new TestResult(testName, false, "❌ " + e.getMessage()));
            }
        }

        public boolean isOverallSuccess() { return overallSuccess; }
        public void setOverallSuccess(boolean success) { this.overallSuccess = success; }
        public String getOverallError() { return overallError; }
        public void setOverallError(String error) { this.overallError = error; }
        public java.util.List<TestResult> getTestResults() { return testResults; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("🧪 Database Connection Test Results:\n");
            sb.append("==================================\n");

            for (TestResult test : testResults) {
                sb.append(String.format("%-25s: %s\n", test.name, test.result));
            }

            sb.append("==================================\n");
            sb.append("Overall Status: ").append(overallSuccess ? "✅ SUCCESS" : "❌ FAILED");

            if (!overallSuccess && !overallError.isEmpty()) {
                sb.append("\nError: ").append(overallError);
            }

            return sb.toString();
        }
    }

    @FunctionalInterface
    private interface TestRunner {
        String run() throws Exception;
    }

    private static class TestResult {
        final String name;
        final boolean success;
        final String result;

        TestResult(String name, boolean success, String result) {
            this.name = name;
            this.success = success;
            this.result = result;
        }
    }
}