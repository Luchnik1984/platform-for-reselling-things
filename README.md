# Платформа по перепродаже вещей - Бэкенд

##  Описание проекта
  REST API сервис для платформы объявлений о продаже вещей. Проект разрабатывается в рамках курсовой работы командой из 3 человек.
## ВАЖНЫЕ ЗАМЕТКИ
### В pom.xml:
- **Версия JAVA11 изменена на JAVA17**
- **Временно, H2 база принята основной (<scope>runtime</scope> вместо test)**
    для возможности тестирования эндпоинтов до момента создания основной бд.
    Переключить на test, при создании Postgresql бд.

##  Текущий статус
  **Этап 1: Написание DTO и контроллеров** (начало работы)  
  **Текущий спринт:** 
  - Sprint 1 - Инфраструктура и DTO;
  - Sprint 2 - Контроллеры (с заглушками)
  - Sprint 3 - Интеграция и проверка

  **Дата:** 2025-12-16

## ✅ Уже реализовано (исходный код)

### Инфраструктура
- [x] Spring Boot 2.7.15 проект
- [x] Настроены основные зависимости (pom.xml)
- [x] Главный класс HomeworkApplication

### Безопасность
- [x] Spring Security с Basic Auth
- [x] WebSecurityConfig с CORS настройками
- [x] BasicAuthCorsFilter
- [x] PasswordEncoder (BCrypt)
- [x] InMemoryUserDetailsManager для тестирования

### DTO (Data Transfer Objects)
- [x] `Login.java` - аутентификация
- [x] `Register.java` - регистрация
- [x] `Role.java` enum - роли пользователей
- [x] `NewPassword.java` - смена пароля
- [x] `User.java` - данные пользователя
- [x] `UpdateUser.java` - обновление профиля
- [x] `Ad.java` - объявление
- [x] `ExtendedAd.java` - детали объявления
- [x] `Ads.java` - список объявлений
- [x] `CreateOrUpdateAd.java` - создание/обновление
- [x] `Comment.java` - комментарий
- [x] `Comments.java` - список комментариев
- [x] `CreateOrUpdateComment.java` - создание/обновление

### Контроллеры
- [x] `RegController` - /register
- [x] `AuthController.java` - /login
- [x] `UserController.java` - /users/**
- [x] `AdController.java` - /ads/**
- [x] `CommentController.java` - /ads/*/comments/**

##  Активные задачи

### В процессе:


## Запланировано:
### Sprint1 - Инфроструктура и DTO:
- **US1.1:** Настройка Swagger/OpenAPI *(Лучник Иван)*
- **US1.2:** DTO паролей и пользователей *(Гребнев Артём)*
- **US1.3:** DTO объявлений *(Лучник Иван)*
- **US1.4:** DTO комментариев *(Шакурова Дарья)*
### Sprint2 - Контроллеры (пока с заглушками)
- **US2.1:** UserController *(Гребнев Артём)*
- **US2.2:** AdController *(Лучник Иван)*
- **US2.3:** CommentController *(Шакурова Дарья)*
### Sprint3 - Интеграция и проверка 
- **US3.1:** Проверка эндпоинтов *(Все участники, каждый свои)*
- **US3.2:** Проверка CORS с фронтэндом *(Все участники)*
- **US3.3:** Проверка Postman *(Лучник Иван)*

## Завершено:
- **US1.1:** Настройка Swagger/OpenAPI *(Лучник Иван)*
- **US1.3:** DTO объявлений *(Лучник Иван)*
- **US2.2:** AdController *(Лучник Иван)*
- **US1.2:** DTO паролей и пользователей *(Гребнев Артём)*
- **US1.4:** DTO комментариев *(Шакурова Дарья)*
- **US2.1:** UserController *(Гребнев Артём)*
- **US2.3:** CommentController *(Шакурова Дарья)*
- **US3.2:** Проверка CORS с фронтэндом *(Все участники)*
- **US3.3:** Проверка Postman *(Лучник Иван)*

# Запуск проекта
### Требования:
- Java 11 или выше
- Maven 3.6+
- (Опционально) Docker для фронтенда.

## Команды:

### Компиляция проекта
```bash
  ./mvnw clean compile 
```

### Запуск приложения
```bash
  ./mvnw spring-boot:run
```
### Сборка пакета
```bash
  ./mvnw package 
```

### Запуск фронтэнда
### Для Windows (PowerShell)
  Запустите Docker Desktop, затем выполните в терминале:
```bash
     docker run -p 3000:3000 --rm ghcr.io/dmitry-bizin/front-react-avito:v1.21
```

##  Ссылки
- OpenAPI спецификация: в файле openapi.yaml
- Фронтенд: http://localhost:3000
- GitHub проекта: https://github.com/Luchnik1984/platform-for-reselling-things

## Документация API 
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- OpenAPI YAML:** http://localhost:8080/api-docs.yaml


##  Статистика проекта
- **ЭТАП 1:**
- **Эндпоинты:** 17/17 готово (100%)
- **DTO:** 12/12 готово (58%)
- **Контроллеры:** 5/5 готово (100%)
- **Этап 1:** 10/10 UserStories (100%)

##  Технологический стек
- **Java 17**
- **Spring Boot 2.7.15**
- **Spring Security** (Basic Auth)
- **Spring Data JPA** (на следующих этапах)
- **PostgreSQL** (основная БД, на следующих этапах)
- **H2 Database** (тестовая БД, на следующих этапах)
- **Lombok** (упрощение кода)
- **SpringDoc OpenAPI** 

## Архитектурное решение
[Посмотреть полную диаграмму архитектуры](/architecture.mmd)

### Ключевые компоненты:
1. **Пользовательский интерфейс**
   -	Frontend React - веб-приложение на порту 3000
2. **Безопасность**
   -	Spring Security - аутентификация и авторизация
   -	CORS - межсайтовые запросы
3. **Контроллеры**
   -	AuthController - вход и регистрация
   -	UserController - управление профилем
   -	AdController - объявления
   -	CommentController - комментарии
4. **Сервисы**
   -	AuthService - логика аутентификации
   -	UserService - логика пользователей
   -	AdService - логика объявлений
   -	CommentService - логика комментариев
   -	ImageService - работа с картинками
5. **Репозитории**
   -	UserRepository - доступ к данным пользователей
   -	AdRepository - доступ к данным объявлений
   -	CommentRepository - доступ к данным комментариев
6. **База данных**
   -	PostgreSQL - основное хранилище
   -	H2 - тестовая БД
7. **Вспомогательные компоненты**
   -	DTO - объекты для передачи данных
   -	Мапперы - преобразование Entity <--> DTO
   - 
#### Поток данных: 
Frontend → Security → Controller → Service → Repository → Database


##  Команда
- **Гребнев Артём**
- **Лучник Иван**
- **Шакурова Дарья**
