/*
====================================================================================================
DATABASE SCRIPT: aoopdatabase_payroll
REVISED BY: Rick Cabugnason
DATE: 2025-07-14
DESCRIPTION:
This revised script addresses feedback regarding database normalization (3NF), and the use of
Views and Stored Procedures.

CHANGELOG:
1. NORMALIZATION (3NF):
   - Created a `positions` table to eliminate transitive dependencies from the `employees` table.
     Columns like basic_salary, allowances, and rates are now tied to a position, not the employee.
   - The `employees` table was refactored to include a `position_id` foreign key.
   - The `immediate_supervisor` (VARCHAR) was replaced with `supervisor_id` (INT), a self-referencing
     foreign key to `employees.employee_id` for proper hierarchical data modeling.
   - Removed redundant `first_name` and `last_name` from `leave_requests`.

2. VIEWS:
   - Added `v_employee_details` to provide a simplified, denormalized view of employee data
     for easy querying without complex joins in the application layer.

3. STORED PROCEDURES:
   - Added `sp_add_new_employee` to encapsulate the logic for adding an employee and their
     credentials in a single, safe transaction.
   - Added `sp_generate_payslip_data` to demonstrate how to centralize business logic (like
     payslip calculations) in the database, which simplifies reporting tasks in the application.
====================================================================================================
*/

-- =============================================
-- Database Creation
-- =============================================
DROP DATABASE IF EXISTS aoopdatabase_payroll;
CREATE DATABASE aoopdatabase_payroll;
USE aoopdatabase_payroll;

-- Set a modern, strict SQL mode
SET GLOBAL sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';


-- =============================================
-- Table: positions
-- Purpose: Achieves 3NF by storing position-related data separately.
-- This removes transitive dependencies from the employees table.
-- =============================================
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS credentials;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS positions;

CREATE TABLE positions (
    position_id INT AUTO_INCREMENT PRIMARY KEY,
    position_title VARCHAR(100) NOT NULL UNIQUE,
    basic_salary DECIMAL(10,2) NOT NULL,
    rice_subsidy DECIMAL(8,2) NOT NULL,
    phone_allowance DECIMAL(8,2) NOT NULL,
    clothing_allowance DECIMAL(8,2) NOT NULL,
    gross_semi_monthly_rate DECIMAL(10,2) NOT NULL,
    hourly_rate DECIMAL(8,2) NOT NULL
);

-- =============================================
-- Table: employees
-- Purpose: Stores core employee data. Now references the `positions` table
-- and uses a self-referencing key for the supervisor.
-- =============================================
CREATE TABLE employees (
    employee_id INT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    birthday DATE,
    address TEXT,
    phone_number VARCHAR(20),
    sss_number VARCHAR(20) UNIQUE,
    philhealth_number VARCHAR(20) UNIQUE,
    tin_number VARCHAR(20) UNIQUE,
    pagibig_number VARCHAR(20) UNIQUE,
    status ENUM('Regular', 'Probationary') NOT NULL,
    position_id INT NOT NULL,
    supervisor_id INT, -- This is now a self-referencing foreign key
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    FOREIGN KEY (supervisor_id) REFERENCES employees(employee_id) ON DELETE SET NULL
);

