# ForumQuestion Testing Guide

Deze guide legt uit hoe je de ForumQuestion functionaliteit kunt testen, zowel automatisch als handmatig.

## Structuur

ForumQuestion gebruikt een **self-referential relatie**:
- **Top-level questions** (posts): `question = null` - Dit zijn de hoofdonderwerpen
- **Comments**: `question != null` - Dit zijn reacties op vragen

### Belangrijke regels:
1. ✅ Een question kan **meerdere comments** hebben
2. ✅ Een comment heeft **altijd één parent question**
3. ❌ Een comment kan **niet meerdere questions** hebben
4. ✅ Wanneer een question wordt verwijderd, worden alle comments ook verwijderd (cascade)

## Automatische Tests

### Repository Tests uitvoeren

```bash
# Alle tests uitvoeren
mvn test

# Alleen ForumQuestion tests
mvn test -Dtest=ForumQuestionRepositoryTest
```

De tests verifiëren:
- ✅ Top-level questions kunnen worden aangemaakt
- ✅ Een question kan meerdere comments hebben
- ✅ Comments hebben altijd één parent
- ✅ Top-level questions worden correct opgehaald
- ✅ Cascade delete werkt (comments worden verwijderd met parent)
- ✅ Een comment kan niet meerdere parents hebben

## Handmatig Testen met API

### 1. Maak een test gebruiker aan (via AuthController)

```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123"
}
```

### 2. Login om een token te krijgen

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

Bewaar de token voor de volgende requests (gebruik in `Authorization: Bearer <token>` header).

### 3. Maak een top-level question (POST)

```bash
POST http://localhost:8080/api/forum/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Wat is de beste manier om te stemmen?"
}
```

**Verwachte response (201 Created):**
```json
{
  "id": 1,
  "body": "Wat is de beste manier om te stemmen?",
  "createdAt": "2024-01-15T10:30:00",
  "comments": []
}
```

**Test checklist:**
- ✅ Status code is 201
- ✅ `id` is aanwezig
- ✅ `question` veld is null (of niet aanwezig in JSON)
- ✅ `createdAt` is automatisch gezet

### 4. Haal alle top-level questions op (GET)

```bash
GET http://localhost:8080/api/forum/questions
Authorization: Bearer <jouw-token>
```

**Verwachte response (200 OK):**
```json
[
  {
    "id": 1,
    "body": "Wat is de beste manier om te stemmen?",
    "createdAt": "2024-01-15T10:30:00",
    "comments": []
  }
]
```

**Test checklist:**
- ✅ Status code is 200
- ✅ Alleen top-level questions (geen comments)
- ✅ Array wordt geretourneerd

### 5. Maak een comment op een question (POST)

```bash
POST http://localhost:8080/api/forum/1/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Ik denk dat je online kunt stemmen via DigiD"
}
```

**Verwachte response (201 Created):**
```json
{
  "id": 2,
  "body": "Ik denk dat je online kunt stemmen via DigiD",
  "createdAt": "2024-01-15T10:35:00"
}
```

**Test checklist:**
- ✅ Status code is 201
- ✅ Comment heeft een `id`
- ✅ Comment is gekoppeld aan parent question (intern)

### 6. Voeg nog meer comments toe aan dezelfde question

```bash
POST http://localhost:8080/api/forum/1/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Je kunt ook per post stemmen"
}
```

```bash
POST http://localhost:8080/api/forum/1/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Of op het stembureau"
}
```

**Test checklist:**
- ✅ Meerdere comments kunnen worden toegevoegd aan dezelfde question
- ✅ Elke comment krijgt een uniek ID
- ✅ Alle comments zijn gekoppeld aan question ID 1

### 7. Haal een question op met zijn comments (GET)

```bash
GET http://localhost:8080/api/forum/questions/1
Authorization: Bearer <jouw-token>
```

**Verwachte response (200 OK):**
```json
{
  "id": 1,
  "body": "Wat is de beste manier om te stemmen?",
  "createdAt": "2024-01-15T10:30:00",
  "comments": [
    {
      "id": 2,
      "body": "Ik denk dat je online kunt stemmen via DigiD",
      "createdAt": "2024-01-15T10:35:00"
    },
    {
      "id": 3,
      "body": "Je kunt ook per post stemmen",
      "createdAt": "2024-01-15T10:36:00"
    },
    {
      "id": 4,
      "body": "Of op het stembureau",
      "createdAt": "2024-01-15T10:37:00"
    }
  ]
}
```

