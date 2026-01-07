<template>
  <div class="post-card">
    <!-- Vote Section -->
    <div class="post-votes">
      <button
        @click="$emit('vote', post.id, 'up')"
        :class="['vote-btn', 'upvote', { active: post.userVote === 'up' }]"
        aria-label="Upvote"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M7 14l5-5 5 5z" />
        </svg>
      </button>
      <button
        @click="$emit('vote', post.id, 'down')"
        :class="['vote-btn', 'downvote', { active: post.userVote === 'down' }]"
        aria-label="Downvote"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M17 10l-5 5-5-5z" />
        </svg>
      </button>
    </div>

    <!-- Post Content -->
    <div class="post-content">
      <div class="post-header">
        <span class="post-author">u/{{ post.author }}</span>
        <span class="post-time">{{ formatTime(post.createdAt) }}</span>
      </div>

      <h3 class="post-title" @click="$emit('open', post.id)">
        {{ post.title }}
      </h3>

      <p v-if="post.content" class="post-text">{{ post.content }}</p>

      <div class="post-footer">
        <button class="post-action">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8" />
            <polyline points="16 6 12 2 8 6" />
            <line x1="12" y1="2" x2="12" y2="15" />
          </svg>
          Delen
        </button>

        <button class="post-action">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
          </svg>
          Opslaan
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { formatTime } from '@/utils/time.js'

// eslint-disable-next-line no-unused-vars
const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})
defineEmits(['vote', 'open']);
</script>

<style scoped>
.post-card {
  display: flex;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.2s;
}

.post-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* Votes */
.post-votes {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  background: #f8fafc;
  min-width: 40px;
}

.vote-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.vote-btn:hover {
  background: #e2e8f0;
}

.vote-btn svg {
  width: 20px;
  height: 20px;
}

.vote-btn.upvote.active {
  color: #ef4444;
}

.vote-btn.downvote.active {
  color: #3b82f6;
}

/* Post content */
.post-content {
  flex: 1;
  padding: 12px 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.post-author {
  font-weight: 600;
  color: #3b82f6;
}

.post-time {
  color: #94a3b8;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  line-height: 1.4;
  cursor: pointer;
}

.post-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.post-footer {
  display: flex;
  gap: 16px;
}

.post-action {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}

.post-action:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.post-action svg {
  width: 16px;
  height: 16px;
}
</style>


