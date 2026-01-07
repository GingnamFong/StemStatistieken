## Endpoints documentation for forum

### Endpoints 

| Method | Endpoint                  | Description                      | Auth Required | Status Codes       |
| ------ | ------------------------- | -------------------------------- | ------------- | ------------------ |
| GET    | `/questions`              | Get all top-level questions      | No            | 200, 500           |
| POST   | `/questions`              | Create a new top-level question  | Yes           | 201, 401           |
| GET    | `/questions/{questionId}` | Get a question with its comments | No            | 200, 404           |
| DELETE | `/questions/{questionId}` | Delete a question                | Yes           | 204, 401, 403, 404 |
| GET    | `/{questionId}/questions` | Get all comments for a question  | No            | 200, 404           |
| POST   | `/{questionId}/questions` | Add a comment to a question      | Yes           | 201, 401, 404      |

## Some json examples 
### GET /api/forum/questions 
#### Response 200 OK
```json
[
  {
    "id": 1,
    "body": "How do I setup Spring Security?",
    "author": {
      "id": 10,
      "name": "Alice"
    },
    "createdAt": "2026-01-06T12:00:00Z",
    "comments": []
  },
  {
    "id": 2,
    "body": "Best practices for REST APIs?",
    "author": {
      "id": 11,
      "name": "Bob"
    },
    "createdAt": "2026-01-06T13:00:00Z",
    "comments": []
  }
]
```
<br>

### POST /api/forum/questions
#### Request Body
```json
{
  "body": "How do I use Spring Boot with JPA?"
}
```
#### Response 201 created
```json
{
  "status": 201,
  "message": "Question created successfully",
  "data": {
    "id": 3,
    "body": "How do I use Spring Boot with JPA?",
    "author": {
      "id": 12,
      "name": "Charlie"
    },
    "createdAt": "2026-01-06T14:00:00Z",
    "comments": []
  }
}
```
<br>

### GET /api/forum/questions/{questionId}
#### Response (200 OK)
```json
{
  "id": 1,
  "body": "How do I setup Spring Security?",
  "author": {
    "id": 10,
    "name": "Alice"
  },
  "createdAt": "2026-01-06T12:00:00Z",
  "comments": [
    {
      "id": 101,
      "body": "You should start with the official docs",
      "author": {
        "id": 11,
        "name": "Bob"
      },
      "createdAt": "2026-01-06T12:30:00Z",
      "comments": []
    }
  ]
}
```
<br>

### GET /api/forum/{questionId}/questions
#### Response (200 OK)
```json
[
  {
    "id": 101,
    "body": "You should start with the official docs",
    "author": {
      "id": 11,
      "name": "Bob"
    },
    "createdAt": "2026-01-06T12:30:00Z",
    "comments": []
  },
  {
    "id": 102,
    "body": "Also check online tutorials.",
    "author": {
      "id": 12,
      "name": "Charlie"
    },
    "createdAt": "2026-01-06T14:30:00Z",
    "comments": []
  }
]
```
<br>

### POST /api/forum/{questionId}/questions
#### Request Body
```json
{
  "body": "You can also check tutorials online."
}
```
#### Response 201
```json
{
  "status": 201,
  "message": "Reply added successfully",
  "data": {
    "id": 102,
    "body": "You can also check tutorials online.",
    "author": {
      "id": 12,
      "name": "Charlie"
    },
    "createdAt": "2026-01-06T14:30:00Z",
    "comments": []
  }
}
```
<br>

### DELETE /api/forum/questions/{questionId}
#### Successful Response
```http
204 No Content
```
<br>

### Error Example (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "You must be logged in to create a question"
}
```


