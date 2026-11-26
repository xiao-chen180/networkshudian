package com.example.bookstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 * Swagger UI: /swagger-ui.html
 * JSON:       /v3/api-docs
 * 右上角 Authorize 填入登录接口返回的 accessToken 即可调试需要登录的接口。
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI bookstoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("书香阁 · 网上书店 API")
                        .version("2.0.0")
                        .description("""
                                网上书店后端接口文档。
                                - 认证方式：`Authorization: Bearer <accessToken>`，token 由 `POST /api/auth/login` 获取
                                - 订单金额由后端按会员折扣重算，前端传入的价格会被忽略
                                - 优惠券在下单时核销，取消订单自动退回
                                """)
                        .contact(new Contact().name("书香阁团队")))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}