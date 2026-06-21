package com.jhict.quality.service.support.rag;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StandardClauseChunker {

    private static final int DEFAULT_MAX_CHUNK_CHARS = 1200;
    private static final Pattern CLAUSE_HEADING = Pattern.compile(
            "^\\s*((?:\\d+\\.){1,5}\\d+|\\d{1,2}\\.?|[一二三四五六七八九十]+[、.])\\s+(.{0,80})\\s*$");

    public List<StandardClauseChunk> chunk(ExtractedStandardDocument document, Integer maxChunkChars) {
        int maxChars = maxChunkChars == null || maxChunkChars < 200 ? DEFAULT_MAX_CHUNK_CHARS : maxChunkChars;
        List<StandardClauseChunk> chunks = new ArrayList<>();
        if (document == null || document.getSegments() == null) {
            return chunks;
        }

        ChunkBuilder current = null;
        int fallbackNo = 1;
        for (DocumentTextSegment segment : document.getSegments()) {
            if (segment == null || !StringUtils.hasText(segment.getText())) {
                continue;
            }
            String[] lines = segment.getText().split("\\n");
            for (String rawLine : lines) {
                String line = cleanLine(rawLine);
                if (!StringUtils.hasText(line)) {
                    if (current != null) {
                        current.appendParagraphBreak();
                    }
                    continue;
                }
                Heading heading = parseHeading(line);
                if (heading != null) {
                    addSplitChunks(chunks, current, maxChars);
                    current = new ChunkBuilder(heading.clauseNo, heading.title, segment.getPageNo());
                    current.append(line);
                    continue;
                }
                if (current == null) {
                    current = new ChunkBuilder("AUTO-" + fallbackNo++, "", segment.getPageNo());
                }
                current.append(line);
            }
        }
        addSplitChunks(chunks, current, maxChars);
        return chunks;
    }

    private Heading parseHeading(String line) {
        String normalized = line.replaceFirst("^#{1,6}\\s*", "").trim();
        Matcher matcher = CLAUSE_HEADING.matcher(normalized);
        if (!matcher.matches()) {
            return null;
        }
        String title = matcher.group(2) == null ? "" : matcher.group(2).trim();
        if (title.length() > 80 || title.contains("。")) {
            return null;
        }
        return new Heading(matcher.group(1).replaceAll("\\.$", ""), title);
    }

    private void addSplitChunks(List<StandardClauseChunk> chunks, ChunkBuilder builder, int maxChars) {
        if (builder == null || !StringUtils.hasText(builder.text())) {
            return;
        }
        String text = builder.text();
        if (text.length() <= maxChars) {
            chunks.add(builder.toChunk(text, builder.clauseNo));
            return;
        }
        List<String> parts = splitByNaturalBoundary(text, maxChars);
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (StringUtils.hasText(part)) {
                String clauseNo = builder.clauseNo + "-" + (i + 1);
                chunks.add(builder.toChunk(part, clauseNo));
            }
        }
    }

    private List<String> splitByNaturalBoundary(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String[] paragraphs = text.split("\\n+");
        for (String paragraph : paragraphs) {
            String clean = paragraph.trim();
            if (!StringUtils.hasText(clean)) {
                continue;
            }
            if (clean.length() > maxChars) {
                appendSentenceParts(result, current, clean, maxChars);
                continue;
            }
            if (current.length() > 0 && current.length() + clean.length() + 1 > maxChars) {
                result.add(current.toString().trim());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(clean);
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    private void appendSentenceParts(List<String> result, StringBuilder current, String paragraph, int maxChars) {
        for (String sentence : splitSentences(paragraph)) {
            if (!StringUtils.hasText(sentence)) {
                continue;
            }
            if (sentence.length() > maxChars) {
                flush(result, current);
                int start = 0;
                while (start < sentence.length()) {
                    int end = Math.min(start + maxChars, sentence.length());
                    result.add(sentence.substring(start, end).trim());
                    start = end;
                }
                continue;
            }
            if (current.length() > 0 && current.length() + sentence.length() > maxChars) {
                flush(result, current);
            }
            current.append(sentence);
        }
    }

    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            current.append(ch);
            if ("。；;.!?！？".indexOf(ch) >= 0) {
                sentences.add(current.toString().trim());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            sentences.add(current.toString().trim());
        }
        return sentences;
    }

    private void flush(List<String> result, StringBuilder current) {
        if (current.length() > 0) {
            result.add(current.toString().trim());
            current.setLength(0);
        }
    }

    private String cleanLine(String line) {
        if (line == null) {
            return "";
        }
        return line.replace('\t', ' ').replaceAll(" {2,}", " ").trim();
    }

    private static class Heading {
        private final String clauseNo;
        private final String title;

        private Heading(String clauseNo, String title) {
            this.clauseNo = clauseNo;
            this.title = title;
        }
    }

    private static class ChunkBuilder {
        private final String clauseNo;
        private final String clauseTitle;
        private final Integer pageNo;
        private final StringBuilder text = new StringBuilder();

        private ChunkBuilder(String clauseNo, String clauseTitle, Integer pageNo) {
            this.clauseNo = clauseNo;
            this.clauseTitle = clauseTitle;
            this.pageNo = pageNo;
        }

        private void append(String line) {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
                text.append(' ');
            }
            text.append(line);
        }

        private void appendParagraphBreak() {
            if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') {
                text.append('\n');
            }
        }

        private String text() {
            return text.toString().trim();
        }

        private StandardClauseChunk toChunk(String paragraphText, String chunkClauseNo) {
            StandardClauseChunk chunk = new StandardClauseChunk();
            chunk.setClauseNo(chunkClauseNo);
            chunk.setClauseTitle(clauseTitle);
            chunk.setPageNo(pageNo);
            chunk.setParagraphText(paragraphText.trim());
            chunk.setRetrievalKeywords((chunkClauseNo + " " + clauseTitle).trim());
            return chunk;
        }
    }
}
