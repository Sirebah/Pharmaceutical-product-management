package management.product.controller;

import management.product.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

@RestController
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping("/download-report")
    public ResponseEntity<?> downloadReport(
            @RequestParam(required = false) String fileName) {
        try {
            logger.info("Starting report generation...");

            byte[] pdfBytes = reportService.generateReport();

            String outputFilename = fileName != null ? fileName :
                    "product_report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", outputFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("Report generated successfully, size: {} bytes", pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            logger.error("Error while generating report", e);

            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generating the report: " + e.getMessage());
        }
    }
    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody(required = false) Map<String, Object> params) {
        try {
            logger.info("POST request received to generate report with parameters: {}", params);

            byte[] pdfBytes = reportService.generateReport(params);

            String outputFilename = "product_report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", outputFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("Report generated successfully via POST");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            logger.error("Error while generating report via POST", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generating the report: " + e.getMessage());
        }
    }

}
