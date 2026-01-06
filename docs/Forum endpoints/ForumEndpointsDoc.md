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

