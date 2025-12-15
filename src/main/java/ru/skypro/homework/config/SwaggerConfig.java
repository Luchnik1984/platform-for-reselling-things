package ru.skypro.homework.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Платформа по перепродаже вещей - API")
                        .version("1.0.0")
                        .description("""
                               REST API для дипломного проекта
                              \s
                               ==Команда проекта==
                               Гребнев Артём
                               Лучник Иван
                               Шакурова Дарья
                              \s
                               GitHub:\s
                               https://github.com/Luchnik1984/platform-for-reselling-things
                       \s"""))

                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Локальный сервер разработки"),
                        new Server()
                                .url("http://localhost:3000")
                                .description("Фронтенд React приложение")
                ));
    }
}
