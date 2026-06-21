package com.jhict.quality.service.support.rag;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ExtractedStandardDocument {

    private String sourceFileName;

    private String sourceFilePath;

    private String fileType;

    private List<DocumentTextSegment> segments = new ArrayList<>();
}
