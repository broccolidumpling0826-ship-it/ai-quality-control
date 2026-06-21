package com.jhict.quality.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhict.quality.common.exception.ServiceException;
import com.jhict.quality.entity.QcStandardDocument;
import com.jhict.quality.entity.QcStandardDocumentChunk;
import com.jhict.quality.mapper.QcStandardDocumentChunkMapper;
import com.jhict.quality.mapper.QcStandardDocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class DocumentIngestService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int CHUNK_SIZE = 500;

    private final Tika tika = new Tika();

    @Value("${app.upload.base-path:uploads}")
    private String uploadBasePath;

    @Resource
    private QcStandardDocumentMapper documentMapper;

    @Resource
    private QcStandardDocumentChunkMapper chunkMapper;

    @Transactional(rollbackFor = Exception.class)
    public QcStandardDocument ingest(String standardId, String filePath, String fileName) {
        File file = resolveFile(filePath);
        if (!file.exists()) {
            throw new ServiceException("文档文件不存在：" + filePath);
        }

        String resolvedName = StringUtils.hasText(fileName) ? fileName : file.getName();
        String fileType = detectFileType(resolvedName);
        String text;
        try {
            text = tika.parseToString(file);
        } catch (Exception e) {
            throw new ServiceException("文档解析失败：" + e.getMessage());
        }
        if (!StringUtils.hasText(text)) {
            throw new ServiceException("文档内容为空");
        }

        QcStandardDocument doc = new QcStandardDocument();
        doc.setStandardId(standardId);
        doc.setFileName(resolvedName);
        doc.setFilePath(filePath);
        doc.setFileType(fileType);
        doc.setIngestStatus("INDEXED");
        doc.setIngestTime(LocalDateTime.now().format(FORMATTER));
        documentMapper.insert(doc);

        chunkMapper.delete(new LambdaQueryWrapper<QcStandardDocumentChunk>()
                .eq(QcStandardDocumentChunk::getDocumentId, doc.getId()));

        List<String> segments = splitIntoChunks(text.trim());
        int index = 0;
        for (String segment : segments) {
            QcStandardDocumentChunk chunk = new QcStandardDocumentChunk();
            chunk.setDocumentId(doc.getId());
            chunk.setStandardId(standardId);
            chunk.setChunkIndex(index++);
            chunk.setSectionRef("§" + chunk.getChunkIndex());
            chunk.setChunkText(segment);
            chunk.setKeywordTags(extractKeywords(segment));
            chunkMapper.insert(chunk);
        }
        doc.setChunkCount(segments.size());
        documentMapper.updateById(doc);
        log.info("标准文档入库完成，documentId={}, chunks={}", doc.getId(), segments.size());
        return doc;
    }

    private File resolveFile(String filePath) {
        File direct = new File(filePath);
        if (direct.isAbsolute() && direct.exists()) {
            return direct;
        }
        return Paths.get(uploadBasePath, filePath).toFile();
    }

    private String detectFileType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return "PDF";
        }
        if (lower.endsWith(".md")) {
            return "MD";
        }
        return "TXT";
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\n+");
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            if (current.length() + trimmed.length() > CHUNK_SIZE && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            if (trimmed.length() > CHUNK_SIZE) {
                for (int i = 0; i < trimmed.length(); i += CHUNK_SIZE) {
                    chunks.add(trimmed.substring(i, Math.min(i + CHUNK_SIZE, trimmed.length())));
                }
            } else {
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(trimmed);
            }
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks.isEmpty() ? java.util.Collections.singletonList(text) : chunks;
    }

    private String extractKeywords(String text) {
        String[] keywords = {"抗拉强度", "延伸率", "屈服强度", "Q235B", "Rm", "ReL", "让步", "GB/T"};
        StringBuilder tags = new StringBuilder();
        for (String kw : keywords) {
            if (text.contains(kw)) {
                if (tags.length() > 0) {
                    tags.append(",");
                }
                tags.append(kw);
            }
        }
        return tags.toString();
    }
}
