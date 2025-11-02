<template>
  <div v-if="selectedMunicipality">
    <h2>{{ selectedMunicipality.name }}</h2>
    <PieChart :data="chartData" />
    <StaffDiagram :data="chartData" />
  </div>
  <div v-else class="placeholder">
    <p>Select a municipality on the map</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PieChart from './PieChart.vue'
import StaffDiagram from './StaffDiagram.vue'

const props = defineProps({
  selectedMunicipality: Object,
})

const chartData = computed(() => {
  if (!props.selectedMunicipality) return []
  return [...props.selectedMunicipality.allParties]
    .sort((a, b) => b.votes - a.votes)
    .slice(0, 5)
})
</script>

<style scoped>
.placeholder {
  color: #888;
  font-style: italic;
  text-align: center;
  margin-top: 2rem;
}
</style>
