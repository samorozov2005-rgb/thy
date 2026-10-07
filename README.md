# Task Manager — Thymeleaf + Spring Security

Учебное приложение для управления задачами. В одном проекте показаны два способа аутентификации: обычная сессия и form-login для HTML-интерфейса, JWT для REST API.

## Стек

- Java 21, Maven, Spring Boot 3
- Spring Web, Spring Data JPA, Spring Security
- Thymeleaf и Thymeleaf Extras Spring Security 6
- Bean Validation
- JJWT
- H2 in-memory
- JUnit 5, MockMvc, Spring Security Test

## Как запустить

Нужны JDK 21 и Maven:

```bash
mvn spring-boot:run
```

Проверка тестами:

```bash
mvn clean test
```

## Адрес

Откройте [http://localhost:8080/login](http://localhost:8080/login). Запрос к `/web/tasks` без сессии автоматически перенаправляется на эту страницу.

## Пользователи

| Роль | Логин | Пароль | Возможности |
|---|---|---|---|
| USER | `user` | `user123` | просмотр, создание, редактирование |
| ADMIN | `admin` | `admin123` | просмотр, создание, редактирование, удаление |

Пароли кодируются `BCryptPasswordEncoder`; `{noop}` не используется.

## Web endpoints

- `GET /login` — форма входа
- `GET /web/tasks` — список задач
- `GET /web/tasks/new` — форма создания
- `POST /web/tasks` — создание
- `GET /web/tasks/{id}` — подробности
- `GET /web/tasks/{id}/edit` — форма редактирования
- `POST /web/tasks/{id}` — обновление
- `POST /web/tasks/{id}/delete` — удаление, только ADMIN

Веб-часть использует session-based form-login. CSRF включён; Thymeleaf автоматически помещает CSRF-токен в POST-формы с `th:action`.

## REST API

REST-часть stateless и принимает JWT в заголовке `Authorization: Bearer <token>`.

Получить токен:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}'
```

Использовать токен:

```bash
curl http://localhost:8080/api/tasks \
  -H 'Authorization: Bearer ВАШ_ТОКЕН'
```

Endpoints:

- `POST /api/auth/login`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}` — только ADMIN

Пример тела задачи:

```json
{
  "title": "Новая задача",
  "description": "Описание",
  "priority": "HIGH",
  "status": "TODO"
}
```

## Как устроено приложение

`TaskWebController` — обычный `@Controller`: кладёт данные в `Model` и возвращает имя Thymeleaf-шаблона. `TaskRestController` — `@RestController`: возвращает объекты, которые сериализуются в JSON. Оба контроллера используют один `TaskService`; только сервис обращается к `TaskRepository`.

После успешного создания, изменения или удаления веб-контроллер возвращает `redirect:/web/tasks`. Это паттерн Post-Redirect-Get: обновление страницы не отправляет POST повторно.

Одна форма `templates/tasks/form.html` обслуживает создание и редактирование. `th:object` связывает форму с `TaskRequest`, а `th:field` — конкретные поля с его свойствами. `@Valid` запускает ограничения DTO, `BindingResult` позволяет вернуть ту же форму и вывести `th:errors` рядом с неверным полем.

Навигация объявлена один раз как `th:fragment="navbar"` в `layout.html` и подключается на страницах через `th:replace`. Атрибут `sec:authorize` скрывает удаление от USER и показывает его ADMIN. Это только уровень интерфейса: сервер дополнительно запрещает USER endpoint удаления в `SecurityFilterChain`.

Две цепочки Spring Security разделяют режимы:

- `/api/**`: stateless, JWT-фильтр, CSRF выключен только для API;
- web: HTTP-сессия, form-login, logout и включённый CSRF.

Несуществующая задача обрабатывается как HTTP 404: веб получает понятную страницу, REST — JSON с сообщением.

## Структура

```text
src/main/java/com/example/taskmanager/
├── config/       SecurityConfig, DataInitializer
├── controller/   MVC, REST и обработчики ошибок
├── dto/          TaskRequest, LoginRequest, JwtResponse
├── entity/       Task, Priority, TaskStatus
├── repository/   TaskRepository
├── security/     JwtService, JwtAuthenticationFilter
└── service/      TaskService, TaskNotFoundException

src/main/resources/
├── static/css/style.css
├── templates/auth/login.html
├── templates/error/404.html
├── templates/tasks/{list,detail,form}.html
├── templates/layout.html
└── application.properties
```

При старте `DataInitializer` добавляет три демонстрационные задачи в H2. Так как база in-memory, данные живут до остановки приложения.
