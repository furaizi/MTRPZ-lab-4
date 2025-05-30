# mtrpz lab 04: Url shortener
## Контекст та область застосування

***Автори***:

***Платунов Юрій IM-33*** Backend Developer / Team Lead
Відповідає за архітектуру сервісу, реалізацію бізнес-логіки, інтеграцію з базою даних та основні API.

***Скрипник Михайло IM-33*** — Backend Developer
Відповідає за розробку та тестування окремих підсистем (наприклад, авторизації та аналітики), допомогу з розробкою HTTP-інтерфейсу, написання unit-тестів.


**Формат співпраці**: Ми працюємо як команда з двох бекенд-розробників, використовуючи Git для спільної розробки, code-review та ведення історії змін. Front-end не плануємо робити (можливо, базова Swagger-UI чи Postman для тестування).

### Мета проєкту
Освоїти спільну роботу з Git, створити невеликий, але корисний pet-проект з реального життя, попрактикуватися у створенні повноцінного backend-сервісу з роботою з базою даних та інтеграцією кількох підсистем.
### Проблема

Довгі URL-адреси незручні для поширення в соціальних мережах, SMS, друкованих матеріалах та інших каналах комунікації. Користувачам потрібен сервіс, який може створювати короткі, зручні для запам'ятовування посилання та надавати детальну аналітику переходів.

## Цілі
-  Надати можливість користувачам скорочувати довгі URL-адреси до коротких посилань. 
- Забезпечити перенаправлення з короткого посилання на оригінальний URL. 
- Давати прозору статистику по скороченому посиланню
- Створити зручний REST API для роботи з сервісом (створення, перегляд, видалення посилань). 

**Проект створюється не для:**
- Підтримки кастомних доменів або індивідуального налаштування вигляду короткого посилання. 
- Впровадження розширеної аналітики (геолокація, унікальні користувачі, джерела переходів тощо). 
- Розробки користувацького інтерфейсу (веб або мобільний фронтенд). 
- Складна система аналітики (тільки базова статистика)
- Мобільні додатки (тільки веб-інтерфейс)
- Персоналізовані короткі посилання (тільки випадкова генерація)
## Високорівнева архітектура

Система використовує serverless архітектуру на базі AWS Lambda та складається з наступних підсистем:

- **Data Storage Subsystem**: Зберігання даних користувачів та посилань
- **Business Logic Subsystem**: Основна логіка скорочення URL та аналітики
### Технологічний стек

- **Backend**: AWS Lambda функції (Kotlin)
- **База даних**: DynamoDB (основні дані)
- **Message Queue**: AWS MSK
- **API Gateway**: AWS API Gateway

#### Взаємодія підсистем

**Створення і з'єднання підсистем:**

- Використання AWS SAM (Serverless Application Model) для deployment
- Environment Variables для передачі конфігурації між Lambda функціями
- AWS IAM ролі для контролю доступу між сервісами
- AWS EventBridge для асинхронної комунікації між підсистемами

### Business Logic Subsystem

**Що робить Business Logic підсистема:**

- **Shortens links** - створює короткі URL з довгих
- **Redirects users** - перенаправляє з коротких URL на оригінальні довгі URL
- **Processes analytics** - збирає та обробляє статистику використання

#### API Schema

| Method | Path                | Request Body                  | Response Body                                        | Description                           |
| ------ | ------------------- | ----------------------------- | ---------------------------------------------------- | ------------------------------------- |
| POST   | `/links`            | `{ "url": "<original_url>" }` | `{ "id": "<link_id>", "shortUrl": "<short_url>" }`   | Створення нового короткого посилання  |
| GET    | `/{code}`           | —                             | 302 Redirect to `<original_url>`                     | Перенаправлення за коротким кодом     |
| GET    | `/links/{id}`       | —                             | `{ "id": "<link_id>", "url": "<original_url>" }`     | Отримання деталей короткого посилання |
| GET    | `/links/{id}/stats` | —                             | `{ "clicks": <number>, "createdAt": "<timestamp>" }` | Отримання статистики переходів        |
| DELETE | `/links/{id}`       | —                             | 204 No Content                                       | Видалення короткого посилання         |

