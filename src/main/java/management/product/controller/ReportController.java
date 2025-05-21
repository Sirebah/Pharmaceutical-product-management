package management.product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import management.product.domain.Produit;
import management.product.service.ProductService;
import management.product.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ProductService productService;

    @GetMapping("/download-report")
    public ResponseEntity<?> downloadReport(@RequestParam(required = false) String fileName) {
        try {
            log.info("Starting report generation...");

            byte[] pdfBytes = reportService.generateReport();
            String outputFilename = fileName != null ? fileName :
                    "product_report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", outputFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Report generated successfully, size: {} bytes", pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            log.error("Error while generating report", e);
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN)
                    .body("Error generating the report: " + e.getMessage());
        }
    }

    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody(required = false) Map<String, Object> params) {
        try {
            log.info("POST request received to generate report with parameters: {}", params);

            byte[] pdfBytes = reportService.generateReport(params);
            String outputFilename = "product_report_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", outputFilename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("Report generated successfully via POST");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            log.error("Error while generating report via POST", e);
            return ResponseEntity.internalServerError().contentType(MediaType.TEXT_PLAIN)
                    .body("Error generating the report: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Produit> createProduct(@RequestBody Produit produit) {
        Produit saved = productService.create(produit);
        return ResponseEntity.ok().body(saved);
    }
}
