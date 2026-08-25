package org.project.collab.common.config;

import java.util.List;
import org.project.collab.common.web.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final CurrentUserArgumentResolver resolver;

  public WebConfig(CurrentUserArgumentResolver r) {
    resolver = r;
  }

  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> rs) {
    rs.add(resolver);
  }
}
