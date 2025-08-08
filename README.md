<h1>🚀 Система Управления Банковскими Картами</h1>
  <p>
    Backend-приложение на Java и Spring Boot для управления банковскими картами, разработанное в рамках тестового задания.
  </p>
  <p>
    Проект реализует REST API для создания и управления картами, просмотра баланса, переводов между счетами и администрирования. Система поддерживает две роли: <code>USER</code> (клиент) и <code>ADMIN</code> (администратор).
  </p>

<hr />

<h2>💡 Ключевые технологии</h2>
  <table>
    <tr>
      <td><b>Язык и фреймворк</b></td>
      <td>Java 17+, Spring Boot v3.5.4, Spring Security (JWT), Spring Data JPA (Hibernate)</td>
    </tr>
    <tr>
      <td><b>База данных</b></td>
      <td>PostgreSQL, Liquibase (миграции)</td>
    </tr>
    <tr>
      <td><b>Окружение и сборка</b></td>
      <td>Docker, Docker Compose, Maven</td>
    <tr>
      <td><b>Документация</b></td>
      <td>
          Динамически генерируемая Swagger UI и статический контракт API в файле <b><code>docs/openapi.yaml</code></b>
      </td>
    </tr>
    <tr>
      <td><b>Тесты</b></td>
      <td>JUnit 5, Mockito, MockMvc (покрытие сервисного и контроллерного слоев)</td>
    </tr>
  </table>

<hr />

<h2>🛠️ Запуск и использование проекта</h2>
  <p>
    Для запуска проекта необходимы установленные <strong>Docker</strong> и <strong>Docker Compose</strong>.
  </p>

<h3>⚙️ Шаг 1: Сборка проекта</h3>
<p>Склонируйте репозиторий и соберите проект с помощью Maven. Эта команда скачает все зависимости и создаст исполняемый <code>.jar</code> файл.</p>
<p><i>(Выполнять в корневой директории проекта)</i></p>

*   Для Windows:
     <pre><code>mvnw.cmd clean install</code></pre>
*   Для macOS/Linux:
     <pre><code>./mvnw clean install</code></pre>

<h3>🐳 Шаг 2: Запуск через Docker Compose</h3>
<p>Эта команда поднимет контейнеры с базой данных и вашим приложением. При первом запуске Liquibase автоматически создаст схему БД и заполнит ее начальными данными.</p>
<pre><code>docker-compose up --build</code></pre>

<h3>🔗 Шаг 3: Проверка работы</h3>
<ul>
<li>Приложение будет доступно по адресу: <a href="http://localhost:8080">http://localhost:8080</a></li>
<li>Интерактивная документация Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
<li><b>Статический контракт API:</b> Спецификация OpenAPI 3 сохранена в файле <code>/docs/openapi.yaml</code>.</li>
</ul>

<hr />

<h2>🔑 Учетные данные для тестирования</h2>
  <p>
    Для удобства тестирования в базу данных автоматически добавляются два пользователя. Используйте их для получения JWT токена через эндпоинт <code>/api/auth/login</code>.
  </p>

  <ul>
    <li>
      <strong>Администратор:</strong>
      <ul>
        <li><strong>Логин:</strong> <code>admin_acc</code></li>
        <li><strong>Пароль:</strong> <code>adminpass</code></li>
      </ul>
    </li>
    <li>
      <strong>Пользователь:</strong>
      <ul>
        <li><strong>Логин:</strong> <code>user001</code></li>
        <li><strong>Пароль:</strong> <code>userpass</code></li>
      </ul>
    </li>
  </ul>

<h2>🧾 Требования, которые были заявлены к проекту</h2>

<h3>✅ Аутентификация и авторизация</h3>
  <ul>
    <li>Spring Security + JWT</li>
    <li>Роли: <code>ADMIN</code> и <code>USER</code></li>
  </ul>

<h3>✅ Возможности</h3>
<strong>Администратор:</strong>
  <ul>
    <li>Создаёт, блокирует, активирует, удаляет карты</li>
    <li>Управляет пользователями</li>
    <li>Видит все карты</li>
  </ul>

<strong>Пользователь:</strong>
  <ul>
    <li>Просматривает свои карты (поиск + пагинация)</li>
    <li>Запрашивает блокировку карты</li>
    <li>Делает переводы между своими картами</li>
    <li>Смотрит баланс</li>
  </ul>

<h3>✅ API</h3>
  <ul>
    <li>CRUD для карт</li>
    <li>Переводы между своими картами</li>
    <li>Фильтрация и постраничная выдача</li>
    <li>Валидация и сообщения об ошибках</li>
  </ul>

<h3>✅ Безопасность</h3>
  <ul>
    <li>Шифрование данных</li>
    <li>Ролевой доступ</li>
    <li>Маскирование номеров карт</li>
  </ul>

<h3>✅ Работа с БД</h3>
  <ul>
    <li>PostgreSQL или MySQL</li>
    <li>Миграции через Liquibase (<code>src/main/resources/db/migration</code>)</li>
  </ul>

<h3>✅ Документация</h3>
  <ul>
    <li>Swagger UI / OpenAPI — <code>docs/openapi.yaml</code></li>
    <li><code>README.md</code> с инструкцией запуска</li>
  </ul>

<h3>✅ Развёртывание и тестирование</h3>
  <ul>
    <li>Docker Compose для dev-среды</li>
    <li>Liquibase миграции</li>
    <li>Юнит-тесты ключевой бизнес-логики</li>
  </ul>