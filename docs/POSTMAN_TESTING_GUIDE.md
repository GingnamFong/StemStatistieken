# Postman Testing Guide - Forum Posts

Deze gids legt uit hoe je met Postman kunt testen of forum posts correct naar de database worden opgeslagen.

## Stap 1: Backend starten

Zorg dat je backend draait op `http://localhost:8081`

## Stap 2: Inloggen om een token te krijgen

### Request 1: Login
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/auth/login`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body** (raw JSON):
  ```json
  {
    "email": "jouw@email.com",
    "password": "jouwWachtwoord123"
  }
  ```

### Response:
```json
{
  "message": "Login successful",
  "userId": 1,
  "token": "user-1-1234567890",
  "email": "jouw@email.com"
}
```

**Belangrijk**: Kopieer de `token` waarde! Je hebt deze nodig voor de volgende stappen.

---

## Stap 3: Een forum post maken

### Request 2: Create Forum Post
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/forum`
- **Headers**:
  ```
  Content-Type: application/json
  Authorization: Bearer user-1-1234567890
  ```
  *(Vervang `user-1-1234567890` met je echte token uit stap 2)*

- **Body** (raw JSON):
  ```json
  {
    "question": "Wat is jouw mening over de verkiezingen?",
    "answer": "Ik vind de verkiezingen belangrijk voor onze democratie."
  }
  ```

### Succesvolle Response (201 Created):
```json
{
  "id": 1,
  "question": "Wat is jouw mening over de verkiezingen?",
  "answer": "Ik vind de verkiezingen belangrijk voor onze democratie.",
  "createdAt": "2024-01-15T10:30:00",
  "author": {
    "id": 1,
    "name": "Jouw Naam",
    "email": "jouw@email.com"
  }
}
```

### Foutmeldingen:
- **401 Unauthorized**: Token ontbreekt of is ongeldig
- **400 Bad Request**: Validatiefout (bijv. lege question of answer)

---

## Stap 4: Controleren of de post in de database staat

### Optie A: Via GET request (aanbevolen)

#### Request 3: Get All Posts
- **Method**: `GET`
- **URL**: `http://localhost:8081/api/forum`
- **Headers**: Geen nodig (publiek endpoint)

### Response:
```json
[
  {
    "id": 1,
    "question": "Wat is jouw mening over de verkiezingen?",
    "answer": "Ik vind de verkiezingen belangrijk voor onze democratie.",
    "createdAt": "2024-01-15T10:30:00",
    "author": {
      "id": 1,
      "name": "Jouw Naam",
      "email": "jouw@email.com"
    }
  }
]
```

Als je post hierin staat, is hij succesvol opgeslagen! ✅

---

### Optie B: Via H2 Database Console (directe database inspectie)

1. Open je browser en ga naar: `http://localhost:8081/h2-console`

2. **JDBC URL**: `jdbc:h2:file:./election-database`
   - *(Let op: dit is een relatief pad, dus het bestand staat in de root van je XMLParser project)*

3. **Username**: `sa`
4. **Password**: *(leeg laten)*
5. Klik op **Connect**

6. Voer deze SQL query uit:
   ```sql
   SELECT * FROM forum_posts;
   ```

7. Je zou je post moeten zien met:
   - `ID`: Het ID van de post
   - `QUESTION`: De vraag/titel
   - `ANSWER`: De inhoud
   - `CREATED_AT`: De datum/tijd
   - `USER_ID`: Het ID van de gebruiker die de post heeft gemaakt

8. Om ook de gebruiker informatie te zien:
   ```sql
   SELECT 
     fp.id,
     fp.question,
     fp.answer,
     fp.created_at,
     u.name as author_name,
     u.email as author_email
   FROM forum_posts fp
   JOIN users u ON fp.user_id = u.id;
   ```

---

## Stap 5: Testen zonder authenticatie (moet falen)

### Request 4: Create Post Zonder Token
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/forum`
- **Headers**:
  ```
  Content-Type: application/json
  ```
  *(Geen Authorization header!)*

- **Body** (raw JSON):
  ```json
  {
    "question": "Test zonder token",
    "answer": "Dit zou moeten falen"
  }
  ```

### Verwachte Response:
- **Status**: `401 Unauthorized`
- **Body**: Leeg

Dit bevestigt dat authenticatie werkt! ✅

---

## Postman Collection Setup

### Environment Variables (aanbevolen)

Maak een Postman Environment aan met:
- `base_url`: `http://localhost:8081`
- `token`: *(wordt automatisch gezet na login)*

### Pre-request Script voor Login (optioneel)

Je kunt een pre-request script toevoegen aan je "Create Post" request:

```javascript
// Haal token op uit environment (als die er is)
const token = pm.environment.get("token");

if (!token) {
    // Login eerst
    pm.sendRequest({
        url: pm.environment.get("base_url") + "/api/auth/login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: "jouw@email.com",
                password: "jouwWachtwoord123"
            })
        }
    }, function (err, res) {
        if (res.json().token) {
            pm.environment.set("token", res.json().token);
        }
    });
}
```

---

## Troubleshooting

### Probleem: 401 Unauthorized bij POST
**Oplossing**: 
- Controleer of je de Authorization header hebt toegevoegd
- Controleer of het token formaat correct is: `Bearer user-1-1234567890`
- Zorg dat je eerst ingelogd bent en een geldig token hebt

### Probleem: Post wordt niet getoond in GET request
**Oplossing**:
- Controleer de database direct via H2 console
- Controleer of de backend logs geen errors tonen
- Controleer of de post daadwerkelijk is opgeslagen (status 201)

### Probleem: Database is leeg
**Oplossing**:
- Controleer of de H2 database file bestaat: `./election-database.mv.db` in je XMLParser directory
- Controleer de `application.properties` voor de database configuratie
- Zorg dat `spring.jpa.hibernate.ddl-auto=update` is ingesteld

### Probleem: Token werkt niet
**Oplossing**:
- Tokens zijn simpel en bevatten alleen de user ID
- Zorg dat de user bestaat in de database
- Probeer opnieuw in te loggen om een nieuwe token te krijgen

---

## Volledige Test Flow

1. ✅ **Login** → Krijg token
2. ✅ **Create Post** → Maak een nieuwe post met token
3. ✅ **Get All Posts** → Controleer of post zichtbaar is
4. ✅ **H2 Console** → Controleer database direct
5. ✅ **Test zonder token** → Verifieer dat authenticatie werkt

Als alle stappen slagen, werkt je forum post functionaliteit correct! 🎉


