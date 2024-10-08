package one.cax.doc_search;

import io.jsonwebtoken.Jwts;
import one.cax.doc_search.controller.ExtractController;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.security.JwtTokenProvider;
import one.cax.doc_search.security.JwtTokenProviderException;
import one.cax.doc_search.security.JwtsWrapper;
import one.cax.doc_search.security.SecurityConfig;
import one.cax.doc_search.service.ExtractorEngine;
import one.cax.doc_search.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExtractController.class)
@Import(SecurityConfig.class)
@ContextConfiguration(initializers = ExtractControllerTest.Initializer.class)
@ActiveProfiles("test")
class ExtractControllerTest {

    private static final KeyPair keyPair = generateKeyPair();
    @MockBean
    private FileService fileService;

    @MockBean
    private ExtractorEngine extractorEngine;

    @InjectMocks
    private ExtractController extractController;
    @Mock
    private JwtsWrapper jwtsWrapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return Jwts.builder()
                .claims(claims)
                .signWith(keyPair.getPrivate())
                .compact();
    }

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(jwtTokenProvider, "publicKeyValue", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())  // Apply security configuration
                .build();
    }


    @Test
    void endpointReturnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());

        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());
        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointReturnsOkWithValidJwt() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", TestUtils.createPdf("test data"));

        XDoc mockXDoc = new XDoc();
        when(extractorEngine.extractTextFromPDF(any(byte[].class))).thenReturn(mockXDoc);
        when(fileService.extractTextFrom(any(byte[].class), eq("application/pdf"))).thenReturn(mockXDoc);

        String jwtToken = generateToken("testUser");


        // Mock the JwtTokenProvider to return a valid authentication object
        when(jwtTokenProvider.validate(anyString())).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(anyString())).thenReturn(new UsernamePasswordAuthenticationToken("testUser", null, Collections.emptyList()));

        mockMvc.perform(multipart("/extract/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    // write a test that generates a invalid JWT token message
    @Test
    void endpointReturnsUnauthorizedWithInvalidJwt() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", TestUtils.createPdf("test data"));
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

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
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", TestUtils.createPdf("test data"));
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

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
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", TestUtils.createPdf("test data"));
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

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

        assertThrows(JwtTokenProviderException.class, () -> {
            jwtTokenProvider.getAuthentication(invalidKeySpec);
        });
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            ConfigurableEnvironment env = configurableApplicationContext.getEnvironment();
            Map<String, Object> map = new HashMap<>();
            map.put("auth_public_key", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            env.getPropertySources().addFirst(new MapPropertySource("test", map));
        }
    }

}



