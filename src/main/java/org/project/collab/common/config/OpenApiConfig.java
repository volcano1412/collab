package org.project.collab.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.project.collab.common.web.CurrentUser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.discoverer.SpringDocParameterNameDiscoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI collabOpenApi() {
    return new OpenAPI()
        .info(new Info().title("Collab API").version("1.0.0").description("프로젝트 협업 도구 REST API"));
  }

  @Bean
  public OperationCustomizer currentUserHeaderCustomizer(
      SpringDocParameterNameDiscoverer parameterNameDiscoverer) {
    return (operation, handlerMethod) -> {
      var methodParameters = handlerMethod.getMethodParameters();
      var discoveredNames = parameterNameDiscoverer.getParameterNames(handlerMethod.getMethod());
      Set<String> currentUserParameterNames =
          IntStream.range(0, methodParameters.length)
              .filter(
                  index ->
                      Arrays.stream(methodParameters[index].getParameterAnnotations())
                          .anyMatch(
                              annotation ->
                                  annotation
                                      .annotationType()
                                      .getName()
                                      .equals(CurrentUser.class.getName())))
              .mapToObj(index -> discoveredNames == null ? null : discoveredNames[index])
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      if (currentUserParameterNames.isEmpty()) {
        return operation;
      }

      if (operation.getParameters() != null) {
        operation
            .getParameters()
            .removeIf(
                parameter ->
                    "query".equals(parameter.getIn())
                        && currentUserParameterNames.contains(parameter.getName()));
      }

      operation.addParametersItem(
          new HeaderParameter()
              .name("X-User-Id")
              .description("요청자 사용자 ID")
              .required(true)
              .schema(new IntegerSchema().format("int64")));
      return operation;
    };
  }
}
