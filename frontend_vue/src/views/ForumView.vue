<script setup>
import { ref, onMounted } from 'vue'

const question = ref('')
const answer = ref('')
const posts = ref([])
const loading = ref(false)
const error = ref('')

const API_BASE = 'http://localhost:8081/api/forum'

async function loadPosts() {
  try {
    const res = await fetch(API_BASE)
    posts.value = await res.json()
  } catch (e) {
    console.error(e)
  }
}

async function submitForm() {
  error.value = ''
  if (!question.value.trim() || !answer.value.trim()) {
    error.value = 'Vraag en antwoord zijn verplicht.'
    return
  }
  loading.value = true
  try {
    const res = await fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question: question.value, answer: answer.value })
    })
    if (!res.ok) {
      throw new Error('Fout bij opslaan in de server')
    }
    question.value = ''
    answer.value = ''
    await loadPosts()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<template>
  <div class="forum-page">
    <h1>Forum forum</h1>

    <form class="forum-form" @submit.prevent="submitForm">
      <label>
        Vraag
        <input v-model="question" type="text" placeholder="Je vraag" />
      </label>

      <label>
        Antwoord
        <textarea v-model="answer" rows="4" placeholder="Je antwoord"></textarea>
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Opslaan...' : 'Plaats bericht' }}
      </button>
    </form>

    <section class="forum-list" v-if="posts.length">
      <h2>Recente berichten</h2>
      <article v-for="post in posts" :key="post.id" class="forum-item">
        <h3>{{ post.question }}</h3>
        <p>{{ post.answer }}</p>
        <small>{{ post.createdAt }}</small>
      </article>
    </section>
  </div>
</template>

<style scoped>
.forum-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Nunito', sans-serif;
}

h1 {
  font-size: 32px;
  margin-bottom: 16px;
}

.forum-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-weight: 600;
}

input,
textarea {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  font-family: inherit;
}

button {
  align-self: flex-start;
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  background: #3b82f6;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: default;
}

.error {
  color: #dc2626;
  font-size: 14px;
}

.forum-list {
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}

.forum-item {
  padding: 16px 0;
  border-bottom: 1px solid #e2e8f0;
}

.forum-item h3 {
  margin: 0 0 4px;
}

.forum-item p {
  margin: 0 0 4px;
}

.forum-item small {
  color: #6b7280;
}
</style>
package hva.nlelections.backend_springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_posts")
public class ForumPost {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@NotBlank
private String question;

@NotBlank
private String answer;

private LocalDateTime createdAt = LocalDateTime.now();

public Long getId() {
return id;
}

public void setId(Long id) {
this.id = id;
}

public String getQuestion() {
return question;
}

public void setQuestion(String question) {
this.question = question;
}

public String getAnswer() {
return answer;
}

public void setAnswer(String answer) {
this.answer = answer;
}

public LocalDateTime getCreatedAt() {
return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
this.createdAt = createdAt;
}
}

