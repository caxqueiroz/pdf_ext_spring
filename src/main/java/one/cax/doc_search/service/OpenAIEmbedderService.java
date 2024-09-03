package one.cax.doc_search.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.azure.core.credential.AzureKeyCredential;
import one.cax.doc_search.exception.EmbedderException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenAIEmbedderService is a service that embeds text using the OpenAI API.
 */
@Service
public class OpenAIEmbedderService implements Embedder {

    /* openAIClient  */
    private final OpenAIClient openAIClient;
    /* model ide  */
    private String model;

    /**
     * Constructor for OpenAIEmbedderService
     */
    public OpenAIEmbedderService(String apiUrl, String apiKey, String apiModel) {
        openAIClient = new OpenAIClientBuilder()
                .endpoint(apiUrl)
                .credential(new AzureKeyCredential(apiKey))
                .buildClient();
        this.model = apiModel;
    }


    /**
     * Embed a text using OpenAI API
     *
     * @param text - the text to embed
     * @return - the embedded text as VectorFloat object
     * @throws EmbedderException - if the text is null or empty
     */
    public float[] embed(String text) throws EmbedderException {

        if (text == null || text.isEmpty()) {
            throw new EmbedderException("Text cannot be null or empty");
        }

        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(List.of(text));
        Embeddings embeddings = openAIClient.getEmbeddings(model, embeddingsOptions);
        var floatEmbeddings = embeddings.getData().get(0).getEmbedding();
        float[] vector = new float[floatEmbeddings.size()];
        for (int i = 0; i < floatEmbeddings.size(); i++) {
            vector[i] = floatEmbeddings.get(i);
        }

        return vector;
    }

    /**
     * Embed a batch of texts using OpenAI API
     *
     * @param texts - the texts to embed
     * @return - the embedded texts as a 2D array of float
     * @throws EmbedderException - if the texts are null or empty
     */
    @Override
    public float[][] embedBatch(List<String> texts) throws EmbedderException {

        if (texts == null || texts.isEmpty()) {
            throw new EmbedderException("Texts cannot be null or empty");
        }

        EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(texts);
        Embeddings embeddings = openAIClient.getEmbeddings(model, embeddingsOptions);
        var responseSize = embeddings.getData().size();
        float[][] vectors = new float[responseSize][];
        int index = 0;
        for (EmbeddingItem item : embeddings.getData()) {
            var embedding = item.getEmbedding();
            float[] vectorArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vectorArray[i] = embedding.get(i);
            }
            vectors[index++] = vectorArray;
        }
        return vectors;
    }
}
