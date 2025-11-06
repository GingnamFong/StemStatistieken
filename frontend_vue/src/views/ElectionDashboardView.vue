<template>
  <div class="dashboard">
    <!-- Improved Header with breadcrumb -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Verkiezingsuitslagen</span>
        </div>
        <div class="header-info">
          <div class="election-badge">
            <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
            <span>Tweede Kamer 2023</span>
          </div>
          <h1 class="page-title">Verkiezingsuitslagen</h1>
          <p class="page-description">Verken interactieve kaarten en gedetailleerde statistieken</p>
        </div>
      </div>
    </header>

    <div class="dashboard-container">
      <!-- View Selector -->
      <div class="view-selector">
        <button
          v-for="type in viewTypes"
          :key="type.value"
          @click="selectViewType(type.value)"
          :class="['view-button', { active: selectedType === type.value }]"
        >
          <svg class="view-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path :d="type.icon" />
          </svg>
          <span>{{ type.label }}</span>
        </button>
      </div>

      <!-- Main Content Grid -->
      <div class="content-grid">
        <!-- Map Section -->
        <section class="map-card">
          <div class="card-header">
            <div class="card-title-group">
              <h2 class="card-title">{{ currentTitle }}</h2>
            </div>
          </div>

          <div class="card-body">
            <div class="map-wrapper">
              <component
                :is="currentMapComponent"
                :showDataSection="false"
                @municipalitySelected="handleMunicipalitySelected"
                @provincieDataForChart="handleProvincieSelected"
                @regionSelected="handleKieskringSelected"
              />
            </div>
          </div>
        </section>

        <!-- Charts Section -->
        <section class="charts-card">
          <div class="card-header">
            <div class="card-title-group">
              <h2 class="card-title">Resultaten</h2>
            </div>
          </div>

          <div class="card-body charts-body">
            <div v-if="!selectedRegion" class="empty-state">
              <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <h3 class="empty-title">Geen regio geselecteerd</h3>
              <p class="empty-description">Selecteer een regio op de kaart om de resultaten te zien</p>
            </div>

            <div v-else class="charts-content">
              <ChartsPanel :selectedMunicipality="selectedRegion" />
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import DutchMapGemeente2024 from '@/components/DutchMapGemeente2024.vue'
import DutchMapProvincie from '@/components/DutchMapProvincie.vue'
import ChartsPanel from '@/components/ChartsPanel.vue'
import DutchMapKiesKring from '@/components/DutchMapKiesKring.vue'

/* --- View Types --- */
const viewTypes = [
  {
    value: 'province',
    label: 'Provincies',
    icon: 'M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7'
  },
  {
    value: 'kieskring',
    label: 'Kieskringen',
    icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z'
  },
  {
    value: 'municipality',
    label: 'Gemeenten',
    icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6'
  }
]

/* --- Reactive state --- */
const selectedType = ref('province')
const selectedRegion = ref(null)

/* --- Handlers --- */
function handleMunicipalitySelected(m) {
  selectedRegion.value = m
}

function handleProvincieSelected(p) {
  selectedRegion.value = p
}

function handleKieskringSelected(k) {
  selectedRegion.value = k
}

function selectViewType(type) {
  selectedType.value = type
  selectedRegion.value = null
}

/* --- Helper functions --- */
function formatNumber(num) {
  if (!num) return '0'
  return num.toLocaleString('nl-NL')
}

/* --- Computed values --- */
const currentTitle = computed(() => {
  switch (selectedType.value) {
    case 'province':
      return 'Provinciekaart'
    case 'kieskring':
      return 'Kieskringenkaart'
    case 'municipality':
    default:
      return 'Gemeentekaart'
  }
})

/* --- Dynamic map loading --- */
const currentMapComponent = computed(() => {
  switch (selectedType.value) {
    case 'municipality':
      return DutchMapGemeente2024
    case 'province':
      return DutchMapProvincie
    case 'kieskring':
      return DutchMapKiesKring
    default:
      return DutchMapGemeente2024
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', sans-serif;
}

/* Header */
.dashboard-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.dashboard-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/images/banner.png') center/cover;
  opacity: 0.05;
  z-index: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 14px;
}

.breadcrumb-item {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: white;
}

.breadcrumb-item.active {
  color: white;
  font-weight: 600;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.5);
}

.header-info {
  color: white;
}

.election-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 8px 16px;
  border-radius: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.badge-icon {
  width: 20px;
  height: 20px;
  color: white;
}

.page-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 12px 0;
  color: white;
  letter-spacing: -0.5px;
}

.page-description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

/* Dashboard Container */
.dashboard-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

/* View Selector */
.view-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  background: white;
  padding: 12px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.view-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #6c757d;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
}

.view-button:hover {
  background: #f8fafc;
  border-color: #3b82f6;
  color: #3b82f6;
}

.view-button.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.view-icon {
  width: 20px;
  height: 20px;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

/* Card Styles */
.map-card,
.charts-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.map-card:hover,
.charts-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  padding: 24px 28px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.card-actions { }

.card-body {
  padding: 28px;
  flex: 1;
  overflow: auto;
}

/* Map hint removed */

.map-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

/* Live indicator removed */

/* Charts Body */
.charts-body {
  display: flex;
  flex-direction: column;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #dee2e6;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: #495057;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 16px;
  color: #6c757d;
  margin: 0;
}

.charts-content { flex: 1; }

/* Quick stats removed */

/* Scrollbar Styling */
.card-body::-webkit-scrollbar {
  width: 8px;
}

.card-body::-webkit-scrollbar-track {
  background: #f8f9fa;
  border-radius: 10px;
}

.card-body::-webkit-scrollbar-thumb {
  background: #dee2e6;
  border-radius: 10px;
}

.card-body::-webkit-scrollbar-thumb:hover {
  background: #adb5bd;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-container {
    padding: 0 20px 40px;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    padding: 32px 20px 48px;
  }

  .page-title {
    font-size: 32px;
  }

  .page-description {
    font-size: 16px;
  }

  .view-selector {
    flex-direction: column;
  }

  .view-button {
    width: 100%;
  }

  .card-header {
    padding: 20px;
  }

  .card-body {
    padding: 20px;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .card-actions {
    display: none;
  }

  .dashboard-container {
    padding: 0 16px 32px;
  }
}
</style>

