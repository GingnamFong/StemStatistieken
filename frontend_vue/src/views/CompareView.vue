<template>
  <div class="dashboard">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Vergelijken</span>
        </div>
        <div class="header-info">
          <div class="header-top-row">
            <div class="header-left">
              <div class="election-badge">
                <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                <span>Vergelijk Verkiezingen</span>
              </div>
              <h1 class="page-title">Vergelijk Uitslagen</h1>
              <p class="page-description">Vergelijk provincies of gemeentes tussen verschillende jaren</p>
            </div>
            <div class="header-right">
              <button class="btn-reset" @click="resetAll">
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4v6h6M20 20v-6h-6M20 8a8 8 0 00-13.657-5.657L4 4m16 16l-2.343 2.343A8 8 0 018 20" />
                </svg>
                Reset selectie
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div class="dashboard-container">
      <!-- Selection Cards -->
      <div class="selection-row">
        <!-- Column 1 -->
        <CompareSelectionColumn
          :column-number="1"
          v-model="columns[0]"
          :available-selections="availableSelections[0]"
          :selected-type="selectedType"
          :disabled="false"
          :show-remove="false"
          @type-change="() => handleTypeChange(0)"
          @year-change="(year) => handleYearChange(0, year)"
          @selection-change="(selection) => handleSelectionChange(0, selection)"
        />

        <!-- Column 2 -->
        <CompareSelectionColumn
          :column-number="2"
          v-model="columns[1]"
          :available-selections="availableSelections[1]"
          :selected-type="selectedType"
          :disabled="columns[0].type === ''"
          :show-remove="false"
          @type-change="() => handleTypeChange(1)"
          @year-change="(year) => handleYearChange(1, year)"
          @selection-change="(selection) => handleSelectionChange(1, selection)"
        />

        <!-- Column 3 (Optional) -->
        <CompareSelectionColumn
          v-if="showThirdColumn"
          :column-number="3"
          v-model="columns[2]"
          :available-selections="availableSelections[2]"
          :selected-type="selectedType"
          :disabled="columns[0].type === ''"
          :show-remove="true"
          @type-change="() => handleTypeChange(2)"
          @year-change="(year) => handleYearChange(2, year)"
          @selection-change="(selection) => handleSelectionChange(2, selection)"
          @remove="removeThirdColumn"
        />

        <!-- Add Column Button -->
        <CompareAddColumnCard v-else @add="addThirdColumn" />
      </div>

      <!-- Results Table -->
      <CompareResultsTable
        v-if="hasAnyResults"
        :columns="activeColumns"
        :all-columns="columns"
        :available-selections="availableSelections"
      />

      <!-- Empty State -->
      <section v-else class="empty-card">
        <div class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <h3 class="empty-title">Geen gegevens om te vergelijken</h3>
          <p class="empty-description">Selecteer regio's hierboven om de resultaten te vergelijken</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ProvincieService from '@/services/ProvincieService'
import ElectionService from '@/services/ElectionService'
import CompareSelectionColumn from '@/components/CompareSelectionColumn.vue'
import CompareAddColumnCard from '@/components/CompareAddColumnCard.vue'
import CompareResultsTable from '@/components/CompareResultsTable.vue'

const showThirdColumn = ref(false)
const columns = ref([
  { type: '', year: '', selection: '', data: null },
  { type: '', year: '', selection: '', data: null },
  { type: '', year: '', selection: '', data: null }
])
const availableSelections = ref([[], [], []])

const selectedType = computed(() => {
  // Find the first column that has a type selected
  for (let i = 0; i < columns.value.length; i++) {
    if (columns.value[i].type) {
      return columns.value[i].type
    }
  }
  return null
})

const activeColumns = computed(() => {
  return columns.value.filter(col => col.data !== null)
})

const hasAnyResults = computed(() => {
  return activeColumns.value.length > 0
})

async function addThirdColumn() {
  showThirdColumn.value = true
  // Only set the type if there's already a selected type, but don't reset other columns
  if (selectedType.value) {
    columns.value[2].type = selectedType.value
    // If there's already a year selected in another column, use that and load selections
    const existingYear = columns.value[0].year || columns.value[1].year
    if (existingYear) {
      columns.value[2].year = existingYear
      await handleYearChange(2, existingYear)
    }
  }
}

function removeThirdColumn() {
  showThirdColumn.value = false
  columns.value[2] = { type: '', year: '', selection: '', data: null }
  availableSelections.value[2] = []
}

function handleTypeChange(index) {
  const newType = columns.value[index].type

  // Propagate type to ALL other columns and reset their selections
  for (let i = 0; i < columns.value.length; i++) {
    if (i !== index) {
      // Skip column 2 if third column is not shown
      if (i === 2 && !showThirdColumn.value) continue

      columns.value[i].type = newType
      columns.value[i].year = ''
      columns.value[i].selection = ''
      columns.value[i].data = null
      availableSelections.value[i] = []
    }
  }

  // Reset current column
  columns.value[index].year = ''
  columns.value[index].selection = ''
  columns.value[index].data = null
  availableSelections.value[index] = []
}

async function handleYearChange(index, year) {
  columns.value[index].selection = ''
  columns.value[index].data = null

  const type = columns.value[index].type

  if (!type || !year) return

  try {
    if (type === 'provincie') {
      const provincies = await ProvincieService.getAllProvincies()
      availableSelections.value[index] = provincies
    } else if (type === 'gemeente') {
      const gemeentes = await ElectionService.getMunicipalities(year)
      availableSelections.value[index] = gemeentes
    }
  } catch (error) {
    console.error(`Failed to load ${type} for column ${index}:`, error)
    availableSelections.value[index] = []
  }
}

async function loadColumnData(index) {
  const col = columns.value[index]

  if (!col.type || !col.year || !col.selection) {
    col.data = null
    return
  }

  try {
    if (col.type === 'provincie') {
      const data = await ProvincieService.getProvincieResultaten(col.selection)
      col.data = data
    } else if (col.type === 'gemeente') {
      const data = await ElectionService.getMunicipality(col.year, col.selection)
      col.data = {
        totaalStemmen: data.validVotes || 0,
        partijen: (data.allParties || []).map(p => ({
          naam: p.name,
          stemmen: p.votes,
          percentage: data.validVotes > 0 ? ((p.votes / data.validVotes) * 100).toFixed(1) : '0.0'
        }))
      }
    }
  } catch (error) {
    console.error(`Failed to load data for column ${index}:`, error)
    col.data = null
  }
}

function handleSelectionChange(index, selection) {
  columns.value[index].selection = selection
  loadColumnData(index)
}

function resetAll() {
  // Hide third column
  showThirdColumn.value = false
  // Reset all columns and selections
  for (let i = 0; i < 3; i++) {
    columns.value[i] = { type: '', year: '', selection: '', data: null }
    availableSelections.value[i] = []
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', sans-serif;
}

/* Header - Same as ElectionDashboardView */
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

.header-top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 32px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  padding-bottom: 4px;
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

/* Reset Button */
.btn-reset {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px 12px 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
  /* MATCH Kandidaten search input */
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-reset:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
}

.btn-reset:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2), 0 2px 8px rgba(0, 0, 0, 0.15);
}

.btn-reset .btn-icon {
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.9);
}

/* Selection Row */
.selection-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

/* Empty Card */
.empty-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 60px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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

/* Responsive */
@media (max-width: 1024px) {
  .selection-row {
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

  .header-top-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .header-right {
    width: 100%;
    padding-bottom: 0;
  }

  .btn-reset {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .dashboard-container {
    padding: 0 16px 32px;
  }
}
</style>
