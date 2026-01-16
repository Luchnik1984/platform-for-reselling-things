package ru.skypro.homework.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Общая конфигурация для всех мапперов MapStruct.
 *
 * <p>Настройки:
 * <ul>
 *   <li>{@code componentModel = "spring"} - создаёт Spring beans</li>
 *   <li>{@code unmappedTargetPolicy = ReportingPolicy.IGNORE} - игнорирует несопоставленные поля</li>
 *   <li>{@code unmappedSourcePolicy = ReportingPolicy.IGNORE} - игнорирует несопоставленные исходные поля</li>
 * </ul>
 *
 * <p>Все мапперы наследуют эту конфигурацию через {@link MapperConfig}.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MapStructConfig {
}
