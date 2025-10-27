<template>
  <div class="map-toggle">
    <h2>Interactieve Nederlandse kaart</h2>
    <p>Kies je weergave en klik op een gebied om verkiezingsgegevens te bekijken</p>

    <div class="toggle-buttons">
      <button
        :class="['toggle-btn', { active: selectedView === 'provincies' }]"
        @click="selectView('provincies')"
      >
        Provincies
      </button>
      <button
        :class="['toggle-btn', { active: selectedView === 'kieskringen' }]"
        @click="selectView('kieskringen')"
      >
        Kieskringen
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'

const selectedView = ref('kieskringen') // Default to kieskringen
const emit = defineEmits(['view-changed'])

const selectView = (view) => {
  selectedView.value = view
  emit('view-changed', view)
}
</script>

<style scoped>
.map-toggle {
  text-align: center;
  margin: 2rem 0;
  padding: 1rem;
}

.map-toggle h2 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.map-toggle p {
  color: #666;
  margin-bottom: 1.5rem;
}

.toggle-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.toggle-btn {
  padding: 12px 24px;
  border: 2px solid #ddd;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 140px;
}

.toggle-btn:hover {
  border-color: #4CAF50;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.toggle-btn.active {
  background: #4CAF50;
  color: white;
  border-color: #4CAF50;
}

@media (max-width: 768px) {
  .toggle-buttons {
    flex-direction: column;
    align-items: center;
  }

  .toggle-btn {
    width: 200px;
  }
}
</style>
