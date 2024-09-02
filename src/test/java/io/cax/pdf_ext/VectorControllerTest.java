package io.cax.pdf_ext;

import io.cax.pdf_ext.controller.VectorController;
import io.cax.pdf_ext.exception.VectorSearchException;
import io.cax.pdf_ext.model.NameUtils;
import io.cax.pdf_ext.model.XPage;
import io.cax.pdf_ext.security.JwtTokenProvider;
import io.cax.pdf_ext.security.JwtsWrapper;
import io.cax.pdf_ext.security.SecurityConfig;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.cax.pdf_ext.service.VectorSearch;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
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

        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "doc"))
                        .content(jsonDoc.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(docId.toString()));
    }



    @Test
    void testAddDocument_VectorSearchException() throws Exception {
        Mockito.when(vectorSearch.addDocument(any(UUID.class), any())).thenThrow(new VectorSearchException("Failed to add document"));
        JSONObject jsonDoc = TestUtils.getJsonObject();
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "doc"))
                        .content(jsonDoc.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("An error occurred while adding the document: Failed to add document!"));
    }

    @Test
    void testAddDocument_InternalServerError() throws Exception {
        Mockito.when(vectorSearch.addDocument(any(UUID.class), any())).thenThrow(new RuntimeException("Unexpected error"));
        JSONObject jsonDoc = TestUtils.getJsonObject();
        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "doc"))
                        .content(jsonDoc.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearch_Success() throws Exception {
        JSONObject searchResult = new JSONObject();
        searchResult.put("result", "search result");

        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenReturn(searchResult);

        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "query"))
                        .content("Test query")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(searchResult.toString()));
    }

    @Test
    void testSearch_VectorSearchException() throws Exception {
        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenThrow(new VectorSearchException("Failed to process query"));

        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "query"))
                        .content("Test query")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("An error occurred while processing the query: Failed to process query!"));
    }

    @Test
    void testSearch_InternalServerError() throws Exception {
        Mockito.when(vectorSearch.search(any(UUID.class), anyString())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post(TestUtils.getURIForSearch(sessionId, "query"))
                        .content("Test query")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while processing the query: Test query!"));
    }
}