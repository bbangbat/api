package com.bbangbat

import com.bbangbat.auth.resolver.AuthMember
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    init {
        // @AuthMember는 SecurityContext에서 주입되는 커스텀 파라미터라
        // springdoc이 쿼리 파라미터로 오인하지 않도록 명세에서 제외한다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthMember::class.java)
    }

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("빵밭 API")
                    .description("대전 빵집 실시간 정보 공유 서비스")
                    .version("v1.0.0"),
            ).addSecurityItem(SecurityRequirement().addList("Bearer"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "Bearer",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
}
