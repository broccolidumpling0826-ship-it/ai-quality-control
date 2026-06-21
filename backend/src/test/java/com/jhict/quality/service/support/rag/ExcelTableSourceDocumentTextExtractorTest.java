package com.jhict.quality.service.support.rag;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelTableSourceDocumentTextExtractorTest {

    private final ExcelTableSourceDocumentTextExtractor extractor = new ExcelTableSourceDocumentTextExtractor();

    @TempDir
    Path tempDir;

    @Test
    void extractShouldProduceRowOrientedSegments() throws Exception {
        Path xlsx = tempDir.resolve("mock-standard-table.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("指标");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("指标");
            header.createCell(1).setCellValue("下限");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("屈服强度 ReL");
            row.createCell(1).setCellValue("235 MPa");
            try (java.io.OutputStream out = Files.newOutputStream(xlsx)) {
                workbook.write(out);
            }
        }

        ExtractedStandardDocument document = extractor.extract(
                xlsx.getFileName().toString(),
                xlsx.toString(),
                xlsx,
                "xlsx");

        assertThat(document.getSegments()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(document.getSegments().get(1).getText()).contains("屈服强度 ReL");
        assertThat(document.getSegments().get(1).getText()).contains("235 MPa");
    }
}
