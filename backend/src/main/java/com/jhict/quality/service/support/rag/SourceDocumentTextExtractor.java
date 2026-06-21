package com.jhict.quality.service.support.rag;

import java.nio.file.Path;

public interface SourceDocumentTextExtractor {

    boolean supports(String fileType);

    ExtractedStandardDocument extract(String sourceFileName, String sourceFilePath,
                                      Path path, String fileType) throws Exception;
}
