# MotorPH Payroll System

A comprehensive Java-based desktop application for managing employee payroll, attendance, and HR operations for MotorPH company.

## 🛠️ Technologies
- **Language:** Java (JDK 8+)
- **Database:** MySQL 8.0+
- **GUI:** Java Swing
- **Architecture:** Object-Oriented Programming with DAO pattern

## 🚀 Quick Setup

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- MySQL Server 8.0+
- MySQL Connector/J (JDBC Driver)

### Installation
1. **Setup Database**
   ```bash
   # Import the database schema
   mysql -u root -p < src/util/aoopdatabase_payroll.sql
   ```

2. **Configure Connection**
   - Host: `localhost:3306`
   - Database: `aoopdatabase_payroll` 
   - Username: `root`
   - Password: `admin`

3. **Run Application**
   ```bash
   javac -cp ".:mysql-connector-java.jar" src/**/*.java
   java -cp ".:mysql-connector-java.jar:src" gui.MotorPHPayrollApp
   ```

## 🔐 Default Login
- **Employee IDs:** 10001 to 10034
- **Password:** `password1234`

---

**Academic Project** | MO-IT113 Advanced Object-Oriented Programming  
**Institution:** Mapúa Malayan Digital College (2024-2025)  
**Team:** Group 6, Section A2101
