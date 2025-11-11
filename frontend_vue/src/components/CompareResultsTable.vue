<template>
  <section class="results-card">
    <div class="card-header">
      <h2 class="card-title">Vergelijkingsresultaten</h2>
    </div>

    <div class="card-body">
      <div class="table-wrapper">
        <table class="comparison-table">
          <thead>
            <tr>
              <th class="party-column">Partij</th>
              <th v-for="(col, idx) in columns" :key="idx" class="result-column">
                <div class="column-header">
                  <div class="column-title">{{ getColumnTitle(col, idx) }}</div>
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
import { computed } from 'vue'

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

  return Array.from(partiesSet).sort()
})

function getColumnTitle(col, index) {
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
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
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

