<template>
  <div class="dashboard">
    <!-- Left: map -->
    <DutchMapGemeente2024
      class="map-section"
      @municipalitySelected="handleMunicipalitySelected"
    />

    <!-- Right: charts -->
    <ChartsPanel
      class="charts-section"
      :selectedMunicipality="selectedMunicipality"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DutchMapGemeente2024 from '@/components/DutchMapGemeente2024.vue'
import ChartsPanel from '@/components/ChartsPanel.vue'

const selectedMunicipality = ref(null)

function handleMunicipalitySelected(municipality) {
  selectedMunicipality.value = municipality
}
</script>

<style scoped>
.dashboard {
  display: flex;
  height: 100vh;
  background: #f9f9f9;
}

/* left side */
.map-section {
  flex: 2;
  border-right: 1px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* right side */
.charts-section {
  flex: 1;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-y: auto;
  background-color: #fff;
}
</style>
