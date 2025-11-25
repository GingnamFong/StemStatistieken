<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const candidates = ref([])
const error = ref(null)
const loading = ref(true)
const search = ref('')
const sortKey = ref('lastName')
const sortDir = ref('asc')
const selectedYear = ref(2023)

import { API_BASE_URL } from '@/config/api'

const availableYears = [2021, 2023, 2025]

const electionId = computed(() => `TK${selectedYear.value}`)

async function loadCandidates() {
  loading.value = true
  error.value = null
  try {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId.value}/candidatelists`, {
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
    candidates.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCandidates()
})

watch(selectedYear, () => {
  loadCandidates()
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

// Get top 3 candidates by votes
const top3Candidates = computed(() => {
  if (!isFilteringByParty.value) return {}
  const q = search.value.toLowerCase()

  const partyTop3 = {}

  // Group candidates by party
  candidates.value.forEach(c => {
    if (c.partyName?.toLowerCase().includes(q)) {
      if (!partyTop3[c.partyName]) {
        partyTop3[c.partyName] = []
      }
      partyTop3[c.partyName].push(c)
    }
  })

  // For each party, sort by votes (descending) and take top 3
  Object.keys(partyTop3).forEach(party => {
    partyTop3[party] = partyTop3[party]
      .sort((a, b) => (b.votes || 0) - (a.votes || 0))
      .slice(0, 3)
  })

  return partyTop3
})

const filteredCandidates = computed(() => {
  let result = candidates.value
  if (search.value.trim()) {
    const searchTerm = search.value.trim().toLowerCase()
    const searchWords = searchTerm.split(/\s+/).filter(word => word.length > 0)

    result = result.filter(c => {
      // Build full name for searching
      const fullName = [
        c.initials,
        c.firstName,
        c.lastName
      ].filter(Boolean).join(' ').toLowerCase()

      // Check if all search words match somewhere in the candidate data
      return searchWords.every(word => {
        return (
          fullName.includes(word) ||
          c.firstName?.toLowerCase().includes(word) ||
          c.lastName?.toLowerCase().includes(word) ||
          c.initials?.toLowerCase().includes(word) ||
          c.partyName?.toLowerCase().includes(word)
        )
      })
    })
  }

  return [...result].sort((a, b) => {
    const dir = sortDir.value === 'asc' ? 1 : -1
    if (sortKey.value === 'candidateIdentifier') return (a.candidateIdentifier - b.candidateIdentifier) * dir
    if (sortKey.value === 'partyName') {
      const aParty = filterUnknown(a.partyName) || ''
      const bParty = filterUnknown(b.partyName) || ''
      return aParty.localeCompare(bParty) * dir
    }
    if (sortKey.value === 'residence') {
      const aRes = filterUnknown(a.residence) || ''
      const bRes = filterUnknown(b.residence) || ''
      return aRes.localeCompare(bRes) * dir
    }
    if (sortKey.value === 'votes') return (a.votes - b.votes) * dir
    const aLastName = filterUnknown(a.lastName) || ''
    const bLastName = filterUnknown(b.lastName) || ''
    return aLastName.localeCompare(bLastName) * dir
  })
})

function viewCandidate(candidate) {
  if (candidate && candidate.id) {
    router.push(`/Candidate/${encodeURIComponent(candidate.id)}?year=${selectedYear.value}`)
  }
}

// filter out "unknown" value
function filterUnknown(value) {
  if (!value) return ''
  const str = String(value).trim()
  if (str.toLowerCase() === 'unknown') return ''
  return str
}

// format candidate name
function formatCandidateName(candidate) {
  const parts = [
    candidate.initials ? filterUnknown(candidate.initials) : '',
    filterUnknown(candidate.firstName),
    filterUnknown(candidate.lastName)
  ].filter(part => part && part.trim())
  return parts.join(' ') || ''
}
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
                <span>Tweede Kamer {{ selectedYear }}</span>
              </div>
              <h1 class="page-title">Kandidaten Lijst</h1>
              <p class="page-description">Bekijk alle kandidaten en hun verkiezingsresultaten</p>
            </div>
            <div class="header-right">
              <div class="year-selector-wrapper">
                <div class="year-selector">
                  <button
                    v-for="year in availableYears"
                    :key="year"
                    @click="selectedYear = year"
                    :class="['year-btn', { active: selectedYear === year }]"
                    :disabled="loading"
                  >
                    {{ year }}
                  </button>
                </div>
              </div>
              <div class="search-wrapper" v-if="candidates.length">
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
      <div v-if="loading && !error" class="loading-container">
        <div class="spinner"></div>
        <p class="loading-text">Kandidaten laden...</p>
      </div>

    <div class="content-wrapper" v-if="!loading && filteredCandidates.length">
      <div v-if="isFilteringByParty" class="top3-sidebar">
        <h3 class="top3-title">Top 3 Kandidaten</h3>
          <div v-for="(candidates, partyName) in top3Candidates" :key="partyName" class="party-section">
          <h4 class="party-name">{{ partyName }}</h4>
          <div class="top3-list">
            <div v-for="c in candidates" :key="c.id" class="top3-item" @click="viewCandidate(c)">
              <div class="candidate-info">
                <span class="candidate-name">
                  {{ c.candidateIdentifier }} {{ formatCandidateName(c) || '-' }}
                </span>
                <span class="candidate-votes">
                  {{ c.votes ? c.votes.toLocaleString() : '0' }} stemmen
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Desktop Table -->
      <div class="table-wrapper desktop-table">
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
          <tr v-for="(c, index) in filteredCandidates" :key="c.id" @click="viewCandidate(c)" class="clickable-row">
            <td class="index-col">{{ index + 1 }}</td>
            <td>{{ c.candidateIdentifier }}</td>
            <td>{{ formatCandidateName(c) || '-' }}</td>
            <td>{{ filterUnknown(c.partyName) || '-' }}</td>
            <td>{{ filterUnknown(c.residence) || '-' }}</td>
            <td class="votes-cell">{{ c.votes ? c.votes.toLocaleString('nl-NL') : '0' }}</td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Mobile Card Layout -->
      <div class="mobile-cards">
        <!-- Mobile Sort Indicator -->
        <div class="mobile-sort-indicator">
          <span class="sort-label">Gesorteerd op:</span>
          <div class="sort-buttons">
            <button
              @click="changeSort('candidateIdentifier')"
              :class="['sort-btn', { active: sortKey === 'candidateIdentifier' }]"
            >
              Identificatie {{ sortKey === 'candidateIdentifier' ? (sortDir === 'asc' ? '↑' : '↓') : '' }}
            </button>
            <button
              @click="changeSort('lastName')"
              :class="['sort-btn', { active: sortKey === 'lastName' }]"
            >
              Naam {{ sortKey === 'lastName' ? (sortDir === 'asc' ? '↑' : '↓') : '' }}
            </button>
            <button
              @click="changeSort('votes')"
              :class="['sort-btn', { active: sortKey === 'votes' }]"
            >
              Stemmen {{ sortKey === 'votes' ? (sortDir === 'asc' ? '↑' : '↓') : '' }}
            </button>
          </div>
        </div>

        <div v-for="(c, index) in filteredCandidates" :key="c.id" class="candidate-card" @click="viewCandidate(c)">
          <div class="card-header">
            <span class="card-number">{{ index + 1 }}</span>
            <div class="card-name-section">
              <h3 class="card-name">{{ formatCandidateName(c) || '-' }}</h3>
              <span class="card-party">{{ filterUnknown(c.partyName) || '-' }}</span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-row">
              <span class="card-label">Identificatie:</span>
              <span class="card-value">{{ c.candidateIdentifier }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">Woonplaats:</span>
              <span class="card-value">{{ filterUnknown(c.residence) || '-' }}</span>
            </div>
            <div class="card-row votes-row">
              <span class="card-label">Stemmen:</span>
              <span class="card-votes">{{ c.votes ? c.votes.toLocaleString('nl-NL') : '0' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

      <p v-else-if="!loading && !error" class="empty">Geen kandidaten gevonden.</p>
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-align: center;
  color: #64748b;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 48px 24px;
  margin-top: 24px;
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

.table-wrapper {
  overflow-x: auto;
  flex: 1;
}

/* Hide mobile cards on desktop */
.mobile-cards {
  display: none;
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

.clickable-row {
  cursor: pointer;
}

.clickable-row:hover {
  background-color: #e0f2fe !important;
}

.index-col {
  text-align: center;
  width: 50px;
}

.votes-cell {
  text-align: right;
  font-weight: 600;
  color: #1e293b;
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

/* Year Selector */
.year-selector-wrapper {
  margin-right: 16px;
}

.year-selector {
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 4px;
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.year-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  font-family: 'Nunito', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  min-width: 70px;
}

.year-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.year-btn.active {
  background: white;
  color: #1e293b;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.year-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  cursor: pointer;
}

.top3-item:hover {
  background-color: #e0f2fe;
}

.candidate-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  width: 100%;
}

.candidate-votes {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 500;
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
    flex-direction: column;
    gap: 16px;
  }

  .year-selector-wrapper {
    width: 100%;
    margin-right: 0;
  }

  .year-selector {
    width: 100%;
    justify-content: stretch;
  }

  .year-btn {
    flex: 1;
  }

  .search-wrapper {
    width: 100%;
  }

  /* Hide desktop table on mobile */
  .desktop-table {
    display: none;
  }

  /* Show mobile cards */
  .mobile-cards {
    display: block;
    width: 100%;
  }

  .mobile-sort-indicator {
    background: white;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
  }

  .sort-label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
    margin-bottom: 12px;
  }

  .sort-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .sort-btn {
    flex: 1;
    min-width: 100px;
    padding: 10px 16px;
    border: 2px solid #e2e8f0;
    background: white;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;
  }

  .sort-btn:hover {
    border-color: #3b82f6;
    color: #3b82f6;
    background: #f8fafc;
  }

  .sort-btn.active {
    border-color: #3b82f6;
    background: #3b82f6;
    color: white;
    font-weight: 600;
  }

  .candidate-card {
    background: white;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .candidate-card:hover {
    background-color: #f0f9ff;
    border-color: #3b82f6;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid #f1f5f9;
  }

  .card-number {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    background: #1e293b;
    color: white;
    border-radius: 8px;
    font-weight: 600;
    font-size: 14px;
  }

  .card-name-section {
    flex: 1;
  }

  .card-name {
    margin: 0 0 4px 0;
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    line-height: 1.3;
  }

  .card-party {
    display: inline-block;
    background: #3b82f6;
    color: white;
    padding: 4px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
  }

  .card-body {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .card-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
  }

  .card-label {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
  }

  .card-value {
    font-size: 14px;
    color: #1e293b;
    font-weight: 500;
    text-align: right;
  }

  .votes-row {
    margin-top: 4px;
    padding-top: 12px;
    border-top: 1px solid #f1f5f9;
  }

  .card-votes {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
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

  .content-wrapper {
    padding: 16px;
  }

  .mobile-sort-indicator {
    padding: 12px;
  }

  .sort-btn {
    min-width: 80px;
    padding: 8px 12px;
    font-size: 13px;
  }

  .candidate-card {
    padding: 12px;
  }

  .card-name {
    font-size: 16px;
  }

  .card-label,
  .card-value {
    font-size: 13px;
  }

  .card-votes {
    font-size: 16px;
  }
}

</style>
