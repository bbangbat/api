package com.bbangbat.auth

import com.bbangbat.auth.jwt.JwtAuthenticationEntryPoint
import com.bbangbat.auth.jwt.JwtAuthenticationFilter
import com.bbangbat.auth.jwt.JwtProperties
import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.oauth2.CustomOAuth2UserService
import com.bbangbat.auth.oauth2.OAuth2AuthenticationSuccessHandler
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val jwtProvider: JwtProvider,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll()
                it.requestMatchers(POST, "/api/members/signup").permitAll()
                it.requestMatchers(GET, "/api/stores/**").permitAll()
                it.requestMatchers(POST, "/api/stores/*/congestion").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/docs/**").permitAll()
                it.anyRequest().authenticated()
            }.oauth2Login {
                it.userInfoEndpoint { endpoint -> endpoint.userService(customOAuth2UserService) }
                it.successHandler(oAuth2AuthenticationSuccessHandler)
            }.exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
