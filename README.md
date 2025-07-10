# PHANTOM: Your Local AI Assistant

**PHANTOM** is a fully local AI assistant inspired by Tony Stark's J.A.R.V.I.S.  
It supports **voice and text interaction**, maintains **context-aware conversation**, and persists **memory across sessions** using PostgreSQL with native `pgvector`.

---

## 🚀 Features
✅ Run completely offline (local LLM + Postgres)  
✅ Voice + text input (text ready; voice I/O planned)  
✅ Memory that persists across restarts  
✅ Semantic recall using Postgres vector search  
✅ Web-based UI with WebSocket chat

---

## ⚙️ Tech Stack

| Layer              | Technology |
|--------------------|------------|
| Backend             | Java 21, Spring Boot 3.5, Spring AI, Spring WebFlux |
| Database            | PostgreSQL + `pgvector` extension |
| AI Model            | Local Ollama (LLaMA3 / Mistral / other) |
| Embeddings          | Ollama embeddings API |
| Frontend            | WebSocket chat UI (React / plain HTML planned) |

---

## 📝 Prerequisites

✅ **Java 21**  
✅ **PostgreSQL 15+** with `pgvector` extension  
✅ **Ollama** installed and running (`ollama serve`)  
✅ Maven 3.9+

---

## 🛠 Setup

### 1️⃣ Prepare Database

```bash
psql -U youruser -d phantomdb -c "CREATE EXTENSION IF NOT EXISTS vector;"

psql -U youruser -d phantomdb <<EOF
CREATE TABLE memory (
  id SERIAL PRIMARY KEY,
  key TEXT UNIQUE,
  value TEXT,
  embedding VECTOR(1536)
);
EOF
```

Update `application.yml` with your Postgres connection info.

---

### 2️⃣ Run Ollama

```bash
ollama serve
ollama pull llama3
```

Optional: pull embedding model if using a dedicated one.

---

### 3️⃣ Build & Run Phantom

```bash
mvn spring-boot:run
```

Access your app at `http://localhost:8080`

---

## 💬 Usage

- Send messages via WebSocket `/chat`
- Say `remember: <text>` → saves semantic memory
- Say `recall: <query>` → retrieves related memories

---

## 🔮 Next Steps

- [ ] Add voice I/O endpoints (speech-to-text, text-to-speech)
- [ ] Build web-based chat UI
- [ ] Add configurable prompt templates

---

## 📂 Project Structure

```
phantom/
├── src/main/java/com/gliesestudio/phantom/
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── model/
│   └── PhantomApplication.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── README.md
```

---

## 🤖 Example WebSocket Messages

**Request**
```json
{
  "text": "remember: I like Batman movies"
}
```

**Response**
```json
{
  "reply": "Got it. I'll remember that."
}
```

---

## 📝 License

MIT — free to modify and use!
