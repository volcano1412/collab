package org.project.collab.common.config;
import io.swagger.v3.oas.models.OpenAPI; import io.swagger.v3.oas.models.info.Info; import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig {@Bean OpenAPI collabOpenApi(){return new OpenAPI().info(new Info().title("Collab API").version("1.0.0").description("프로젝트 협업 도구 REST API"));}}
