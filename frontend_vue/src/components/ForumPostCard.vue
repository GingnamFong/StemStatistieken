<template>
  <div class="post-card">
    <!-- Vote Section -->
    <div class="post-votes">
      <button
        @click="$emit('vote', post.id, 'up')"
        :class="['vote-btn upvote', { active: post.userVote === 'up' }]"
        aria-label="Upvote"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M7 14l5-5 5 5z" />
        </svg>
      </button>

      <button
        @click="$emit('vote', post.id, 'down')"
        :class="['vote-btn downvote', { active: post.userVote === 'down' }]"
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

      <h3
        class="post-title"
        @click="$router.push(`/forum/questions/${post.id}`)"
      >
        {{ post.title }}
      </h3>

      <p v-if="post.content" class="post-text">
        {{ post.content }}
      </p>

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
import { formatTime } from '@/utils/time.js'

defineProps({
  post: {
    type: Object,
    required: true
  }
})

defineEmits(['vote'])
</script>


