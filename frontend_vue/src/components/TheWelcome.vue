<script setup>
import { ref, onMounted } from 'vue'
import { API_BASE_URL } from '@/config/api'

const election = ref(null)
const error = ref(null)

onMounted(async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/elections/TK2023`)
    if (!response.ok) throw new Error('Failed to load election data')
    election.value = await response.json()
  } catch (err) {
    error.value = err.message
  }
})
</script>

<template>
  <div class="container">
    <h1 class="title">🗳️ Election: {{ election?.id }}</h1>

    <p v-if="error" class="error">⚠️ {{ error }}</p>
    <p v-if="!election && !error" class="loading">Loading...</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
        <tr>
          <th>#</th>
          <th>Constituency Name</th>
          <th>ID</th>
          <th>Total Votes</th>
        </tr>
        </thead>
        <tbody>
        <tr
          v-for="(constituency, index) in election.constituencies"
          :key="constituency.id"
        >
          <td>{{ index + 1 }}</td>
          <td>{{ constituency.name }}</td>
          <td>{{ constituency.id }}</td>
          <td>{{ constituency.totalVotes.toLocaleString() }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
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

th:last-child, td:last-child {
  text-align: right;
  font-weight: 600;
}
</style>
