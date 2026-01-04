<template>
  <div class="post-form-modal" @click.self="$emit('close')">
    <div class="post-form-card">
      <div class="post-form-header">
        <h3>Maak een post</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="post-form-body">
        <input
          class="post-form-title"
          type="text"
          placeholder="Titel..."
          :value="title"
          @input="$emit('update:title', $event.target.value)"
        />

        <textarea
          class="post-form-content"
          rows="6"
          placeholder="Tekst"
          :value="content"
          @input="$emit('update:content', $event.target.value)"
        />

        <div class="post-form-actions">
          <button class="cancel-btn" @click="$emit('close')">
            Annuleren
          </button>

          <button
            class="submit-btn"
            :disabled="loading"
            @click="$emit('submit')"
          >
            {{ loading ? 'Opslaan...' : 'Posten' }}
          </button>
        </div>

        <p v-if="error" class="error-text">
          {{ error }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  content: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

defineEmits([
  'update:title',
  'update:content',
  'submit',
  'close'
])
</script>

<style scoped>
.post-form-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.post-form-card {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.post-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.post-form-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #64748b;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.post-form-body {
  padding: 24px;
}

.post-form-title {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  font-family: 'Nunito', sans-serif;
  margin-bottom: 16px;
  transition: all 0.2s;
}

.post-form-title:focus {
  outline: none;
  border-color: #3b82f6;
}

.post-form-content {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  resize: vertical;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.post-form-content:focus {
  outline: none;
  border-color: #3b82f6;
}

.post-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.submit-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
  border: none;
}

.cancel-btn {
  background: #f1f5f9;
  color: #64748b;
}

.cancel-btn:hover {
  background: #e2e8f0;
}

.submit-btn {
  background: #3b82f6;
  color: white;
}

.submit-btn:hover {
  background: #2563eb;
}

.error-text {
  margin-top: 8px;
  font-size: 14px;
  color: #dc2626;
}

@media (max-width: 768px) {
  .post-form-modal {
    padding: 10px;
  }

  .post-form-card {
    max-height: 95vh;
  }
}

</style>

