<template>
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
        <div v-if="isLoadingData" class="loading-container">
          <div class="spinner"></div>
          <p class="loading-text">{{ loadingText }}</p>
        </div>
        <template v-else>
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <h3 class="empty-title">Geen gegevens om te vergelijken</h3>
          <p class="empty-description">Selecteer regio's hierboven om de resultaten te vergelijken</p>
        </template>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import ProvincieService from '@/services/ProvincieService'
import ElectionService from '@/services/ElectionService'
import MunicipalityService from '@/services/MunicipalityService'
import ConstituencyService from '@/services/ConstituencyService'
import PollingstationService, {PollingStationService} from '@/services/PollingstationService'
import CompareSelectionColumn from '@/components/CompareSelectionColumn.vue'
import CompareAddColumnCard from '@/components/CompareAddColumnCard.vue'
import CompareResultsTable from '@/components/CompareResultsTable.vue'

const loading = ref(false)
const error = ref(null)
const showThirdColumn = ref(false)
const columns = ref([
  { type: '', year: '', selection: '', data: null },
  { type: '', year: '', selection: '', data: null },
  { type: '', year: '', selection: '', data: null }
])
const availableSelections = ref([[], [], []])
const isLoadingData = ref(false)
const loadingText = ref('Data laden...')

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

function setLoadingState(type) {
  isLoadingData.value = true
  if (type === 'provincie') {
    loadingText.value = 'Provincie data laden...'
  } else if (type === 'gemeente') {
    loadingText.value = 'Gemeente data laden...'
  } else if (type === 'kieskring') {
    loadingText.value = 'Kieskring data laden...'
  } else if (type === 'stembureau') {
  loadingText.value = 'Stembureau data laden...'
}
}

async function preloadData() {
  loading.value = true
  try {
    await ElectionService.getElection('TK2023')
    console.log('✅ Election data ready')

    const gemeentes = await MunicipalityService.getMunicipalities('TK2023')
    console.log('✅ Loaded municipalities:', gemeentes.length)
    availableSelections.value[0] = gemeentes

  } catch (err) {
    console.error('❌ Error preloading data:', err)
    error.value = err.message
  } finally {
    loading.value = false
  }
}

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

  setLoadingState(type)

  try {
    await ElectionService.getElection(`TK${year}`).catch(() => null)

    if (type === 'provincie') {
      const electionId = `TK${year}`
      const provincies = await ProvincieService.getAllProvincies(electionId)
      availableSelections.value[index] = provincies
    } else if (type === 'gemeente') {
      const gemeentes = await MunicipalityService.getMunicipalities(`TK${year}`)
      availableSelections.value[index] = gemeentes
    } else if (type === 'kieskring') {
      const kieskringen = await ConstituencyService.getConstituencies(`TK${year}`)
      availableSelections.value[index] = kieskringen
    } else if (type === 'stembureau') {
      // Load ALL polling stations and extract unique postal codes
      const all = await PollingStationService.getAll(`TK${year}`)

      const postcodes = [...new Set(all.map(sb => sb.postalCode))]

      availableSelections.value[index] = postcodes.map(pc => ({ id: pc, name: pc }))
    }

  } catch (error) {
    console.error(`Failed to load ${type} for column ${index}:`, error)
    availableSelections.value[index] = []
  } finally {
    isLoadingData.value = false
  }
}

async function loadColumnData(index) {
  const col = columns.value[index]

  if (!col.type || !col.year || !col.selection) {
    col.data = null
    return
  }

  setLoadingState(col.type)

  try {
    if (col.type === 'provincie') {
      const electionId = `TK${col.year}`
      const data = await ProvincieService.getProvincieResultaten(col.selection, electionId)
      col.data = data

    } else if (col.type === 'gemeente') {
      const data = await MunicipalityService.getMunicipality(`TK${col.year}`, col.selection)
      col.data = {
        totaalStemmen: data.validVotes || 0,
        partijen: (data.allParties || []).map(p => ({
          naam: p.name,
          stemmen: p.votes,
          percentage: data.validVotes > 0 ? ((p.votes / data.validVotes) * 100).toFixed(1) : '0.0'
        }))
      }

    } else if (col.type === 'kieskring') {
      const allConstituencies = await ConstituencyService.getConstituencies(`TK${col.year}`)
      const constituency = allConstituencies.find(c => c.id === col.selection || c.name === col.selection)

      if (!constituency) {
        col.data = null
      } else {
        const partijTotaal = {}

        for (const gemeente of constituency.municipalities || []) {
          for (const partij of gemeente.allParties || []) {
            if (!partijTotaal[partij.name]) partijTotaal[partij.name] = 0
            partijTotaal[partij.name] += partij.votes
          }
        }

        // Bereken totaal stemmen: gebruik totalVotes als beschikbaar, anders tel alle partijstemmen op
        let totaalStemmen = constituency.totalVotes || 0
        if (totaalStemmen === 0) {
          // Fallback: tel alle partijstemmen op
          totaalStemmen = Object.values(partijTotaal).reduce((sum, votes) => sum + votes, 0)
        }

        const partijen = Object.entries(partijTotaal).map(([naam, stemmen]) => ({
          naam,
          stemmen,
          percentage: totaalStemmen > 0 ? ((stemmen / totaalStemmen) * 100).toFixed(1) : '0.0'
        }))

        col.data = {
          totaalStemmen,
          partijen
        }
      }
    } else if (col.type === 'stembureau') {
      const data = await PollingstationService.getByPostalCode(`TK${col.year}`, col.selection)

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
  } finally {
    isLoadingData.value = false
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

// Expose resetAll for parent component
defineExpose({
  resetAll
})

onMounted(() => {
  preloadData()
})
</script>

<style scoped>
/* Dashboard Container */
.dashboard-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 20px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-text {
  font-size: 16px;
  font-weight: 500;
  color: #475569;
  margin: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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

@media (max-width: 480px) {
  .dashboard-container {
    padding: 0 16px 32px;
  }
}
</style>

