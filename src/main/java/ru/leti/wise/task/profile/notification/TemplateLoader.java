package ru.leti.wise.task.profile.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class TemplateLoader {

    public String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            var template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Template loaded: name='{}', size={} bytes", templateName, template.length());
            return template;
        } catch (IOException e) {
            log.error("Failed to load template: name='{}'", templateName, e);
            throw new RuntimeException("Не удалось загрузить шаблон: " + templateName, e);
        }
    }

    public String loadTemplateWithParams(String templateName, Map<String, String> params) {
        log.debug("Template loaded with params: name='{}', params={}", templateName, params.keySet());
        String template = loadTemplate(templateName);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return template;
    }
}