### Data Storage Subsystem

**Що зберігає підсистема:**

- **Shortened links** - mapping між короткими кодами та оригінальними URL
- **Analytics data** - статистика кліків та поведінка користувачів

**Data Persistence Strategy:**

**High Availability:**

- DynamoDB: Multi-AZ replication автоматично
- S3: 99.999999999% (11 9's) durability

**Backup Strategy:**

- DynamoDB: Point-in-time recovery + daily backups
- Redis: Persistence не потрібна (тільки cache)
- S3: Cross-region replication для критичних даних

**Disaster Recovery:**

- RTO (Recovery Time Objective): < 4 години
- RPO (Recovery Point Objective): < 1 година
- Automated failover через AWS infrastructure

## Альтернативи

Перед вибором serverless-архітектури ми розглянули кілька варіантів реалізації:

| #   | Варіант                                           | Переваги                                                                                              | Недоліки / компроміси                                                                                                                                | Чому відхилено                                                                                                      |
| --- | ------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| 1   | Моноліт + PostgreSQL на одній VM/EC2              | Найпростіший деплой — одна інстанція, Знайомий стек (Spring Boot/Ktor + SQL), Повний контроль ОС і БД | Скейлінг — лише vertical resize або ручний LB, Адміністрування ОС, патчі, backup, Один SPOF, HA дороге, DevOps ≈ 30 % часу                           | Хотіли pay-per-use та мінімум Ops. Моноліт не дає авто-скейл і потребує значних витрат на підтримку інфраструктури. |
| 2   | Мікросервіси в Kubernetes (EKS/k3s)               | Чітке розділення доменів, Vendor-neutral деплой, Продакшн-досвід (Helm, ArgoCD)                       | Висока складність для 2-х людей — кластер, CI/CD, observability, Дорожче (мінімум 2–3 EC2), Більшість часу — «будуємо платформу», а не бізнес-логіку | Обмежений час проєкту; Kubernetes відволікає від основної мети — швидкої розробки сервісу.                          |
| 3   | Реляційна БД (PostgreSQL/Aurora) замість DynamoDB | ACID-транзакції, JOIN-и, Легко тестувати локально (docker-compose)                                    | Потрібен окремий інстанс/кластерf Ручне масштабування, schema-migrations, Always-on оплата навіть у фазі low-load                                    | Для нашої простої key-value схеми вистачає DynamoDB; вона дає авто-скейл та free-tier без адміністрування.          |
| 4   | Статистика прямо в БД                             | Одне UPDATE — мінімальна затримка                                                                     | Немає відтворюваності подій (replay), Складно додати нові види аналітики, Інтенсивні записи/читання на один shard                                    | Kafka (MSK) + асинхронний консьюмер забезпечує розширюваність аналітики й демонструє event-driven патерн.           |
| 5   | AWS Fargate (container-based serverless)          | Той самий Docker-образ локально й у хмарі, Підходить для long-running HTTP                            | Платиш від 1 хвилини за запуск — дорожче за Lambda, Потрібен репозиторій образів, Cold start ≈ 15–20 с при Kotlin                                    | Lambda + SAM → деплой у 2 команди; 1 млн безкоштовних викликів/місяць закриває потреби лабораторної.                |

---

### Обрана архітектура: Serverless

- **AWS Lambda** для business logic — швидкий деплой та pay-per-request.
    
- **DynamoDB** для key-value з авто-скейлом.
    
- **MSK + EventBridge** для асинхронної аналітики.
    
- **API Gateway** для відкритого REST API.
    

**Переваги**:

- Автоматичне масштабування без управління серверами.
    
- Мінімальні операційні витрати.
    
- Швидка розробка та деплой.
    

**Недоліки**:

- Холодні старти можуть збільшувати затримку.
    
- Ліміт часу виконання функцій (до 15 хв.).
    
- Vendor lock-in на AWS.

## Діаграма

![diagram](./static/mtrpz-lab-4.drawio.png)

