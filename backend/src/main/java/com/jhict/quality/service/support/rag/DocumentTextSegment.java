package com.jhict.quality.service.support.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTextSegment {

    private Integer pageNo;

    private String text;
}
