package com.web.CertiQuest.controller;

import com.web.CertiQuest.model.Certificate;
import com.web.CertiQuest.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("/generate")
    public ResponseEntity<Certificate> generateCertificate(@RequestParam String userId,
                                                           @RequestParam String userName,
                                                           @RequestParam String quizTitle,
                                                           @RequestParam int score,
                                                           @RequestParam int totalQuestions,
                                                           @RequestParam String difficulty,
                                                           @RequestParam int quizId) {
        try {
            Certificate certificate = certificateService.generateCertificate(
                    userId, userName, quizTitle, score, totalQuestions, difficulty, quizId
            );
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable String fileName) throws IOException {
        String filePath = "certificates/" + fileName;
        File file = new File(filePath);
        System.out.println("Working directory: " + new File(".").getAbsolutePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<Certificate>> getAllCertificates() {
        List<Certificate> certificates = certificateService.getAllCertificates();
        return ResponseEntity.ok(certificates);
    }
}
