<template>
  <div v-if="selectedMunicipality" class="charts-panel">
    <h2 class="region-title">{{ selectedMunicipality.name }}</h2>

    <div class="chart-block">
      <PieChart :data="chartData" />
    </div>

    <div class="chart-block scrollable">
      <StaffDiagram :data="chartData" />
    </div>
  </div>

  <div v-else class="placeholder">
    <p>Select a municipality on the map</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PieChart from './PieChart.vue'
import StaffDiagram from './StaffDiagram.vue'
import { partyColors } from '@/utils/chartColors.js'

const props = defineProps({
  selectedMunicipality: Object,
})


const chartData = computed(() => {
  if (!props.selectedMunicipality?.allParties) return []
  return [...props.selectedMunicipality.allParties]
    .sort((a, b) => b.votes - a.votes)
    .map((p, i) => ({
      ...p,
      color: partyColors[i % partyColors.length], // matchende kleuren
    }))
})
</script>

<style scoped>
.charts-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.2rem;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 1rem;
}

.region-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.chart-block {
  background: #fafafa;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 95%;
  max-height: 420px;
}

/* De staafdiagram kan veel labels bevatten → horizontaal scrollen */
.scrollable {
  overflow-x: auto;
  max-height: 360px;
}

/* Placeholder voor geen selectie */
.placeholder {
  color: #888;
  font-style: italic;
  text-align: center;
  margin-top: 2rem;
}
</style>
