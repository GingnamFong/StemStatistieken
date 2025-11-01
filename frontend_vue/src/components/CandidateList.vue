<script setup>
import { ref, onMounted, computed } from 'vue'

const candidates = ref([])
const error = ref(null)
const search = ref('')
const sortKey = ref('lastName')
const sortDir = ref('asc')

onMounted(async () => {
  try {
    const response = await fetch('http://localhost:8081/elections/TK2023/candidatelists', {
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
    return a.lastName.localeCompare(b.lastName) * dir
  })
})
</script>

<template>
  <div class="container">
    <h1 class="title">👤 Kandidaten Lijst</h1>

    <p v-if="error" class="error">⚠️ {{ error }}</p>
    <p v-if="!candidates.length && !error" class="loading">Loading candidates...</p>

    <div class="toolbar" v-if="candidates.length">
      <input
        v-model="search"
        placeholder="🔍 Search by name or party..."
        class="search"
      />
    </div>

    <div class="table-wrapper" v-if="filteredCandidates.length">
      <table>
        <thead>
        <tr>
          <th>#</th>
          <th @click="changeSort('candidateIdentifier')" class="sortable">
            Identifier <span class="sort-icon">{{ getSortIcon('candidateIdentifier') }}</span>
          </th>
          <th @click="changeSort('lastName')" class="sortable">
            Name <span class="sort-icon">{{ getSortIcon('lastName') }}</span>
          </th>
          <th @click="changeSort('partyName')" class="sortable">
            Party <span class="sort-icon">{{ getSortIcon('partyName') }}</span>
          </th>
          <th @click="changeSort('residence')" class="sortable">
            Residence <span class="sort-icon">{{ getSortIcon('residence') }}</span>
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(c, index) in filteredCandidates" :key="c.id">
          <td>{{ index + 1 }}</td>
          <td>{{ c.candidateIdentifier }}</td>
          <td>{{ c.initials ? c.initials + ' ' : '' }}{{ c.firstName }} {{ c.lastName }}</td>
          <td>{{ c.partyName }}</td>
          <td>{{ c.residence }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <p v-else-if="!error" class="empty">No candidates found.</p>
  </div>
</template>

<style scoped>
.container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 1rem 2rem;
  background: #fdfdfd;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  font-family: 'Inter', system-ui, sans-serif;
}

.title {
  text-align: center;
  margin-bottom: 1rem;
  font-size: 1.8rem;
  color: #2c3e50;
}

.error {
  color: #e74c3c;
  text-align: center;
}

.loading {
  text-align: center;
  color: #555;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

thead {
  background-color: #2c3e50;
  color: white;
}

th, td {
  padding: 0.75rem;
  text-align: left;
}

tbody tr:nth-child(odd) {
  background-color: #f5f6fa;
}

tbody tr:hover {
  background-color: #eaf1ff;
  transition: background-color 0.2s ease;
}

th:first-child, td:first-child {
  text-align: center;
  width: 50px;
}

.sortable {
  cursor: pointer;
  user-select: none;
  position: relative;
  white-space: nowrap;
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
  margin-bottom: 0.5rem;
}

.search {
  padding: 0.5rem 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
  width: 250px;
}

.empty {
  text-align: center;
  color: #888;
  margin-top: 1rem;
}
</style>
