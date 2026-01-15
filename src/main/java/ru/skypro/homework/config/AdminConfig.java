package ru.skypro.homework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Конфигурация для регистрации администраторов.
 * Настройки берутся из application-dev.yml и .env
 */
@Data
@Component
@ConfigurationProperties(prefix = "admin.registration")
public class AdminConfig {

    /**
     * Разрешена ли регистрация администраторов через API
     */
    private boolean enabled = false;

    /**
     * Секретный код для регистрации администратора
     */
    private String code;

    /**
     * Белый список email для автоматической регистрации как ADMIN
     * (через запятую)
     */
    private String emailWhitelist;

    /**
     * Возвращает список email из whitelist
     */
    public List<String> getEmailWhitelistAsList() {
        if (emailWhitelist == null || emailWhitelist.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(emailWhitelist.split("\\s*,\\s*"));
    }

    /**
     * Проверяет, находится ли email в белом списке
     */
    public boolean isEmailInWhitelist(String email) {
        return getEmailWhitelistAsList().contains(email);
    }

    /**
     * Проверяет валидность кода администратора
     */
    public boolean isValidAdminCode(String providedCode) {
        if (!enabled || code == null || code.isEmpty()) {
            return false;
        }
        return code.equals(providedCode);
    }
}
