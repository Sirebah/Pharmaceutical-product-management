package management.product.service;

import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final DataSource dataSource;
    private final String jrxmlPath;

    @Autowired
    public ReportService(DataSource dataSource,
                         @Value("${report.path:/reports/product.jrxml}") String jrxmlPath) {
        this.dataSource = dataSource;
        this.jrxmlPath = jrxmlPath;
        logger.info("Initializing ReportService with JRXML path: {}", jrxmlPath);
    }

    public byte[] generateReport() throws JRException, SQLException {
        logger.debug("Calling generateReport() without parameters");
        return generateReport(Collections.emptyMap());
    }

    public byte[] generateReport(Map<String, Object> customParameters) throws JRException, SQLException {
        logger.info("Starting report generation with parameters: {}", customParameters);

        JasperReport jasperReport = compileJrxmlReport();
        Map<String, Object> parameters = prepareParameters(customParameters);

        try (Connection connection = dataSource.getConnection()) {
            logger.debug("Database connection established");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            logger.info("Report filled successfully - Page count: {}", jasperPrint.getPages().size());

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            logger.info("PDF export successful - Size: {} bytes", pdfBytes.length);

            return pdfBytes;
        } catch (JRException e) {
            logger.error("JasperReports error", e);
            throw e;
        } catch (SQLException e) {
            logger.error("SQL error", e);
            throw e;
        }
    }

    private JasperReport compileJrxmlReport() throws JRException {
        logger.debug("Loading JRXML file: {}", jrxmlPath);

        InputStream jrxmlStream = getClass().getResourceAsStream(jrxmlPath);
        if (jrxmlStream == null) {
            throw new JRException("JRXML file not found at location: " + jrxmlPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        logger.info("JRXML compilation successful");
        return jasperReport;
    }

    private Map<String, Object> prepareParameters(Map<String, Object> customParameters) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Report from PostgreSQL");

        if (customParameters != null) {
            parameters.putAll(customParameters);
        }
        return parameters;
    }
}
