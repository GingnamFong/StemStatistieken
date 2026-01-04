<template>
  <div class="create-post-card">
    <!-- Avatar -->
    <div class="create-post-avatar">
      <slot>
        <!-- Default avatar SVG -->
        <svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
        </svg>
      </slot>
    </div>

    <!-- Input -->
    <input
      type="text"
      :value="title"
      @input="$emit('update:title', $event.target.value)"
      @click="$emit('open')"
      @keyup.enter="$emit('open')"
      placeholder="Maak een post..."
      class="create-post-input"
    />

    <!-- Button -->
    <button v-if="!open" @click="$emit('open')" class="create-post-btn">
      <slot>
        <!-- Default post icon SVG -->
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14,2 14,8 20,8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10,9 9,9 8,9" />
        </svg>
      </slot>
    </button>
  </div>
</template>


<script setup>
defineProps({
  title: String, // read-only prop
  open: Boolean
})

defineEmits(['update:title', 'open'])
</script>


<style scoped>
.create-post-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.create-post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.create-post-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  background: #f8fafc;
  transition: all 0.2s;
}

.create-post-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
}

.create-post-btn {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-post-btn:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
  color: #3b82f6;
}

</style>
