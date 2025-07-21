package service;

import dao.EmployeeDAO;
import model.Employee;
import service.PayrollCalculator.PayrollData;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JasperPayslipService {
    private static final Logger LOGGER = Logger.getLogger(JasperPayslipService.class.getName());
    private static final String PAYSLIP_TEMPLATE = "/motorph_payslip.jrxml";
    private static final String COMPANY_LOGO = "/images/motorph_logo.png";

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final PayrollCalculator payrollCalculator = new PayrollCalculator();
    private static JasperReport cachedCompiledReport;
    private final boolean testMode; // added

    public enum ExportFormat {
        PDF("pdf", "application/pdf"),
        EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        private final String extension, mimeType;
        ExportFormat(String ext, String type) { this.extension = ext; this.mimeType = type; }
        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
    }

    // Normal constructor (validates environment)
    public JasperPayslipService() throws JasperReportException {
        this(false);
    }

    // Test mode constructor
    public JasperPayslipService(boolean testMode) throws JasperReportException {
        this.testMode = testMode;
        if (!testMode) {
            validateEnvironment();
        }
    }

    public byte[] generatePayslipReport(int employeeId, java.time.LocalDate start,
                                        java.time.LocalDate end, ExportFormat format)
            throws JasperReportException {
        try {
            Employee emp = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (emp == null) throw new JasperReportException("Employee not found: " + employeeId);
            PayrollData pd = payrollCalculator.calculatePayroll(employeeId, start, end);
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(
                    Collections.singletonList(createPayslipData(emp, pd)));
            JasperPrint print = JasperFillManager.fillReport(getCompiledReport(), createReportParameters(emp), ds);
            return exportReport(print, format);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Payslip generation failed", e);
            throw new JasperReportException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    public File generatePayslipToFile(int employeeId, java.time.LocalDate start,
                                      java.time.LocalDate end, ExportFormat format, String outputDir)
            throws JasperReportException {
        byte[] data = generatePayslipReport(employeeId, start, end, format);
        Employee emp = employeeDAO.getEmployeeById(employeeId);
        String name = (emp != null ? emp.getLastName() : "Unknown").replaceAll("\\s+", "");
        String fileName = String.format("Payslip_%s_%d_%s.%s",
                name, employeeId, start.format(DateTimeFormatter.ofPattern("yyyy_MM")), format.getExtension());

        File dir = new File(outputDir);
        if (!dir.exists() && !dir.mkdirs())
            throw new JasperReportException("Failed to create output directory: " + outputDir);

        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(data); }
        catch (IOException e) { throw new JasperReportException("Failed to save payslip: " + e.getMessage(), e); }
        LOGGER.info("Payslip saved: " + file.getAbsolutePath());
        return file;
    }

    private void validateEnvironment() throws JasperReportException {
        try { Class.forName("net.sf.jasperreports.engine.JasperReport"); }
        catch (ClassNotFoundException e) { throw new JasperReportException("JasperReports missing in classpath."); }
        if (getClass().getResourceAsStream(PAYSLIP_TEMPLATE) == null)
            throw new JasperReportException("Template not found: " + PAYSLIP_TEMPLATE);
    }

    private JasperReport getCompiledReport() throws JRException, IOException {
        if (testMode) {
            // Return a simple dummy compiled report in test mode
            String dummy = "<?xml version=\"1.0\"?><jasperReport name=\"dummy\"></jasperReport>";
            try (InputStream in = new ByteArrayInputStream(dummy.getBytes())) {
                return JasperCompileManager.compileReport(in);
            }
        }

        if (cachedCompiledReport != null) return cachedCompiledReport;
        try (InputStream in = getClass().getResourceAsStream(PAYSLIP_TEMPLATE)) {
            if (in == null) throw new JRException("Missing template: " + PAYSLIP_TEMPLATE);
            cachedCompiledReport = JasperCompileManager.compileReport(in);
        }
        return cachedCompiledReport;
    }

    private PayslipData createPayslipData(Employee emp, PayrollData pd) {
        PayslipData d = new PayslipData();
        d.setEmployeeId(emp.getId());
        d.setEmployeeName(emp.getFullName());
        d.setPosition(emp.getPosition() != null ? emp.getPosition() : "N/A");
        d.setDepartment(getDepartmentFromPosition(emp.getPosition()));
        d.setPayslipNo(String.format("PS-%d-%s", emp.getId(),
                pd.getPeriodEnd().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        d.setPeriodStart(Date.valueOf(pd.getPeriodStart()));
        d.setPeriodEnd(Date.valueOf(pd.getPeriodEnd()));
        d.setMonthlyRate(BigDecimal.valueOf(pd.getMonthlyRate()));
        d.setDailyRate(BigDecimal.valueOf(pd.getDailyRate()));
        d.setDaysWorked(pd.getDaysWorked());
        d.setRiceSubsidy(BigDecimal.valueOf(pd.getRiceSubsidy()));
        d.setPhoneAllowance(BigDecimal.valueOf(pd.getPhoneAllowance()));
        d.setClothingAllowance(BigDecimal.valueOf(pd.getClothingAllowance()));
        d.setTotalBenefits(BigDecimal.valueOf(pd.getTotalAllowances()));
        d.setSss(BigDecimal.valueOf(pd.getSss()));
        d.setPhilhealth(BigDecimal.valueOf(pd.getPhilhealth()));
        d.setPagibig(BigDecimal.valueOf(pd.getPagibig()));
        d.setTax(BigDecimal.valueOf(pd.getTax()));
        d.setTotalDeductions(BigDecimal.valueOf(pd.getTotalDeductions()));
        d.setGrossPay(BigDecimal.valueOf(pd.getGrossPay()));
        d.setNetPay(BigDecimal.valueOf(pd.getNetPay()));
        return d;
    }

    private String getDepartmentFromPosition(String pos) {
        if (pos == null) return "General";
        pos = pos.toLowerCase();
        if (pos.contains("hr")) return "Human Resources";
        if (pos.contains("accounting") || pos.contains("payroll")) return "Accounting";
        if (pos.contains("marketing") || pos.contains("sales")) return "Marketing";
        if (pos.contains("it") || pos.contains("operations")) return "IT Operations";
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("executive")) return "Executive";
        return "General";
    }

    private Map<String, Object> createReportParameters(Employee emp) {
        Map<String, Object> params = new HashMap<>();
        if (!testMode) {
            try (InputStream logo = getClass().getResourceAsStream(COMPANY_LOGO)) {
                params.put("COMPANY_LOGO", logo);
            } catch (IOException e) {
                LOGGER.warning("Logo load failed: " + e.getMessage());
            }
        }
        params.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        params.put("GENERATED_BY", "MotorPH Payroll System");
        params.put("EMPLOYEE_NAME", emp.getFullName());
        params.put("EMPLOYEE_ID", emp.getId());
        params.put("GENERATION_DATE", new java.util.Date());
        return params;
    }

    private byte[] exportReport(JasperPrint print, ExportFormat format) throws JRException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (format == ExportFormat.PDF) {
                JRPdfExporter pdf = new JRPdfExporter();
                pdf.setExporterInput(new SimpleExporterInput(print));
                pdf.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                pdf.setConfiguration(new SimplePdfExporterConfiguration());
                pdf.exportReport();
            } else {
                JRXlsxExporter xlsx = new JRXlsxExporter();
                xlsx.setExporterInput(new SimpleExporterInput(print));
                xlsx.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                SimpleXlsxReportConfiguration cfg = new SimpleXlsxReportConfiguration();
                cfg.setOnePagePerSheet(true);
                cfg.setDetectCellType(true);
                xlsx.setConfiguration(cfg);
                xlsx.exportReport();
            }
            return out.toByteArray();
        } catch (IOException e) { throw new JRException("Export failed: " + e.getMessage(), e); }
    }

    public static class PayslipData {
        private Integer employeeId, daysWorked;
        private String employeeName, position, department, payslipNo;
        private Date periodStart, periodEnd;
        private BigDecimal monthlyRate, dailyRate, riceSubsidy, phoneAllowance,
                clothingAllowance, totalBenefits, sss, philhealth, pagibig, tax,
                totalDeductions, grossPay, netPay;

        public Integer getEmployeeId() { return employeeId; }
        public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getPayslipNo() { return payslipNo; }
        public void setPayslipNo(String payslipNo) { this.payslipNo = payslipNo; }

        public Date getPeriodStart() { return periodStart; }
        public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }

        public Date getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }

        public BigDecimal getMonthlyRate() { return monthlyRate; }
        public void setMonthlyRate(BigDecimal monthlyRate) { this.monthlyRate = monthlyRate; }

        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

        public BigDecimal getRiceSubsidy() { return riceSubsidy; }
        public void setRiceSubsidy(BigDecimal riceSubsidy) { this.riceSubsidy = riceSubsidy; }

        public BigDecimal getPhoneAllowance() { return phoneAllowance; }
        public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }

        public BigDecimal getClothingAllowance() { return clothingAllowance; }
        public void setClothingAllowance(BigDecimal clothingAllowance) { this.clothingAllowance = clothingAllowance; }

        public BigDecimal getTotalBenefits() { return totalBenefits; }
        public void setTotalBenefits(BigDecimal totalBenefits) { this.totalBenefits = totalBenefits; }

        public BigDecimal getSss() { return sss; }
        public void setSss(BigDecimal sss) { this.sss = sss; }

        public BigDecimal getPhilhealth() { return philhealth; }
        public void setPhilhealth(BigDecimal philhealth) { this.philhealth = philhealth; }

        public BigDecimal getPagibig() { return pagibig; }
        public void setPagibig(BigDecimal pagibig) { this.pagibig = pagibig; }

        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getGrossPay() { return grossPay; }
        public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }

        public BigDecimal getNetPay() { return netPay; }
        public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }

        public Integer getDaysWorked() { return daysWorked; }
        public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }
    }

    public static class JasperReportException extends Exception {
        public JasperReportException(String msg) { super(msg); }
        public JasperReportException(String msg, Throwable cause) { super(msg, cause); }
    }
}
