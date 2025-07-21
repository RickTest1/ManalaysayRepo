package dao;

import model.Position;
import org.junit.*;
import util.DBConnection;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PositionDAOTest {

    private static Connection connection;
    private PositionDAO dao;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        // Load H2 driver and create in-memory DB
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");

        // Tell DBConnection to use this test connection
        DBConnection.setTestConnection(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE positions (" +
                    "position_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "position_title VARCHAR(255) NOT NULL," +
                    "basic_salary DOUBLE," +
                    "rice_subsidy DOUBLE," +
                    "phone_allowance DOUBLE," +
                    "clothing_allowance DOUBLE," +
                    "gross_semi_monthly_rate DOUBLE," +
                    "hourly_rate DOUBLE" +
                    ")");
            stmt.execute("CREATE TABLE employees (" +
                    "employee_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "position_id INT" +
                    ")");
        }
    }

    @AfterClass
    public static void tearDownDatabase() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE employees");
            stmt.execute("DROP TABLE positions");
        }
        connection.close();
    }

    @Before
    public void setUp() {
        dao = new PositionDAO();
    }

    @After
    public void cleanUp() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM employees");
            stmt.execute("DELETE FROM positions");
        }
    }

    @Test
    public void testInsertAndFetchPosition() {
        Position p = createPosition("Software Engineer", 50000);
        int id = dao.insertPosition(p);
        assertTrue(id > 0);

        Position fetched = dao.getPositionById(id);
        assertNotNull(fetched);
        assertEquals("Software Engineer", fetched.getPositionName());
    }

    @Test
    public void testUpdatePosition() {
        Position p = createPosition("QA Engineer", 40000);
        int id = dao.insertPosition(p);

        p.setPositionId(id);
        p.setPositionName("QA Specialist");
        p.setMonthlySalary(45000);
        boolean updated = dao.updatePosition(p);
        assertTrue(updated);

        Position updatedPosition = dao.getPositionById(id);
        assertEquals("QA Specialist", updatedPosition.getPositionName());
        assertEquals(45000, updatedPosition.getMonthlySalary(), 0.01);
    }

    @Test
    public void testDeletePosition() {
        Position p = createPosition("Temp Position", 30000);
        int id = dao.insertPosition(p);
        assertTrue(dao.deletePosition(id));
        assertFalse(dao.positionExists(id));
    }

    @Test
    public void testGetPositionsBySalaryRange() {
        dao.insertPosition(createPosition("Junior Dev", 20000));
        dao.insertPosition(createPosition("Mid Dev", 40000));
        dao.insertPosition(createPosition("Senior Dev", 80000));

        List<Position> positions = dao.getPositionsBySalaryRange(30000, 90000);
        assertEquals(2, positions.size());
    }

    @Test
    public void testGetEmployeeCountByPosition() throws SQLException {
        Position p1 = createPosition("Designer", 35000);
        Position p2 = createPosition("Project Manager", 70000);
        int id1 = dao.insertPosition(p1);
        int id2 = dao.insertPosition(p2);

        // Add employees
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO employees (position_id) VALUES (?)")) {
            stmt.setInt(1, id1);
            stmt.executeUpdate();
            stmt.setInt(1, id1);
            stmt.executeUpdate();
            stmt.setInt(1, id2);
            stmt.executeUpdate();
        }

        Map<Integer, Integer> counts = dao.getEmployeeCountByPosition();
        assertEquals(2, (int) counts.get(id1));
        assertEquals(1, (int) counts.get(id2));
    }

    private Position createPosition(String title, double salary) {
        Position p = new Position();
        p.setPositionName(title);
        p.setMonthlySalary(salary);
        p.setRiceSubsidy(1500);
        p.setPhoneAllowance(1000);
        p.setClothingAllowance(500);
        p.setGrossSemiMonthlyRate(salary / 2);
        p.setHourlyRate(salary / 160);
        return p;
    }
}