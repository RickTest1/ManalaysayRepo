package dao;

import model.Employee;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import util.DBConnection;

public class EmployeeDAOTest {

    private static Connection conn;
    private EmployeeDAO dao;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Initialize in-memory H2 database
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        try (Statement stmt = conn.createStatement()) {
            // Create tables
            stmt.execute("CREATE TABLE positions (position_id INT PRIMARY KEY AUTO_INCREMENT, position_title VARCHAR(255), basic_salary DOUBLE)");
            stmt.execute("CREATE TABLE employees (" +
                    "employee_id INT PRIMARY KEY," +
                    "last_name VARCHAR(255)," +
                    "first_name VARCHAR(255)," +
                    "birthday DATE," +
                    "address VARCHAR(255)," +
                    "phone_number VARCHAR(50)," +
                    "sss_number VARCHAR(50)," +
                    "philhealth_number VARCHAR(50)," +
                    "tin_number VARCHAR(50)," +
                    "pagibig_number VARCHAR(50)," +
                    "status VARCHAR(50)," +
                    "position_id INT," +
                    "supervisor_id INT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            stmt.execute("INSERT INTO positions (position_title, basic_salary) VALUES ('Developer', 50000)");
        }

        // Override DBConnection to return our H2 connection
        DBConnection.setTestConnection(conn);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        conn.close();
    }

    @Before
    public void setUp() {
        dao = new EmployeeDAO();
    }

    @After
    public void tearDown() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM employees");
        }
    }

    @Test
    public void testInsertAndGetEmployee() {
        Employee e = new Employee();
        e.setId(1);
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setStatus("Regular");
        e.setBirthday(LocalDate.of(1990, 1, 1));
        e.setPosition("Developer");

        boolean inserted = dao.insertEmployee(e);
        assertTrue(inserted);

        Employee fetched = dao.getEmployeeById(1);
        assertNotNull(fetched);
        assertEquals("John", fetched.getFirstName());
        assertEquals("Doe", fetched.getLastName());
    }

    @Test
    public void testUpdateEmployee() {
        // Insert first
        Employee e = new Employee();
        e.setId(2);
        e.setFirstName("Jane");
        e.setLastName("Smith");
        e.setStatus("Probationary");
        e.setPosition("Developer");
        dao.insertEmployee(e);

        // Update
        e.setLastName("Johnson");
        boolean updated = dao.updateEmployee(e);
        assertTrue(updated);

        Employee updatedEmployee = dao.getEmployeeById(2);
        assertEquals("Johnson", updatedEmployee.getLastName());
    }

    @Test
    public void testDeleteEmployee() {
        Employee e = new Employee();
        e.setId(3);
        e.setFirstName("Mark");
        e.setLastName("Lee");
        e.setStatus("Regular");
        e.setPosition("Developer");
        dao.insertEmployee(e);

        boolean deleted = dao.deleteEmployee(3);
        assertTrue(deleted);

        Employee deletedEmployee = dao.getEmployeeById(3);
        assertNull(deletedEmployee);
    }

    @Test
    public void testGetAllEmployees() {
        Employee e1 = new Employee();
        e1.setId(4);
        e1.setFirstName("Alice");
        e1.setLastName("Brown");
        e1.setStatus("Regular");
        e1.setPosition("Developer");

        Employee e2 = new Employee();
        e2.setId(5);
        e2.setFirstName("Bob");
        e2.setLastName("White");
        e2.setStatus("Probationary");
        e2.setPosition("Developer");

        dao.insertEmployee(e1);
        dao.insertEmployee(e2);

        List<Employee> employees = dao.getAllEmployees();
        assertNotNull(employees);
        assertEquals(2, employees.size());
    }

    @Test
    public void testEmployeeExists() {
        Employee e = new Employee();
        e.setId(6);
        e.setFirstName("Sam");
        e.setLastName("Green");
        e.setStatus("Regular");
        e.setPosition("Developer");
        dao.insertEmployee(e);

        assertTrue(dao.employeeExists(6));
        assertFalse(dao.employeeExists(999));
    }

    @Test
    public void testGetEmployeeCountByStatus() {
        Employee e1 = new Employee();
        e1.setId(7);
        e1.setFirstName("Ella");
        e1.setLastName("Gray");
        e1.setStatus("Regular");
        e1.setPosition("Developer");

        Employee e2 = new Employee();
        e2.setId(8);
        e2.setFirstName("Tom");
        e2.setLastName("Black");
        e2.setStatus("Regular");
        e2.setPosition("Developer");

        dao.insertEmployee(e1);
        dao.insertEmployee(e2);

        int count = dao.getEmployeeCountByStatus("Regular");
        assertEquals(2, count);
    }
}
