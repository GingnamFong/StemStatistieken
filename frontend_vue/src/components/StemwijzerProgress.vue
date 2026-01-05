<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  }
})

const progress = computed(() => ((props.current + 1) / props.total) * 100)
</script>

<template>
  <div class="progress-bar-container">
    <div class="progress-bar" :style="{ width: `${progress}%` }"></div>
    <span class="progress-text">Vraag {{ current + 1 }} van {{ total }}</span>
  </div>
</template>

<style scoped>
.progress-bar-container {
  position: relative;
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  margin-bottom: 16px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 10px;
  right: 0;
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}
</style>
