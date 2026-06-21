package com.jhict.quality.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One embedding vector in a provider-neutral embedding response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelEmbedding {

    private Integer index;

    private String inputText;

    private List<Double> vector;
}
