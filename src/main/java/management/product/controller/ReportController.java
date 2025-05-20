package management.product.controller;

import management.product.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.text.SimpleDateFormat;

@RestController
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping("/download-report")
    public ResponseEntity<?> downloadReport(
            @RequestParam(required = false) String fileName) {
        try {
            logger.info("Début de la génération du rapport...");


            byte[] pdfBytes = reportService.generateReport();


            String outputFilename = fileName != null ? fileName :
                    "rapport_produit_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", outputFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("Rapport généré avec succès, taille: {} octets", pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Erreur lors de la génération du rapport: " + e.getMessage());
        }
    }
}