**Test checklist:**
- ✅ Status code is 200
- ✅ Question heeft alle comments
- ✅ Comments zijn gesorteerd op `createdAt` (oudste eerst)
- ✅ Comments hebben geen eigen `comments` array (geen nested comments in deze response)

### 8. Haal alleen de comments op (GET)

```bash
GET http://localhost:8080/api/forum/1/questions
Authorization: Bearer <jouw-token>
```

**Verwachte response (200 OK):**
```json
[
  {
    "id": 2,
    "body": "Ik denk dat je online kunt stemmen via DigiD",
    "createdAt": "2024-01-15T10:35:00"
  },
  {
    "id": 3,
    "body": "Je kunt ook per post stemmen",
    "createdAt": "2024-01-15T10:36:00"
  },
  {
    "id": 4,
    "body": "Of op het stembureau",
    "createdAt": "2024-01-15T10:37:00"
  }
]
```

**Test checklist:**
- ✅ Status code is 200
- ✅ Alleen comments (geen top-level questions)
- ✅ Array wordt geretourneerd
- ✅ Gesorteerd op `createdAt` (ascending)

### 9. Test cascade delete

```bash
# Verwijder de parent question
DELETE http://localhost:8080/api/forum/questions/1
Authorization: Bearer <jouw-token>
```

**Verwachte response (204 No Content)**

**Test checklist:**
- ✅ Status code is 204
- ✅ Question is verwijderd
- ✅ Alle comments (2, 3, 4) zijn ook verwijderd (cascade)

Verifieer door te proberen de comments op te halen:
```bash
GET http://localhost:8080/api/forum/questions/2
Authorization: Bearer <jouw-token>
```
**Verwachte response: 404 Not Found**

### 10. Test dat een comment niet meerdere parents kan hebben

```bash
# Maak 2 nieuwe top-level questions
POST http://localhost:8080/api/forum/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Vraag 1"
}

POST http://localhost:8080/api/forum/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Vraag 2"
}

# Maak een comment op vraag 1
POST http://localhost:8080/api/forum/5/questions
Authorization: Bearer <jouw-token>
Content-Type: application/json

{
  "body": "Comment op vraag 1"
}
```

**Test checklist:**
- ✅ Comment heeft alleen parent question 5
- ✅ Comment kan niet tegelijkertijd parent question 6 hebben
- ✅ Dit wordt afgedwongen door de database relatie (ManyToOne)

## Database Verificatie

Je kunt ook direct in de database kijken:

```sql
-- Bekijk alle questions
SELECT id, body, question_id, user_id, created_at 
FROM forum_questions;

-- Top-level questions (geen parent)
SELECT * FROM forum_questions WHERE question_id IS NULL;

-- Comments (hebben een parent)
SELECT * FROM forum_questions WHERE question_id IS NOT NULL;

-- Alle comments van een specifieke question
SELECT * FROM forum_questions WHERE question_id = 1;
```

## Veelvoorkomende Problemen

### Probleem: Comments worden niet getoond
**Oplossing:** Check of de `comments` lijst correct wordt geladen. Gebruik `findByIdWithAuthor` of laad comments expliciet.

### Probleem: Cascade delete werkt niet
**Oplossing:** Check of `cascade = CascadeType.ALL` en `orphanRemoval = true` correct zijn ingesteld in het model.

### Probleem: Een comment kan meerdere parents hebben
**Oplossing:** Dit zou niet moeten kunnen. De `@ManyToOne` relatie zorgt ervoor dat een comment maar één parent kan hebben. Als dit wel gebeurt, check de database constraints.

## Test Scenario's Samenvatting

| Scenario | Endpoint | Methode | Verwachting |
|----------|----------|---------|-------------|
| Maak top-level question | `/api/forum/questions` | POST | 201, question zonder parent |
| Haal alle questions op | `/api/forum/questions` | GET | 200, array van top-level questions |
| Maak comment | `/api/forum/{id}/questions` | POST | 201, comment met parent |
| Haal question + comments op | `/api/forum/questions/{id}` | GET | 200, question met comments array |
| Haal alleen comments op | `/api/forum/{id}/questions` | GET | 200, array van comments |
| Verwijder question | `/api/forum/questions/{id}` | DELETE | 204, question + comments verwijderd |

## Next Steps

Na het testen kun je:
1. ✅ Verifiëren dat de relaties correct werken
2. ✅ Controleren dat cascade delete werkt
3. ✅ Testen of meerdere comments op één question mogelijk zijn
4. ✅ Verifiëren dat een comment niet meerdere parents kan hebben

