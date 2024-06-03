package net.judah.zenith.embed;

import java.util.List;

import org.springframework.ai.openai.OpenAiEmbeddingOptions;

public record VectorData(OpenAiEmbeddingOptions options, List<DocData> documents) {

}
