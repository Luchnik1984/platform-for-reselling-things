package ru.skypro.homework.service.uniteTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.AdServiceImpl;
import ru.skypro.homework.util.SecurityUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты {@link AdServiceImpl}.
 * <p>
 * Фокус: CRUD + проверка прав (author vs ADMIN).
 */
@ExtendWith(MockitoExtension.class)
class AdServiceUnitTest {

    @Mock private AdRepository adRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdMapper adMapper;
    @Mock private Authentication authentication;

    @InjectMocks
    private AdServiceImpl adService;

    @Test
    void getAd_shouldThrowAdNotFoundException_whenNotExists() {
        when(adRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(AdNotFoundException.class, () -> adService.getAd(1));
    }

    @Test
    void updateAd_shouldThrowAccessDenied_whenNotAdminAndNotAuthor() {
        when(authentication.getName()).thenReturn("not-owner@test.ru");

        UserEntity owner = new UserEntity();
        owner.setEmail("owner@test.ru");

        AdEntity adEntity = new AdEntity();
        adEntity.setId(10);
        adEntity.setAuthor(owner);

        when(adRepository.findById(10)).thenReturn(Optional.of(adEntity));

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("Updated");
        dto.setPrice(100);
        dto.setDescription("Updated desc");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            assertThrows(AccessDeniedException.class,
                    () -> adService.updateAd(10, authentication, dto));

            verify(adRepository, never()).save(any());
        }
    }

    @Test
    void deleteAd_shouldDelete_whenAdmin() {
        when(authentication.getName()).thenReturn("admin@test.ru");

        UserEntity owner = new UserEntity();
        owner.setEmail("owner@test.ru");

        AdEntity adEntity = new AdEntity();
        adEntity.setId(10);
        adEntity.setAuthor(owner);

        when(adRepository.findById(10)).thenReturn(Optional.of(adEntity));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(true);

            adService.deleteAd(10, authentication);

            verify(adRepository).delete(adEntity);
        }
    }

    @Test
    void updateAd_shouldUpdate_whenAuthor() {
        when(authentication.getName()).thenReturn("owner@test.ru");

        UserEntity owner = new UserEntity();
        owner.setEmail("owner@test.ru");

        AdEntity adEntity = new AdEntity();
        adEntity.setId(10);
        adEntity.setAuthor(owner);

        when(adRepository.findById(10)).thenReturn(Optional.of(adEntity));

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("Updated");
        dto.setPrice(100);
        dto.setDescription("Updated desc");

        Ad mapped = new Ad();
        mapped.setPk(10);

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            when(adRepository.save(adEntity)).thenReturn(adEntity);
            when(adMapper.toDto(adEntity)).thenReturn(mapped);

            Ad result = adService.updateAd(10, authentication, dto);

            assertNotNull(result);
            verify(adMapper).updateEntityFromDto(dto, adEntity);
            verify(adRepository).save(adEntity);
            verify(adMapper).toDto(adEntity);
            verify(adRepository).findById(10);
        }
    }

    @Test
    void deleteAd_shouldThrowAccessDenied_whenNotAdminAndNotAuthor() {
        when(authentication.getName()).thenReturn("not-owner@test.ru");

        UserEntity owner = new UserEntity();
        owner.setEmail("owner@test.ru");

        AdEntity adEntity = new AdEntity();
        adEntity.setId(10);
        adEntity.setAuthor(owner);

        when(adRepository.findById(10)).thenReturn(Optional.of(adEntity));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            assertThrows(AccessDeniedException.class, () -> adService.deleteAd(10, authentication));

            verify(adRepository, never()).delete(any());
        }
    }

    @Test
    void deleteAd_shouldThrowAdNotFoundException_whenMissing() {
        when(adRepository.findById(10)).thenReturn(Optional.empty());
        assertThrows(AdNotFoundException.class, () -> adService.deleteAd(10, authentication));
    }

}