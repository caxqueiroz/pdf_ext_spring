package one.cax.doc_search.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(new AnonymousAuthenticationFilter("anonymous", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")), JwtTokenFilter.class);
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(e -> e.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        http.authorizeHttpRequests(requests -> {
            requests.requestMatchers("/extract/upload/").permitAll();
            requests.requestMatchers("/actuator/**").permitAll();
            requests.requestMatchers("/search/**").permitAll();
            requests.requestMatchers("/session/**").permitAll();
            requests.requestMatchers("/docs/**").permitAll();
            requests.requestMatchers("/swagger-ui/**").permitAll();
            requests.requestMatchers("/v3/api-docs/**").permitAll();
            requests.requestMatchers("/swagger-resources/**").permitAll();
            requests.requestMatchers("/swagger-resources").permitAll();
            requests.anyRequest().authenticated();
        });
        http.csrf(CsrfConfigurer::disable);
        return http.build();
    }

}