-- =============================================
-- Table: credentials
-- =============================================
CREATE TABLE credentials (
    employee_id INT PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL, -- Storing plain text passwords is a major security risk. Name changed to reflect hashing.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- =============================================
-- Table: leave_requests
-- Note: Redundant name columns have been removed.
-- =============================================
CREATE TABLE leave_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- =============================================
-- Table: attendance
-- =============================================
CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    log_in TIME,
    log_out TIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_employee_date (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);


-- =============================================
-- Data Insertion (Normalized)
-- =============================================

-- Step 1: Populate the `positions` table with unique roles from the original data.
INSERT INTO positions (position_title, basic_salary, rice_subsidy, phone_allowance, clothing_allowance, gross_semi_monthly_rate, hourly_rate) VALUES
('Chief Executive Officer', 90000.00, 1500.00, 2000.00, 1000.00, 45000.00, 535.71),
('Chief Operating Officer', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
('Chief Finance Officer', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
('Chief Marketing Officer', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
('IT Operations and Systems', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
('HR Manager', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
('HR Team Leader', 42975.00, 1500.00, 800.00, 800.00, 21487.50, 255.80),
('HR Rank and File', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
('Accounting Head', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
('Payroll Manager', 50825.00, 1500.00, 1000.00, 1000.00, 25412.50, 302.53),
('Payroll Team Leader', 38475.00, 1500.00, 800.00, 800.00, 19237.50, 229.02),
('Payroll Rank and File', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
('Account Manager', 53500.00, 1500.00, 1000.00, 1000.00, 26750.00, 318.45),
('Account Team Leader', 42975.00, 1500.00, 800.00, 800.00, 21487.50, 255.80),
('Account Team Leader (Variant)', 41850.00, 1500.00, 800.00, 800.00, 20925.00, 249.11),
('Account Rank and File', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
('Account Rank and File (Variant 1)', 23250.00, 1500.00, 500.00, 500.00, 11625.00, 138.39),
('Account Rank and File (Variant 2)', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
('Account Rank and File (Variant 3)', 24750.00, 1500.00, 500.00, 500.00, 12375.00, 147.32),
('Sales & Marketing', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
('Supply Chain and Logistics', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
('Customer Service and Relations', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51);

-- Step 2: Populate the `employees` table.
-- Note how supervisor_id is now used. This must be done in order, from top management down.
-- The CEO (10001) has a NULL supervisor_id.
INSERT INTO employees (employee_id, last_name, first_name, birthday, address, phone_number, sss_number, philhealth_number, tin_number, pagibig_number, status, position_id, supervisor_id) VALUES
(10001, 'Garcia', 'Manuel III', '1983-10-11', 'Valero Carpark Building Valero Street 1227, Makati City', '966-860-270', '44-4506057-3', '820126853951', '442-605-657-000', '691295330870', 'Regular', 1, NULL),
(10002, 'Lim', 'Antonio', '1988-06-19', 'San Antonio De Padua 2, Block 1 Lot 8 and 2, Dasmarinas, Cavite', '171-867-411', '52-2061274-9', '331735646338', '683-102-776-000', '663904995411', 'Regular', 2, 10001),
(10003, 'Aquino', 'Bianca Sofia', '1989-08-04', 'Rm. 402 4/F Jiao Building Timog Avenue Cor. Quezon Avenue 1100, Quezon City', '966-889-370', '30-8870406-2', '177451189665', '971-711-280-000', '171519773969', 'Regular', 3, 10001),
(10004, 'Reyes', 'Isabella', '1994-06-16', '460 Solanda Street Intramuros 1000, Manila', '786-868-477', '40-2511815-0', '341911411254', '876-809-437-000', '416946776041', 'Regular', 4, 10001),
(10005, 'Hernandez', 'Eduard', '1989-09-23', 'National Highway, Gingoog, Misamis Occidental', '088-861-012', '50-5577638-1', '957436191812', '031-702-374-000', '952347222457', 'Regular', 5, 10002),
(10006, 'Villanueva', 'Andrea Mae', '1988-02-14', '17/85 Stracke Via Suite 042, Poblacion, Las Piñas 4783 Dinagat Islands', '918-621-603', '49-1632020-8', '382189453145', '317-674-022-000', '441093369646', 'Regular', 6, 10002),
(10007, 'San Jose', 'Brad', '1996-03-15', '99 Strosin Hills, Poblacion, Bislig 5340 Tawi-Tawi', '797-009-261', '40-2400714-1', '239192926939', '672-474-690-000', '210850209964', 'Regular', 7, 10006),
(10008, 'Romualdez', 'Alice', '1992-05-14', '12A/33 Upton Isle Apt. 420, Roxas City 1814 Surigao del Norte', '983-606-799', '55-4476527-2', '545652640232', '888-572-294-000', '211385556888', 'Regular', 8, 10007),
(10009, 'Atienza', 'Rosie', '1948-09-24', '90A Dibbert Terrace Apt. 190, San Lorenzo 6056 Davao del Norte', '266-036-427', '41-0644692-3', '708988234853', '604-997-793-000', '260107732354', 'Regular', 8, 10007),
(10010, 'Alvaro', 'Roderick', '1988-03-30', '#284 T. Morato corner, Scout Rallos Street, Quezon City', '053-381-386', '64-7605054-4', '578114853194', '525-420-419-000', '799254095212', 'Regular', 9, 10003),
(10011, 'Salcedo', 'Anthony', '1993-09-14', '93/54 Shanahan Alley Apt. 183, Santo Tomas 1572 Masbate', '070-766-300', '26-9647608-3', '126445315651', '210-805-911-000', '218002473454', 'Regular', 10, 10010),
(10012, 'Lopez', 'Josie', '1987-01-14', '49 Springs Apt. 266, Poblacion, Taguig 3200 Occidental Mindoro', '478-355-427', '44-8563448-3', '431709011012', '218-489-737-000', '113071293354', 'Regular', 11, 10011),
(10013, 'Farala', 'Martha', '1942-01-11', '42/25 Sawayn Stream, Ubay 1208 Zamboanga del Norte', '329-034-366', '45-5656375-0', '233693897247', '210-835-851-000', '631130283546', 'Regular', 12, 10011),
(10014, 'Martinez', 'Leila', '1970-07-11', '37/46 Kulas Roads, Maragondon 0962 Quirino', '877-110-749', '27-2090996-4', '515741057496', '275-792-513-000', '101205445886', 'Regular', 12, 10011),
(10015, 'Romualdez', 'Fredrick', '1985-03-10', '22A/52 Lubowitz Meadows, Pililla 4895 Zambales', '023-079-009', '26-8768374-1', '308366860059', '598-065-761-000', '223057707853', 'Regular', 13, 10002),
(10016, 'Mata', 'Christian', '1987-10-21', '90 O\'Keefe Spur Apt. 379, Catigbian 2772 Sulu', '783-776-744', '49-2959312-6', '824187961962', '103-100-522-000', '631052853464', 'Regular', 14, 10015),
(10017, 'De Leon', 'Selena', '1975-02-20', '89A Armstrong Trace, Compostela 7874 Maguindanao', '975-432-139', '27-2090208-8', '587272469938', '482-259-498-000', '719007608464', 'Regular', 15, 10015),
(10018, 'San Jose', 'Allison', '1986-06-24', '08 Grant Drive Suite 406, Poblacion, Iloilo City 9186 La Union', '179-075-129', '45-3251383-0', '745148459521', '121-203-336-000', '114901859343', 'Regular', 16, 10016),
(10019, 'Rosario', 'Cydney', '1996-10-06', '93A/21 Berge Points, Tapaz 2180 Quezon', '868-819-912', '49-1629900-2', '579253435499', '122-244-511-000', '265104358643', 'Regular', 16, 10016),
(10020, 'Bautista', 'Mark', '1991-02-12', '65 Murphy Center Suite 094, Poblacion, Palayan 5636 Quirino', '683-725-348', '49-1647342-5', '399665157135', '273-970-941-000', '260054585575', 'Regular', 17, 10016),
(10021, 'Lazaro', 'Darlene', '1985-11-25', '47A/94 Larkin Plaza Apt. 179, Poblacion, Caloocan 2751 Quirino', '740-721-558', '45-5617168-2', '606386917510', '354-650-951-000', '104907708845', 'Probationary', 17, 10016),
(10022, 'Delos Santos', 'Kolby', '1980-02-26', '06A Gulgowski Extensions, Bongabon 6085 Zamboanga del Sur', '739-443-033', '52-0109570-6', '357451271274', '187-500-345-000', '113017988667', 'Probationary', 18, 10016),
(10023, 'Santos', 'Vella', '1983-12-31', '99A Padberg Spring, Poblacion, Mabalacat 3959 Lanao del Sur', '955-879-269', '52-9883524-3', '548670482885', '101-558-994-000', '360028104576', 'Probationary', 16, 10016),
(10024, 'Del Rosario', 'Tomas', '1978-12-18', '80A/48 Ledner Ridges, Poblacion, Kabankalan 8870 Marinduque', '882-550-989', '45-5866331-6', '953901539995', '560-735-732-000', '913108649964', 'Probationary', 16, 10016),
(10025, 'Tolentino', 'Jacklyn', '1984-05-19', '96/48 Watsica Flats Suite 734, Poblacion, Malolos 1844 Ifugao', '675-757-366', '47-1692793-0', '753800654114', '841-177-857-000', '210546661243', 'Probationary', 18, 10017),
(10026, 'Gutierrez', 'Percival', '1970-12-18', '58A Wilderman Walks, Poblacion, Digos 5822 Davao del Sur', '512-899-876', '40-9504657-8', '797639382265', '502-995-671-000', '210897095686', 'Probationary', 19, 10017),
(10027, 'Manalaysay', 'Garfield', '1986-08-28', '60 Goyette Valley Suite 219, Poblacion, Tabuk 3159 Lanao del Sur', '948-628-136', '45-3298166-4', '810909286264', '336-676-445-000', '211274476563', 'Probationary', 19, 10017),
(10028, 'Villegas', 'Lizeth', '1981-12-12', '66/77 Mann Views, Luisiana 1263 Dinagat Islands', '332-372-215', '40-2400719-4', '934389652994', '210-395-397-000', '122238077997', 'Probationary', 18, 10017),
(10029, 'Ramos', 'Carol', '1978-08-20', '72/70 Stamm Spurs, Bustos 4550 Iloilo', '250-700-389', '60-1152206-4', '351830469744', '395-032-717-000', '212141893454', 'Probationary', 16, 10017),
(10030, 'Maceda', 'Emelia', '1973-04-14', '50A/83 Bahringer Oval Suite 145, Kiamba 7688 Nueva Ecija', '973-358-041', '54-1331005-0', '465087894112', '215-973-013-000', '515012579765', 'Probationary', 16, 10017),
(10031, 'Aguilar', 'Delia', '1989-01-27', '95 Cremin Junction, Surallah 2809 Cotabato', '529-705-439', '52-1859253-1', '136451303068', '599-312-588-000', '110018813465', 'Probationary', 16, 10017),
(10032, 'Castro', 'John Rafael', '1992-02-09', 'Hi-way, Yati, Liloan Cebu', '332-424-955', '26-7145133-4', '601644902402', '404-768-309-000', '697764069311', 'Regular', 20, 10004),
(10033, 'Martinez', 'Carlos Ian', '1990-11-16', 'Bulala, Camalaniugan', '078-854-208', '11-5062972-7', '380685387212', '256-436-296-000', '993372963726', 'Regular', 21, 10004),
(10034, 'Santos', 'Beatriz', '1990-08-07', 'Agapita Building, Metro Manila', '526-639-511', '20-2987501-5', '918460050077', '911-529-713-000', '874042259378', 'Regular', 22, 10004);

-- Step 3: Populate credentials.
-- IMPORTANT: In a real application, you would insert a HASH of the password, not the plain text.
-- For example, using a library like BCrypt: '$2a$12$your_long_generated_hash_here'
INSERT INTO credentials (employee_id, password_hash) VALUES
(10001, 'password1234'), (10002, 'password1234'), (10003, 'password1234'), (10004, 'password1234'), (10005, 'password1234'),
(10006, 'password1234'), (10007, 'password1234'), (10008, 'password1234'), (10009, 'password1234'), (10010, 'password1234'),
(10011, 'password1234'), (10012, 'password1234'), (10013, 'password1234'), (10014, 'password1234'), (10015, 'password1234'),
(10016, 'password1234'), (10017, 'password1234'), (10018, 'password1234'), (10019, 'password1234'), (10020, 'password1234'),
(10021, 'password1234'), (10022, 'password1234'), (10023, 'password1234'), (10024, 'password1234'), (10025, 'password1234'),
(10026, 'password1234'), (10027, 'password1234'), (10028, 'password1234'), (10029, 'password1234'), (10030, 'password1234'),
(10031, 'password1234'), (10032, 'password1234'), (10033, 'password1234'), (10034, 'password1234');

-- Step 4: Populate attendance data
INSERT INTO attendance (employee_id, attendance_date, log_in, log_out) VALUES
(10001, '2024-06-03', '08:59:00', '18:31:00'), (10002, '2024-06-03', '10:35:00', '19:44:00'),
(10003, '2024-06-03', '10:23:00', '18:32:00'), (10004, '2024-06-03', '10:57:00', '18:14:00'),
(10001, '2024-06-04', '09:47:00', '19:07:00'), (10002, '2024-06-04', '10:11:00', '20:16:00'),
(10003, '2024-06-04', '10:45:00', '20:37:00'), (10004, '2024-06-04', '09:45:00', '16:54:00');


-- =============================================
-- View Creation
-- Purpose: To simplify application queries by providing a pre-joined, comprehensive view of employee data.
-- =============================================
CREATE OR REPLACE VIEW v_employee_details AS
SELECT
    e.employee_id,
    e.first_name,
    e.last_name,
    CONCAT(e.last_name, ', ', e.first_name) AS full_name,
    e.birthday,
    e.address,
    e.phone_number,
    e.status,
    p.position_title,
    p.basic_salary,
    p.rice_subsidy,
    p.phone_allowance,
    p.clothing_allowance,
    p.gross_semi_monthly_rate,
    p.hourly_rate,
    sup.employee_id as supervisor_id,
    CONCAT(sup.last_name, ', ', sup.first_name) AS supervisor_name,
    e.sss_number,
    e.philhealth_number,
    e.tin_number,
    e.pagibig_number
FROM
    employees e
JOIN
    positions p ON e.position_id = p.position_id
LEFT JOIN
    employees sup ON e.supervisor_id = sup.employee_id;


-- =============================================
-- Stored Procedure Creation
-- =============================================

-- Procedure 1: Add a new employee safely.
-- Purpose: Encapsulates the multi-table insert logic into a single, transactional call.
DELIMITER $$
CREATE PROCEDURE sp_add_new_employee (
    IN p_employee_id INT,
    IN p_last_name VARCHAR(50),
    IN p_first_name VARCHAR(50),
    IN p_birthday DATE,
    IN p_address TEXT,
    IN p_phone_number VARCHAR(20),
    IN p_sss_number VARCHAR(20),
    IN p_philhealth_number VARCHAR(20),
    IN p_tin_number VARCHAR(20),
    IN p_pagibig_number VARCHAR(20),
    IN p_status ENUM('Regular', 'Probationary'),
    IN p_position_id INT,
    IN p_supervisor_id INT,
    IN p_password_hash VARCHAR(255)
)
BEGIN
    -- Use a transaction to ensure both inserts succeed or fail together.
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL; -- Re-throw the exception to the caller
    END;

    START TRANSACTION;

    INSERT INTO employees (
        employee_id, last_name, first_name, birthday, address, phone_number,
        sss_number, philhealth_number, tin_number, pagibig_number,
        status, position_id, supervisor_id
    ) VALUES (
        p_employee_id, p_last_name, p_first_name, p_birthday, p_address, p_phone_number,
        p_sss_number, p_philhealth_number, p_tin_number, p_pagibig_number,
        p_status, p_position_id, p_supervisor_id
    );

    INSERT INTO credentials (employee_id, password_hash)
    VALUES (p_employee_id, p_password_hash);

    COMMIT;
END$$
DELIMITER ;


-- Procedure 2: Generate data for a payslip.
-- Purpose: Centralizes complex business logic for reporting. Your Java application can now
-- simply call this procedure instead of building a complex query with calculations.
DELIMITER $$
CREATE PROCEDURE sp_generate_payslip_data (
    IN p_employee_id INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    -- NOTE: This is a simplified example. A real-world scenario would be more complex,
    -- calculating overtime, late deductions, and integrating government contribution brackets.
    -- This demonstrates the concept of centralizing the logic.

    -- Placeholder values for government deductions
    DECLARE v_sss_deduction DECIMAL(10, 2) DEFAULT 581.30;
    DECLARE v_philhealth_deduction DECIMAL(10, 2) DEFAULT 400.00;
    DECLARE v_pagibig_deduction DECIMAL(10, 2) DEFAULT 100.00;
    DECLARE v_withholding_tax DECIMAL(10, 2);
    DECLARE v_total_deductions DECIMAL(10, 2);
    DECLARE v_gross_pay DECIMAL(10, 2);
    DECLARE v_net_pay DECIMAL(10, 2);

    SELECT gross_semi_monthly_rate INTO v_gross_pay
    FROM v_employee_details
    WHERE employee_id = p_employee_id;

    -- Simplified tax calculation
    SET v_withholding_tax = (v_gross_pay - v_sss_deduction - v_philhealth_deduction - v_pagibig_deduction) * 0.10; -- Example 10% tax
    IF v_withholding_tax < 0 THEN
        SET v_withholding_tax = 0;
    END IF;

    SET v_total_deductions = v_sss_deduction + v_philhealth_deduction + v_pagibig_deduction + v_withholding_tax;
    SET v_net_pay = v_gross_pay - v_total_deductions;

    -- Final select statement returns the payslip data
    SELECT
        emp.employee_id,
        emp.full_name,
        emp.position_title,
        p_start_date AS pay_period_start,
        p_end_date AS pay_period_end,
        emp.basic_salary,
        v_gross_pay AS gross_income,
        v_sss_deduction AS sss_contribution,
        v_philhealth_deduction AS philhealth_contribution,
        v_pagibig_deduction AS pagibig_contribution,
        v_withholding_tax AS withholding_tax,
        v_total_deductions AS total_deductions,
        v_net_pay AS net_income
    FROM
        v_employee_details emp
    WHERE
        emp.employee_id = p_employee_id;

END$$
DELIMITER ;


-- =============================================
-- Index Creation for Performance
-- =============================================
CREATE INDEX idx_employees_name ON employees(last_name, first_name);
CREATE INDEX idx_employees_position ON employees(position_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_leave_requests_employee_date ON leave_requests(employee_id, start_date, end_date);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, attendance_date);


-- =============================================
-- Verification and Usage Examples
-- =============================================

-- 1. Verify row counts
SELECT 'positions' as table_name, COUNT(*) as row_count FROM positions
UNION ALL
SELECT 'employees' as table_name, COUNT(*) as row_count FROM employees
UNION ALL
SELECT 'credentials' as table_name, COUNT(*) as row_count FROM credentials
UNION ALL
SELECT 'attendance' as table_name, COUNT(*) as row_count FROM attendance;

-- 2. Use the View to see detailed employee info easily
SELECT employee_id, full_name, position_title, supervisor_name, basic_salary FROM v_employee_details WHERE employee_id = 10007;

-- 3. Use the Stored Procedure to generate payslip data for an employee
CALL sp_generate_payslip_data(10008, '2024-06-01', '2024-06-15');