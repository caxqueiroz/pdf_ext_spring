package io.cax.pdf_ext;

import io.cax.pdf_ext.controller.ExtractController;
import io.cax.pdf_ext.security.JwtTokenProvider;
import io.cax.pdf_ext.security.JwtsWrapper;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.jsonwebtoken.Jwts;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(initializers = ExtractControllerTest.Initializer.class)
class ExtractControllerTest {

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            ConfigurableEnvironment env = configurableApplicationContext.getEnvironment();
            Map<String, Object> map = new HashMap<>();
            map.put("auth_public_key", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            env.getPropertySources().addFirst(new MapPropertySource("test", map));
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private ExtractorEngine extractorEngine;

    @InjectMocks
    private ExtractController extractController;
    @Mock
    private JwtsWrapper jwtsWrapper;

    private MockMvc mockMvc;

    private static final KeyPair keyPair = generateKeyPair();

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(extractController, "tempFolder", "/tmp/");

        // Create mock Timer and Counter
        Timer mockTimer = mock(Timer.class);
        Counter mockCounter = mock(Counter.class);
        when(meterRegistry.timer(anyString())).thenReturn( mockTimer);
        when(meterRegistry.counter(anyString())).thenReturn(mockCounter);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) // Apply Spring Security filters
                .build();
    }


    @Test
    void endpointReturnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());
        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointReturnsOkWithValidJwt() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", createPdf("test data"));
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        String jwtToken = generateToken("testUser");

        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    // write a test that generates a invalid JWT token message
    @Test
    void endpointReturnsUnauthorizedWithInvalidJwt() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", createPdf("test data"));
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        String jwtToken = generateToken("testUser");

        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken + "invalid")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    // write a test the generates a expired JWT token message
    @Test
    void endpointReturnsUnauthorizedWithExpiredJwt() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", createPdf("test data"));
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        String jwtToken = Jwts.builder()
                .setSubject("testUser")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(keyPair.getPrivate())
                .compact();

        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    // write a test where header is null and expect unauthorized
    @Test
    void endpointReturnsUnauthorizedWithNullHeader() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", createPdf("test data"));
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    // write a test that generates a InvalidKeySpecException message
    @Test
    void getPublicKeyThrowsInvalidKeySpecException() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtsWrapper);

        // Pass an invalid key spec
        String invalidKeySpec = "invalidKeySpec";

        assertThrows(RuntimeException.class, () -> {
            jwtTokenProvider.getAuthentication(invalidKeySpec);
        });
    }


    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return Jwts.builder()
                .claims(claims)
                .signWith(keyPair.getPrivate())
                .compact();
    }

    public static byte[] createPdf(String content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText(content);
            contentStream.endText();
            contentStream.close();

            document.save(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }


}



