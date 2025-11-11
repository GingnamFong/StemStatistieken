<template>
  <div class="add-column-card">
    <button @click="$emit('add')" class="add-column-btn">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 4v16m8-8H4" />
      </svg>
      <span>Kolom Toevoegen</span>
    </button>
  </div>
</template>

<script setup>
defineEmits(['add'])
</script>

<style scoped>
.add-column-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 250px;
}

.add-column-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  background: white;
  border: 2px dashed #d1d5db;
  border-radius: 16px;
  color: #6b7280;
  font-size: 15px;
  font-weight: 600;
  font-family: 'Nunito', sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  height: 100%;
}

.add-column-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #f0f9ff;
}

.add-column-btn svg {
  width: 32px;
  height: 32px;
}
</style>

