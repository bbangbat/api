package com.bbangbat.auth

import com.bbangbat.auth.jwt.JwtAuthenticationEntryPoint
import com.bbangbat.auth.jwt.JwtAuthenticationFilter
import com.bbangbat.auth.jwt.JwtProperties
import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.oauth2.CustomOAuth2UserService
import com.bbangbat.auth.oauth2.OAuth2AuthenticationSuccessHandler
import com.bbangbat.auth.oauth2.OAuth2RedirectUriCookieFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

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
    fun filterChain(
        http: HttpSecurity,
        @Value("\${app.frontend-allowed-origins:}") allowedOrigins: String,
    ): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**", "/oauth2/**", "/login/**").permitAll()
                it.requestMatchers(POST, "/api/members/signup").permitAll()
                it.requestMatchers(POST, "/api/members/link").permitAll()
                it.requestMatchers(GET, "/api/members/nickname/check").permitAll()
                it.requestMatchers(GET, "/api/health").permitAll()
                it.requestMatchers(GET, "/api/stores", "/api/stores/*").permitAll()
                it.requestMatchers(GET, "/api/search").permitAll()
                it.requestMatchers(GET, "/api/congestion", "/api/congestion/*").permitAll()
                it.requestMatchers(POST, "/api/congestion").permitAll()
                it.requestMatchers(GET, "/api/talks", "/api/talks/summary").permitAll()
                it.requestMatchers(GET, "/api/reviews").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/docs", "/docs/**").permitAll()
                it.anyRequest().authenticated()
            }.oauth2Login {
                it.userInfoEndpoint { endpoint -> endpoint.userService(customOAuth2UserService) }
                it.successHandler(oAuth2AuthenticationSuccessHandler)
            }.exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .addFilterBefore(JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(
                OAuth2RedirectUriCookieFilter(allowedOrigins),
                OAuth2AuthorizationRequestRedirectFilter::class.java,
            ).build()

    @Bean
    fun corsConfigurationSource(
        @Value("\${app.cors.allowed-origins}") allowedOrigins: String,
    ): CorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                this.allowedOrigins = allowedOrigins.split(",").map { it.trim() }
                allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
