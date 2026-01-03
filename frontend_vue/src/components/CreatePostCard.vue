<template>
  <div class="create-post-card">
    <div class="create-post-avatar">
      <slot name="avatar" />
    </div>

    <!-- Correctly bind value without mutating prop -->
    <input
      type="text"
      :value="title"
      @input="$emit('update:title', $event.target.value)"
      @click="$emit('open')"
      @keyup.enter="$emit('open')"
      placeholder="Maak een post..."
      class="create-post-input"
    />

    <button v-if="!open" @click="$emit('open')" class="create-post-btn">
      <slot name="icon" />
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
