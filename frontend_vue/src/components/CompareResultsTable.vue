<template>
  <section class="results-card">
    <div class="card-header">
      <h2 class="card-title">Vergelijkingsresultaten</h2>
      <button @click="toggleSortOrder" class="sort-toggle" :title="sortDescending ? 'Sorteer: Laag → Hoog' : 'Sorteer: Hoog → Laag'">
        <svg v-if="sortDescending" class="sort-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M7 13l5 5 5-5M7 6l5-5 5 5" />
        </svg>
        <svg v-else class="sort-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M7 17l5-5 5 5M7 6l5 5 5-5" />
        </svg>
        <span class="sort-label">{{ sortDescending ? 'Hoog → Laag' : 'Laag → Hoog' }}</span>
      </button>
    </div>

    <div class="card-body">
      <div class="table-wrapper">
        <table class="comparison-table">
          <thead>
            <tr>
              <th class="party-column">Partij</th>
              <th v-for="(col, idx) in columns" :key="idx" class="result-column">
                <div class="column-header">
                  <div class="column-title">{{ getColumnTitle(col) }}</div>
                  <div class="column-subtitle">{{ getColumnSubtitle(col) }}</div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="party in allParties" :key="party">
              <td class="party-name">{{ party }}</td>
              <td v-for="(col, idx) in columns" :key="idx" class="result-cell">
                <div v-if="getPartyData(col, party)" class="result-content">
                  <div class="votes">{{ formatNumber(getPartyData(col, party).votes) }}</div>
                  <div class="percentage">{{ getPartyData(col, party).percentage }}%</div>
                </div>
                <div v-else class="no-data">—</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed } from 'vue'

const sortDescending = ref(true)

function toggleSortOrder() {
  sortDescending.value = !sortDescending.value
}

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  allColumns: {
    type: Array,
    required: true
  },
  availableSelections: {
    type: Array,
    default: () => []
  }
})

const allParties = computed(() => {
  const partiesSet = new Set()

  props.columns.forEach(col => {
    if (col.data && col.data.partijen) {
      col.data.partijen.forEach(party => {
        partiesSet.add(party.naam)
      })
    }
  })

  // Sort by total votes (highest to lowest) across all columns
  const parties = Array.from(partiesSet)
  return parties.sort((a, b) => {
    let aTotal = 0
    let bTotal = 0

    props.columns.forEach(col => {
      const aData = getPartyData(col, a)
      const bData = getPartyData(col, b)
      if (aData) aTotal += aData.votes
      if (bData) bTotal += bData.votes
    })

    // Sort based on toggle: descending (high to low) or ascending (low to high)
    return sortDescending.value ? bTotal - aTotal : aTotal - bTotal
  })
})

function getColumnTitle(col) {
  // Find the original column index in allColumns
  const originalIndex = props.allColumns.findIndex(c => c === col)
  const selections = props.availableSelections[originalIndex] || []
  const item = selections.find(s => (s.id || s.naam) === col.selection)
  return item ? (item.name || item.naam) : col.selection
}

function getColumnSubtitle(col) {
  if (!col.data) return ''
  const year = col.year === 'TK2023' ? '2023' : col.year
  return `${year} • ${formatNumber(col.data.totaalStemmen)} stemmen`
}

function getPartyData(col, partyName) {
  if (!col.data || !col.data.partijen) return null
  const party = col.data.partijen.find(p => p.naam === partyName)
  if (!party) return null
  return {
    votes: party.stemmen,
    percentage: party.percentage
  }
}

function formatNumber(num) {
  if (!num) return '0'
  return num.toLocaleString('nl-NL')
}
</script>

<style scoped>
.results-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  padding: 24px 28px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.sort-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #1f2937;
  font-size: 15px;
  font-weight: 500;
  font-family: 'Nunito', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-toggle:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #f8fafc;
}

.sort-toggle:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.sort-icon {
  width: 18px;
  height: 18px;
}

.sort-label {
  font-weight: 600;
}

.card-body {
  padding: 0;
}

.table-wrapper {
  overflow-x: auto;
}

.comparison-table {
  width: 100%;
  border-collapse: collapse;
}

.comparison-table thead {
  background: #f8fafc;
  border-bottom: 2px solid #e5e7eb;
}

.comparison-table th {
  padding: 16px 20px;
  text-align: left;
  font-weight: 600;
  color: #374151;
}

.party-column {
  min-width: 200px;
  position: sticky;
  left: 0;
  background: #f8fafc;
  z-index: 1;
}

.result-column {
  min-width: 180px;
}

.column-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.column-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.column-subtitle {
  font-size: 13px;
  font-weight: 400;
  color: #6b7280;
}

.comparison-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.comparison-table tbody tr:hover {
  background: #f9fafb;
}

.comparison-table td {
  padding: 16px 20px;
}

.party-name {
  font-weight: 600;
  color: #1f2937;
  position: sticky;
  left: 0;
  background: white;
  z-index: 1;
}

.comparison-table tbody tr:hover .party-name {
  background: #f9fafb;
}

.result-cell {
  text-align: left;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.votes {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.percentage {
  font-size: 14px;
  color: #6b7280;
}

.no-data {
  color: #9ca3af;
  font-size: 18px;
}

@media (max-width: 768px) {
  .card-header {
    padding: 20px;
  }

  .comparison-table th,
  .comparison-table td {
    padding: 12px 16px;
  }
}
</style>

