<template>
  <div class="dashboard">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1>🇳🇱 Dutch Election Dashboard</h1>
        <p class="subtitle">Interactive results of the 2023 Tweede Kamer election</p>
      </div>
    </header>

    <div class="dashboard-content">
      <!-- Left: map -->
      <section class="map-section card">
        <h2>Municipality Map</h2>
        <p class="map-subtitle">Click a municipality to view detailed results</p>
        <DutchMapGemeente2024 @municipalitySelected="handleMunicipalitySelected" />
      </section>

      <!-- Right: charts -->
      <section class="charts-section card">
        <ChartsPanel :selectedMunicipality="selectedMunicipality" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DutchMapGemeente2024 from '@/components/DutchMapGemeente2024.vue'
import ChartsPanel from '@/components/ChartsPanel.vue'

const selectedMunicipality = ref(null)
function handleMunicipalitySelected(m) {
  selectedMunicipality.value = m
}
</script>

<style scoped>
/* --- Layout --- */
.dashboard {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f7f8fa 0%, #eef1f5 100%);
  overflow: hidden;
}

/* --- Header --- */
.dashboard-header {
  background: #ffffff;
  border-bottom: 1px solid #ddd;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.header-left h1 {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  color: #1a1a1a;
}

.subtitle {
  color: #666;
  font-size: 0.95rem;
  margin-top: 0.25rem;
}

/* --- Main content --- */
.dashboard-content {
  display: flex;
  flex: 1;
  padding: 1.5rem 2rem;
  gap: 2rem;
  overflow: hidden;
}

/* --- Card container --- */
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 3px 8px rgba(0,0,0,0.08);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* --- Map section --- */
.map-section {
  flex: 1.5;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.map-section h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.map-subtitle {
  color: #777;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

/* --- Charts section --- */
.charts-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-y: auto;
}

/* Scroll bar subtle styling */
.charts-section::-webkit-scrollbar {
  width: 8px;
}
.charts-section::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
}
</style>
