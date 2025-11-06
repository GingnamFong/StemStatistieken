<template>
  <div v-if="selectedMunicipality" class="charts-panel">
    <div class="chart-block">
      <h3 class="chart-title">{{ selectedMunicipality.name }}</h3>
      <PieChart :data="chartData" />
    </div>

    <div class="chart-block scrollable">
      <h3 class="chart-title">Stemverdeling</h3>
      <StaffDiagram :data="chartData" />
    </div>
  </div>

  <div v-else class="placeholder">
    <p>Selecteer een regio op de kaart</p>
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
  // Check if it's a municipality/province with direct allParties
  if (props.selectedMunicipality?.allParties) {
    return [...props.selectedMunicipality.allParties]
      .sort((a, b) => b.votes - a.votes)
      .map((p, i) => ({
        ...p,
        color: partyColors[i % partyColors.length],
      }))
  }

  // Check if it's a constituency (kieskring) with municipalities
  if (props.selectedMunicipality?.municipalities) {
    const map = new Map()

    // Aggregate parties from all municipalities in the constituency
    for (const m of props.selectedMunicipality.municipalities || []) {
      for (const p of m.allParties || []) {
        const cur = map.get(p.id) || { id: p.id, name: p.name, votes: 0 }
        cur.votes += p.votes || 0
        map.set(p.id, cur)
      }
    }

    const sorted = [...map.values()].sort((a, b) => b.votes - a.votes)
    return sorted.map((p, i) => ({
      ...p,
      color: partyColors[i % partyColors.length],
    }))
  }

  return []
})
</script>

<style scoped>
.charts-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 0;
  font-family: 'Nunito', sans-serif;
}

.chart-block {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.2s ease;
}

.chart-block:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #cbd5e1;
}

.chart-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  text-align: center;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}

/* De staafdiagram kan veel labels bevatten → horizontaal scrollen */
.scrollable {
  overflow-x: auto;
  max-height: 500px;
}

/* Placeholder voor geen selectie */
.placeholder {
  color: #64748b;
  font-size: 16px;
  text-align: center;
  margin-top: 3rem;
  font-weight: 500;
}

/* Scrollbar styling voor de charts-panel */
.charts-panel::-webkit-scrollbar {
  width: 8px;
}

.charts-panel::-webkit-scrollbar-track {
  background: #f8fafc;
  border-radius: 10px;
}

.charts-panel::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.charts-panel::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Responsive */
@media (max-width: 768px) {
  .chart-block {
    padding: 16px;
  }

  .chart-title {
    font-size: 16px;
  }
}
</style>
