package com.zhongan.ifrs17agentjava.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // 1. PDF 解析并存入向量库
    public String processAndIngestPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // 简单切分（实际可用 TokenTextSplitter）
            List<Document> docs = List.of(new Document(text));
            vectorStore.add(docs); // 存入你的 Milvus 或 SimpleVectorStore

            return "成功解析 " + file.getOriginalFilename() + "，并存入知识库！";
        }
    }

    // 2. 生成 Excel 并落盘
    public String generateExcelReport(String orgName, double oldAmt, double newAmt) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("差异明细");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("科目");
            header.createCell(1).setCellValue("旧账套(万元)");
            header.createCell(2).setCellValue("新账套(万元)");
            header.createCell(3).setCellValue("差异(万元)");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(orgName + "准备金");
            dataRow.createCell(1).setCellValue(oldAmt);
            dataRow.createCell(2).setCellValue(newAmt);
            dataRow.createCell(3).setCellValue(newAmt - oldAmt);

            // 保存到本地的一个 downloads 文件夹（需提前创建）
            String fileName = "IFRS17_Report_" + System.currentTimeMillis() + ".xlsx";
            Path path = Paths.get("downloads", fileName);
            Files.createDirectories(path.getParent());

            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                workbook.write(out);
            }
            return fileName; // 返回文件名，供前端下载
        }
    }
}