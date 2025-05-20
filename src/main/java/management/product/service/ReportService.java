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
        logger.info("Initialisation ReportService avec le chemin JRXML: {}", jrxmlPath);
    }

    public byte[] generateReport() throws JRException, SQLException {
        logger.debug("Appel de generateReport() sans paramètres");
        return generateReport(Collections.emptyMap());
    }

    public byte[] generateReport(Map<String, Object> customParameters) throws JRException, SQLException {
        logger.info("Début de la génération du rapport avec paramètres: {}", customParameters);


        JasperReport jasperReport = compileJrxmlReport();


        Map<String, Object> parameters = prepareParameters(customParameters);

        try (Connection connection = dataSource.getConnection()) {
            logger.debug("Connexion à la base de données établie");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            logger.info("Rapport rempli avec succès - Nombre de pages: {}", jasperPrint.getPages().size());

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            logger.info("Export PDF réussi - Taille: {} octets", pdfBytes.length);

            return pdfBytes;
        } catch (JRException e) {
            logger.error("Erreur JasperReports", e);
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur SQL", e);
            throw e;
        }
    }

    private JasperReport compileJrxmlReport() throws JRException {
        logger.debug("Chargement du fichier JRXML: {}", jrxmlPath);

        InputStream jrxmlStream = getClass().getResourceAsStream(jrxmlPath);
        if (jrxmlStream == null) {
            throw new JRException("Fichier JRXML introuvable à l'emplacement : " + jrxmlPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        logger.info("Compilation JRXML réussie");
        return jasperReport;
    }

    private Map<String, Object> prepareParameters(Map<String, Object> customParameters) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Rapport depuis PostgreSQL");

        if (customParameters != null) {
            parameters.putAll(customParameters);
        }
        return parameters;
    }
}
