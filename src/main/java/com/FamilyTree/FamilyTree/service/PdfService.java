package com.FamilyTree.FamilyTree.service;

import com.FamilyTree.FamilyTree.model.BinaryTree;
import com.FamilyTree.FamilyTree.model.Node;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.util.*;

@Service
public class PdfService {


    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);


    private Node root;

    public Node getTreeRoot() {
        return root;
    }

    public List<List<String>> extractAndFilterData(PDDocument document) {
        List<List<String>> filteredData = new ArrayList<>();
        try {
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            PageIterator pi = new ObjectExtractor(document).extract();

            while (pi.hasNext()) {
                Page page = pi.next();
                processPage(filteredData, sea, page);
            }

            List<List<String>> rootList = findRoot(filteredData);
            if (!rootList.isEmpty()) {
                List<String> root = rootList.get(0);
                int rootIndex = filteredData.indexOf(root);
                if (rootIndex >= 0) {
                    filteredData = filteredData.subList(0, rootIndex + 1);
                }
                Collections.reverse(filteredData);
            }
        } catch (Exception e) {
            logger.error("Veri çıkarma sırasında hata oluştu: {}", e.getMessage(), e);
        }
        return filteredData;
    }

    private void processPage(List<List<String>> filteredData, SpreadsheetExtractionAlgorithm sea, Page page) {
        for (Table table : sea.extract(page)) {
            for (var row : table.getRows()) {
                List<String> cells = new ArrayList<>();
                for (var cell : row) {
                    String cleanedText = cell.getText().replace("\r", " ").trim();
                    cells.add(cleanedText);
                }

                if (cells.size() > 1) {
                    String firstElement = cells.get(0);
                    String secondElement = cells.get(1);

                    if (firstElement.matches("\\d+") && (secondElement.equals("E") || secondElement.equals("K"))) {
                        filteredData.add(cells);
                    }
                }
            }
        }
    }

    public List<List<String>> findRoot(List<List<String>> data) {
        return data.stream()
                .filter(row -> row.size() > 2 && "Kendisi".equals(row.get(2)))
                .findFirst()
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }

    @Async
    public void buildTreeAsync(List<List<String>> data) {
        try {
            BinaryTree tree = new BinaryTree();
            tree.insert(data);
            root = tree.getRoot();
            logger.info("Ağaç oluşturma işlemi tamamlandı.");
        } catch (Exception e) {
            logger.error("Ağaç oluşturma sırasında hata oluştu: {}", e.getMessage(), e);
        }
    }

    public void buildTree(List<List<String>> data) {
        BinaryTree tree = new BinaryTree();
        tree.insert(data);
        root = tree.getRoot();
    }

    public Map<String, Object> convertTreeToJson(Node node) {
        Map<String, Object> result = new HashMap<>();
        if (node == null) return result;

        List<String> data = node.data.get(0);
        String name = data.size() > 4 ? data.get(3) + " " + data.get(4) : "Bilinmiyor";
        String gender = data.size() > 1 ? data.get(1) : "Bilinmiyor";
        String birthDate = data.size() > 7 ? "DT: " + data.get(7) : "DT: Bilinmiyor";
        String deathDate = data.size() > 11 ? "ÖT: " + data.get(11) : "ÖT: Bilinmiyor";

        result.put("name", name);
        result.put("gender", gender);
        result.put("birthDate", birthDate);
        result.put("deathDate", deathDate);
        result.put("relation", data.size() > 2 ? data.get(2) : "Bilinmiyor");

        List<Map<String, Object>> children = new ArrayList<>();
        if (node.left != null) {
            children.add(convertTreeToJson(node.left));
        }
        if (node.right != null) {
            children.add(convertTreeToJson(node.right));
        }

        if (!children.isEmpty()) {
            result.put("children", children);
        }

        return result;
    }

}
