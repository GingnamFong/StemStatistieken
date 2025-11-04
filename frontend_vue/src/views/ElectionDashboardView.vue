<template>
  <div class="dashboard">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1>Verkiezingen 2023 Nederland</h1>
        <p class="subtitle">Interactive results of the 2023 Tweede Kamer election</p>
      </div>
    </header>

    <div class="dashboard-content">
      <!-- Left: map section -->
      <section class="map-section card">
        <div class="map-header">
          <div class="map-header-left">
            <h2>{{ currentTitle }}</h2>
            <p class="map-subtitle">Click a region to view detailed results</p>
          </div>
          <!-- Dropdown (active but only municipality map exists yet) -->
          <div class="map-header-right">
            <label for="dataType" class="dropdown-label">View:</label>
            <select
              id="dataType"
              v-model="selectedType"
              class="data-dropdown"
              @change="handleTypeChange"
            >
              <option value="municipality">Municipalities</option>
              <option value="province">Provinces</option>
            </select>
          </div>
        </div>

        <!-- For now, just always show the municipality map -->
        <!-- Dynamic Map -->
        <div class="map-container">
          <component
            :is="currentMapComponent"
            :showDataSection="false"
            @municipalitySelected="handleMunicipalitySelected"
            @provincieDataForChart="handleProvincieSelected"
          />
        </div>

        <!-- Optional message for missing maps -->
        <p v-if="missingMapMessage" class="coming-soon">{{ missingMapMessage }}</p>
      </section>

      <!-- Right: charts -->
      <section class="charts-section card">
        <ChartsPanel :selectedMunicipality="selectedRegion" />
      </section>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import DutchMapGemeente2024 from '@/components/DutchMapGemeente2024.vue'
import DutchMapProvincie from '@/components/DutchMapProvincie.vue'
import ChartsPanel from '@/components/ChartsPanel.vue'

/* --- Reactive state --- */
const selectedType = ref('municipality') // Dropdown selection
const selectedRegion = ref(null) // Any clicked region (municipality or province)

/* --- Handlers --- */
function handleMunicipalitySelected(m) {
  selectedRegion.value = m
}

function handleProvincieSelected(p) {
  selectedRegion.value = p
}

function handleTypeChange() {
  // Reset selected region when switching map types
  selectedRegion.value = null
}

/* --- Computed values --- */
const currentTitle = computed(() => {
  switch (selectedType.value) {
    case 'province':
      return 'Province Map'
    default:
      return 'Municipality Map'
  }
})

/* --- Dynamic map loading --- */
const currentMapComponent = computed(() => {
  switch (selectedType.value) {
    case 'municipality':
      return DutchMapGemeente2024
    case 'province':
      return DutchMapProvincie
    default:
      return DutchMapGemeente2024
  }
})

/* --- Info message for missing maps --- */
const missingMapMessage = computed(() => {
  return null
})
</script>

<style scoped>
/* --- Overall Layout --- */
.dashboard {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(180deg, #f7f8fa 0%, #eef1f5 100%);
  overflow: hidden;
  font-family: 'Nunito', sans-serif;
}

/* --- Header --- */
.dashboard-header {
  background: #ffffff;
  border-bottom: 1px solid #ddd;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  z-index: 10;
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

/* --- Dropdown in header or map section --- */
.header-right,
.map-header-right {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.dropdown-label {
  font-size: 0.95rem;
  color: #444;
  font-weight: 500;
}

.data-dropdown {
  padding: 6px 12px;
  font-size: 0.95rem;
  border-radius: 6px;
  border: 1px solid #ccc;
  background-color: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.data-dropdown:hover {
  border-color: #3f4383;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-dropdown:focus {
  outline: none;
  border-color: #3f4383;
  box-shadow: 0 0 0 2px rgba(63, 67, 131, 0.2);
}

/* --- Main Dashboard Area --- */
.dashboard-content {
  display: flex;
  flex: 1;
  padding: 1.5rem 2rem;
  gap: 2rem;
  overflow: hidden;
}

/* --- Card Container --- */
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  flex: 1;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}

/* --- Map Section --- */
.map-section {
  flex: 1.3;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
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

.map-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.map-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* --- Charts Section --- */
.charts-section {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-y: auto;
  max-height: 100%;
}

/* --- Scrollbar Styling --- */
.charts-section::-webkit-scrollbar {
  width: 8px;
}
.charts-section::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 10px;
}
.charts-section::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.25);
}

/* --- Responsive Design --- */
@media (max-width: 1024px) {
  .dashboard-content {
    flex-direction: column;
    padding: 1rem;
  }

  .map-section,
  .charts-section {
    flex: unset;
    width: 100%;
  }

  .map-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .data-dropdown {
    font-size: 0.9rem;
  }
}
</style>

