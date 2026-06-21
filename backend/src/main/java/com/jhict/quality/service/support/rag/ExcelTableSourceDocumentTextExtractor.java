package com.jhict.quality.service.support.rag;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelTableSourceDocumentTextExtractor implements SourceDocumentTextExtractor {

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public boolean supports(String fileType) {
        return "xls".equals(fileType) || "xlsx".equals(fileType);
    }

    @Override
    public ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                             Path path, String fileType) throws Exception {
        ExtractedStandardDocument document = DocumentExtractionUtils.baseDocument(sourceFileName, sourceFilePath, fileType);
        try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                String sheetName = sheet.getSheetName();
                int firstRow = sheet.getFirstRowNum();
                int lastRow = sheet.getLastRowNum();
                for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }
                    String rowText = formatRow(row);
                    if (!StringUtils.hasText(rowText)) {
                        continue;
                    }
                    String segmentText = sheetName + " | Row" + (rowIndex + 1) + " | " + rowText;
                    document.getSegments().add(new DocumentTextSegment(null, segmentText));
                }
            }
        }
        return document;
    }

    private String formatRow(Row row) {
        List<String> cells = new ArrayList<>();
        short firstCell = row.getFirstCellNum();
        short lastCell = row.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return "";
        }
        for (int cellIndex = firstCell; cellIndex < lastCell; cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            String value = DocumentExtractionUtils.cleanText(dataFormatter.formatCellValue(cell));
            if (StringUtils.hasText(value)) {
                cells.add("Col" + (cellIndex + 1) + ":" + value);
            }
        }
        return String.join(" | ", cells);
    }
}
