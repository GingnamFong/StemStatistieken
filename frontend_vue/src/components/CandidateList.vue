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
  <div class="candidates-page">
    <!-- Header with breadcrumb -->
    <header class="page-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Kandidaten</span>
        </div>
        <div class="header-info">
          <div class="header-top-row">
            <div class="header-left">
              <div class="election-badge">
                <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                <span>Tweede Kamer 2023</span>
              </div>
              <h1 class="page-title">Kandidaten Lijst</h1>
              <p class="page-description">Bekijk alle kandidaten en hun verkiezingsresultaten</p>
            </div>
            <div class="header-right" v-if="candidates.length">
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
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <p v-if="error" class="error">⚠️ {{ error }}</p>
      <p v-if="!candidates.length && !error" class="loading">Loading candidates...</p>

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
  </div>
</template>

<style scoped>
.candidates-page {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Header */
.page-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.page-header::before {
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

/* Page Container */
.page-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

.content-wrapper {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 24px;
}

.error {
  color: #e74c3c;
  text-align: center;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 24px;
}

.loading {
  text-align: center;
  color: #64748b;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 24px;
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

.search-wrapper {
  position: relative;
  width: 350px;
  min-width: 280px;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  font-size: 15px;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  color: white;
  font-family: 'Nunito', sans-serif;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.search-input:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2), 0 2px 8px rgba(0, 0, 0, 0.15);
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.8);
  pointer-events: none;
  transition: color 0.2s;
}

.search-wrapper:focus-within .search-icon {
  color: #3b82f6;
}

.search-icon svg {
  width: 20px;
  height: 20px;
  display: block;
}

.empty {
  text-align: center;
  color: #64748b;
  margin-top: 1rem;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
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

/* Responsive Design */
@media (max-width: 1200px) {
  .content-wrapper {
    flex-direction: column;
  }

  .page-container {
    padding: 0 20px 40px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 32px 20px 48px;
  }

  .page-title {
    font-size: 32px;
  }

  .page-description {
    font-size: 16px;
  }

  .content-wrapper {
    flex-direction: column;
    padding: 20px;
  }

  .top3-sidebar {
    min-width: 100%;
    position: static;
    margin-bottom: 1rem;
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

  .search-wrapper {
    width: 100%;
  }

  table {
    font-size: 14px;
  }

  th, td {
    padding: 0.5rem;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .page-container {
    padding: 0 16px 32px;
  }

  .toolbar {
    padding: 12px;
  }

  table {
    font-size: 12px;
  }

  th, td {
    padding: 0.4rem;
  }
}

</style>
