package one.cax.doc_search;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.cax.doc_search.controller.VectorController;
import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.*;
import one.cax.doc_search.security.JwtTokenProvider;
import one.cax.doc_search.security.JwtsWrapper;
import one.cax.doc_search.security.SecurityConfig;
import one.cax.doc_search.service.ExtractorEngine;
import one.cax.doc_search.service.VectorSearch;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(VectorController.class)
@Import(SecurityConfig.class)
class VectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExtractorEngine extractorEngine;

    @MockBean
    private VectorSearch vectorSearch;

    private String sessionId;

    @Mock
    private JwtsWrapper jwtsWrapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID().toString();
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())  // Apply security configuration
                .build();
    }

    @Test
    void testAddDocument_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        Mockito.when(vectorSearch.addDocument(any(UUID.class), any())).thenReturn(docId);

        JSONObject jsonDoc = TestUtils.getJsonObject();
        var request = new AddTextRequest();
        request.setTitle(jsonDoc.getString(NameUtils.DOC_TITLE));
        request.setContent(jsonDoc.toString());

        var response = new AddTextResponse();
        response.setTextId(docId.toString());
        response.setSessionId(sessionId);
        response.setResponseText("text added successfully!");

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = objectMapper.writeValueAsString(response);
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "addText"))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(responseJson));
    }


    @Test
    void testAddDocument_VectorSearchException() throws Exception {
        Mockito.when(vectorSearch.addDocument(any(UUID.class), any())).thenThrow(new VectorSearchException("Failed to add document"));
        JSONObject jsonDoc = TestUtils.getJsonObject();
        var request = new AddTextRequest();
        request.setTitle(jsonDoc.getString(NameUtils.DOC_TITLE));
        request.setContent(jsonDoc.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        var response = new AddTextResponse();
        response.setSessionId(sessionId);
        response.setResponseText("An error occurred while adding the document: Failed to add document");
        String requestJson = objectMapper.writeValueAsString(request);
        String responseJson = objectMapper.writeValueAsString(response);
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "addText"))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(responseJson));
    }

    @Test
    void testAddDocument_InternalServerError() throws Exception {
        Mockito.when(vectorSearch.addDocument(any(UUID.class), any())).thenThrow(new RuntimeException("Unexpected error"));
        JSONObject jsonDoc = TestUtils.getJsonObject();
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "addText"))
                        .content(jsonDoc.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearch_Success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject searchResult = new JSONObject();
        searchResult.put("result", "search result");

        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenReturn(searchResult);
        var request = new SearchRequest();
        request.setQuery("Test query");
        var requestJson = objectMapper.writeValueAsString(request);
        var searchResponse = new SearchResponse();
        searchResponse.setSessionId(sessionId);
        searchResponse.setResponseText(searchResult.toString());
        var responseJson = objectMapper.writeValueAsString(searchResponse);
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "run"))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(responseJson));
    }

    @Test
    void testSearch_VectorSearchException() throws Exception {
        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenThrow(new VectorSearchException("Failed to process query"));
        ObjectMapper objectMapper = new ObjectMapper();
        var request = new SearchRequest();
        request.setQuery("Test query");
        var requestJson = objectMapper.writeValueAsString(request);

        var response = new SearchResponse();
        response.setSessionId(sessionId);
        response.setResponseText("An error occurred while processing the query: Failed to process query");
        var responseJson = objectMapper.writeValueAsString(response);

        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "run"))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(responseJson));
    }

    @Test
    void testSearch_InternalServerError() throws Exception {
        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenThrow(new RuntimeException("Unexpected error"));
        ObjectMapper objectMapper = new ObjectMapper();
        var request = new SearchRequest();
        request.setQuery("Test query");
        var requestJson = objectMapper.writeValueAsString(request);

        var response = new SearchResponse();
        response.setSessionId(sessionId);
        response.setResponseText("An unexpected error occurred while processing the query: Test query Unexpected error");
        var responseJson = objectMapper.writeValueAsString(response);
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "run"))
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(responseJson));
    }
}