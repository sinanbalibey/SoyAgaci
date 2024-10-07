package com.FamilyTree.FamilyTree.controller;


import com.FamilyTree.FamilyTree.model.Node;
import com.FamilyTree.FamilyTree.service.PdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/pdf")
public class PdfController {

    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);
    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> extract(@RequestParam("file") MultipartFile file) {
        logger.info("PDF yükleme işlemi başladı.");
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            logger.info("PDF başarıyla yüklendi ve işleniyor.");
            return processPdfDocument(document);
        } catch (IOException e) {
            logger.error("PDF işlemi sırasında hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PDF işlemi sırasında hata oluştu.");
        } catch (Exception e) {
            logger.error("Beklenmedik bir hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Beklenmedik bir hata oluştu.");
        }
    }

    private ResponseEntity<?> processPdfDocument(PDDocument document) {
        try {
            logger.info("Veri çıkarma ve ağaç oluşturma işlemi başlıyor.");
            List<List<String>> extractedData = pdfService.extractAndFilterData(document);
            pdfService.buildTree(extractedData);
            Node root = pdfService.getTreeRoot();
            Map<String, Object> treeJson = pdfService.convertTreeToJson(root);
            logger.info("Veri çıkarma ve ağaç oluşturma işlemi başarıyla tamamlandı.");
            return ResponseEntity.ok(treeJson);
        } catch (Exception e) {
            logger.error("Ağaç oluşturma veya JSON dönüştürme sırasında hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ağaç oluşturma veya JSON dönüştürme sırasında hata oluştu.");
        }
    }

}
