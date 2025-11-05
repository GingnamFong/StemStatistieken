<script setup>
import { ref, onMounted, computed } from 'vue'

const candidates = ref([])
const error = ref(null)
const search = ref('')
const sortKey = ref('lastName')
const sortDir = ref('asc')

const API_BASE_URL =
  (location.origin === 'https://hva-frontend.onrender.com')
    ? 'https://hva-backend-c647.onrender.com'
    : 'http://localhost:8081'

onMounted(async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/elections/TK2023/candidatelists`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) throw new Error('Failed to load candidate data')
    const data = await response.json()
    candidates.value = data.candidates || []
  } catch (err) {
    error.value = err.message
  }
})

function changeSort(key) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortDir.value = 'asc'
  }
}

function getSortIcon(key) {
  if (sortKey.value !== key) return ''
  return sortDir.value === 'asc' ? '↑' : '↓'
}

// filtering by party name
const isFilteringByParty = computed(() => {
  if (!search.value.trim()) return false
  const q = search.value.toLowerCase()

  const matchingParties = new Set()
  candidates.value.forEach(c => {
    if (c.partyName?.toLowerCase().includes(q)) {
      matchingParties.add(c.partyName)
    }
  })

  return matchingParties.size > 0
})

// Get top 3 candidates
const top3Candidates = computed(() => {
  if (!isFilteringByParty.value) return {}
  const q = search.value.toLowerCase()

  const partyTop3 = {}

  candidates.value.forEach(c => {
    if (c.partyName?.toLowerCase().includes(q) &&
        (c.candidateIdentifier === 1 || c.candidateIdentifier === 2 || c.candidateIdentifier === 3)) {
      if (!partyTop3[c.partyName]) {
        partyTop3[c.partyName] = []
      }
      partyTop3[c.partyName].push(c)
    }
  })

  Object.keys(partyTop3).forEach(party => {
    partyTop3[party].sort((a, b) => a.candidateIdentifier - b.candidateIdentifier)
  })

  return partyTop3
})

const filteredCandidates = computed(() => {
  let result = candidates.value
  if (search.value.trim()) {
    const q = search.value.toLowerCase()
    result = result.filter(
      c =>
        c.firstName?.toLowerCase().includes(q) ||
        c.lastName?.toLowerCase().includes(q) ||
        c.initials?.toLowerCase().includes(q) ||
        c.partyName?.toLowerCase().includes(q)
    )
  }

  return [...result].sort((a, b) => {
    const dir = sortDir.value === 'asc' ? 1 : -1
    if (sortKey.value === 'candidateIdentifier') return (a.candidateIdentifier - b.candidateIdentifier) * dir
    if (sortKey.value === 'partyName') return a.partyName.localeCompare(b.partyName) * dir
    if (sortKey.value === 'residence') return a.residence.localeCompare(b.residence) * dir
    if (sortKey.value === 'votes') return (a.votes - b.votes) * dir
    return a.lastName.localeCompare(b.lastName) * dir
  })
})
</script>

<template>
  <div class="container">
    <h1 class="title">
      <span class="title-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </span>
      Kandidaten Lijst
    </h1>

    <p v-if="error" class="error">⚠️ {{ error }}</p>
    <p v-if="!candidates.length && !error" class="loading">Loading candidates...</p>

    <div class="toolbar" v-if="candidates.length">
      <div class="search-wrapper">
        <input
          v-model="search"
          type="text"
          placeholder="Zoeken op naam of partij..."
          class="search-input"
          aria-label="Zoeken"
        />
        <span class="search-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </span>
      </div>
    </div>

    <div class="content-wrapper" v-if="filteredCandidates.length">
      <div v-if="isFilteringByParty" class="top3-sidebar">
        <h3 class="top3-title">Top 3 Kandidaten</h3>
        <div v-for="(candidates, partyName) in top3Candidates" :key="partyName" class="party-section">
          <h4 class="party-name">{{ partyName }}</h4>
          <div class="top3-list">
            <div v-for="c in candidates" :key="c.id" class="top3-item">
              <span class="candidate-name">
                {{ c.candidateIdentifier }} {{ c.initials ? c.initials + ' ' : '' }}{{ c.firstName }} {{ c.lastName }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="table-wrapper">
        <table>
          <thead>
          <tr>
            <th class="index-col">#</th>
            <th @click="changeSort('candidateIdentifier')" class="sortable">
              Identificatie <span class="sort-icon">{{ getSortIcon('candidateIdentifier') }}</span>
            </th>
            <th @click="changeSort('lastName')" class="sortable">
              Naam <span class="sort-icon">{{ getSortIcon('lastName') }}</span>
            </th>
            <th @click="changeSort('partyName')" class="sortable">
              Partij <span class="sort-icon">{{ getSortIcon('partyName') }}</span>
            </th>
            <th @click="changeSort('residence')" class="sortable">
              Woonplaats <span class="sort-icon">{{ getSortIcon('residence') }}</span>
            </th>
            <th @click="changeSort('votes')" class="sortable">
              Stemmen <span class="sort-icon">{{ getSortIcon('votes') }}</span>
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(c, index) in filteredCandidates" :key="c.id">
            <td class="index-col">{{ index + 1 }}</td>
            <td>{{ c.candidateIdentifier }}</td>
            <td>{{ c.initials ? c.initials + ' ' : '' }}{{ c.firstName }} {{ c.lastName }}</td>
            <td>{{ c.partyName }}</td>
            <td>{{ c.residence }}</td>
            <td>{{ c.votes ? c.votes.toLocaleString() : '0' }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <p v-else-if="!error" class="empty">No candidates found.</p>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 1rem 2rem;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.content-wrapper {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
}

.title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  text-align: center;
  margin-bottom: 1rem;
  font-size: 1.8rem;
  color: #1e293b;
  font-weight: 700;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #1e293b;
}

.title-icon svg {
  width: 28px;
  height: 28px;
}

.error {
  color: #e74c3c;
  text-align: center;
}

.loading {
  text-align: center;
  color: #64748b;
}

.table-wrapper {
  overflow-x: auto;
  flex: 1;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

thead {
  background-color: #1e293b;
  color: white;
}

th, td {
  padding: 0.75rem;
  text-align: left;
}

tbody tr:nth-child(odd) {
  background-color: #f8fafc;
}

tbody tr:hover {
  background-color: #f1f5f9;
  transition: background-color 0.2s ease;
}

.index-col {
  text-align: center;
  width: 50px;
}

.sortable {
  cursor: pointer;
  user-select: none;
  position: relative;
  white-space: nowrap;
  transition: all 0.2s;
}

.sortable:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.sort-icon {
  margin-left: 0.5rem;
  font-size: 0.9em;
  opacity: 0.8;
  display: inline;
  vertical-align: middle;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1rem;
}

.search-wrapper {
  position: relative;
  width: 300px;
  min-width: 250px;
}

.search-input {
  width: 100%;
  padding: 8px 36px 8px 36px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.18);
  font-size: 14px;
  transition: all 0.2s;
  background: rgba(0, 0, 0, 0.02);
  color: #1e293b;
  font-family: 'Nunito', sans-serif;
}

.search-input::placeholder {
  color: rgba(0, 0, 0, 0.5);
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

.search-icon svg {
  width: 18px;
  height: 18px;
  display: block;
}

.empty {
  text-align: center;
  color: #64748b;
  margin-top: 1rem;
}

.top3-sidebar {
  min-width: 250px;
  background: white;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 100px;
}

.top3-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #1e293b;
}

.party-section {
  margin-bottom: 1.5rem;
}

.party-section:last-child {
  margin-bottom: 0;
}

.party-name {
  font-size: 1rem;
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 0.5rem;
}

.top3-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.top3-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  border-radius: 6px;
  background-color: #f8fafc;
  transition: background-color 0.2s ease;
}

.top3-item:hover {
  background-color: #f1f5f9;
}

</style>
