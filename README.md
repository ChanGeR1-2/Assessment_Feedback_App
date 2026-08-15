## Assessment Feedback Application

## API Documentation

Interactive documentation is generated from the code and available while the
application is running:

- **Swagger UI** — http://localhost:8080/swagger-ui/index.html
- **OpenAPI spec** — http://localhost:8080/v3/api-docs

To call protected endpoints from the UI, first `POST /api/auth/login` with
`{"email": "lecturer@dissertation.com", "password": "password123"}`, then paste
the returned token into the **Authorize** dialog. This allows endpoints to be called
as the lecturer role.

### Endpoint overview

| Area | Endpoints | Access |
|---|---|---|
| Auth | `POST /api/auth/login` | Public |
| Users | `GET/POST /api/users` | Collection: admin · Individual: authenticated |
| Enrolments | `/api/enrolments`, `/api/students/{studentId}/modules` | Write: admin · Read: authenticated |
| Modules | `GET /api/modules`, `POST /api/modules` | Read: authenticated · Write: admin |
| Assessments | `GET /api/assessments`, `/api/assessments/{id}`, `/api/assessments/stats` | Scoped by role |
| Marking scheme | `/api/assessments/{id}/marking-items` | Owning lecturer |
| Feedback | `POST /api/feedback`, `GET /api/feedback/{id}` | Lecturer writes · student reads own |
| Audio | `/api/feedback/{id}/audio` | Owning lecturer · recipient student |
| Queries | `/api/students/{studentId}/feedback-queries`, `/api/lecturer/feedback-queries`, `/api/feedback-queries/{id}/answer` | Student asks · lecturer answers |
| Tags | `GET /api/tags`, `/api/students/{id}/tag-summary`, `/api/lecturer/tag-summary`, `/api/students/{id}/tag-summary/per-year` | Vocabulary: authenticated · Summaries: own data only |
| Phrases | `/api/phrases` | Lecturer (own only) |

## Entity Relationship Diagrams

Two entity relationship diagrams have been made using Mermaid Live Editor. The first one shows the overall schema and the second shows the omitted extensions of the feedback entity (split up to improve readability).
```mermaid
erDiagram
    app_user |o--o{ course_module : "owns (lecturer)"
    feedback ||--o{ feedback_item : "breaks down into"
    marking_item ||--o{ feedback_item : "based on"
    app_user ||--o{ feedback : "receives (student)"
    app_user ||--o{ feedback : "authors (lecturer)"
    assessment ||--o{ feedback : "receives"
    assessment ||--o{ marking_item : "marked against"
    course_module ||--o{ assessment : "contains"
    app_user ||--o{ enrolment : "enrolled via (student)"
    course_module ||--o{ enrolment : has
    app_user ||--o{ feedback_phrase : "has (lecturer)"
    app_user {
        bigint id PK
        varchar(150) full_name
        varchar(255) email UK
        varchar(255) password_hash
        varchar(50) role
        timestamp created_at
    }
    course_module {
        bigint id PK
        varchar(255) title
        varchar(255) code 
        varchar(9) academic_year
        bigint lecturer_id FK "nullable"
    }
    feedback_phrase {
        bigint id PK
        varchar(100) label
        text text
        timestamp created_at
        bigint lecturer_id FK
    }
    enrolment {
        bigint id PK
        bigint student_id FK
        bigint module_id FK
        timestamp enrolled_at
    }
    assessment {
        bigint id PK
        bigint module_id FK
        varchar(255) title
        timestamp due_date
        timestamp feedback_due_date
        smallint weight
    }
    marking_item {
        bigint id PK
        bigint assessment_id FK
        varchar(255) name 
        smallint max_mark
        smallint position
    }
    feedback {
        bigint id PK
        bigint assessment_id FK
        bigint student_id FK
        bigint lecturer_id FK
        smallint mark
        timestamp created_at
        text summary
        varchar(20) status
    }
    feedback_item {
        bigint id PK
        bigint feedback_id FK
        bigint marking_item_id FK
        smallint awarded_mark
        text comment
    }
```
```mermaid
erDiagram
    app_user ||--o{ feedback : "receives (student)"
    app_user ||--o{ feedback : "authors (lecturer)"
    feedback ||--o| feedback_audio : "may have"
    feedback ||--o{ feedback_tag : "tagged with"
    tag ||--o{ feedback_tag : "applied via"
    feedback ||--o| feedback_query : "may be queried by"
    app_user ||--o{ feedback_query : "asks (student)"
    feedback_query ||--o| feedback_query_answer : "may be answered by"
    app_user ||--o{ feedback_query_answer : "answers (lecturer)"
    feedback {
        bigint id PK
    }
    app_user {
        bigint id PK
    }
    feedback_audio {
        bigint id PK
        bigint feedback_id FK
        varchar(255) filename
        varchar(100) content_type
        bigint size_bytes
        timestamp created_at
    }
    feedback_tag {
        bigint id PK
        bigint feedback_id FK
        bigint tag_id FK
        varchar(20) tag_type 
    }
    tag {
        bigint id PK
        varchar(100) name
    }
    feedback_query {
        bigint id PK
        bigint feedback_id FK
        bigint student_id FK
        text query
        timestamp created_at
    }
    feedback_query_answer {
        bigint id PK
        bigint lecturer_id FK
        bigint feedback_query_id FK
        text answer
        timestamp created_at
    }
```
