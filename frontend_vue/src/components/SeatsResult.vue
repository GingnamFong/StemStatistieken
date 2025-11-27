<template>
  <div class="seats-display">
    <div v-if="loading" class="loading-state">
      <p>Zetels laden...</p>
    </div>
    
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
    </div>
    
    <div v-else-if="seatsData.length === 0" class="empty-state">
      <p>Geen zetelgegevens beschikbaar</p>
    </div>
    
    <div v-else class="seats-content">
      <div class="seats-header">
        <h3>Zetelverdeling Tweede Kamer {{ year }}</h3>
        <div class="totals-info">
          <p class="total-seats">Totaal: <strong>{{ totalSeats }}</strong> zetels</p>
          <p class="total-votes">Totaal: <strong>{{ formatNumber(totalVotes) }}</strong> geldige stemmen</p>
        </div>
      </div>
      
      <div class="seats-list">
        <div
          v-for="party in sortedSeats"
          :key="party.partyId"
          class="seat-item"
        >
          <div class="party-info">
            <span class="party-name">{{ party.partyName }}</span>
          </div>
          <div class="seat-info">
            <span class="seat-count">{{ party.numberOfSeats }}</span>
            <span class="seat-label">zetel{{ party.numberOfSeats !== 1 ? 's' : '' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { API_BASE_URL } from '@/config/api'

const props = defineProps({
  year: {
    type: Number,
    required: true
  }
})

const seatsData = ref([])
const loading = ref(true)
const error = ref(null)

const electionId = computed(() => `TK${props.year}`)

const sortedSeats = computed(() => {
  return [...seatsData.value]
    .filter(party => party.seats_Data?.numberOfSeats > 0)
    .sort((a, b) => b.seats_Data.numberOfSeats - a.seats_Data.numberOfSeats)
    .map(party => ({
      partyId: party.party_Info.partyId,
      partyName: party.party_Info.partyName,
      numberOfSeats: party.seats_Data.numberOfSeats
    }))
})

const totalSeats = computed(() => {
  return sortedSeats.value.reduce((sum, party) => sum + party.numberOfSeats, 0)
})

const totalVotes = computed(() => {
  return seatsData.value
    .filter(party => party.seats_Data?.validVotes > 0)
    .reduce((sum, party) => sum + (party.seats_Data?.validVotes || 0), 0)
})

function formatNumber(num) {
  if (!num) return '0'
  return num.toLocaleString('nl-NL')
}

async function fetchSeats() {
  loading.value = true
  error.value = null
  
  try {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId.value}/national/results-with-seats`)
    
    if (!response.ok) {
      throw new Error(`Failed to fetch seats: ${response.statusText}`)
    }
    
    const data = await response.json()
    seatsData.value = data
  } catch (err) {
    error.value = err.message || 'Er is een fout opgetreden bij het laden van zetelgegevens'
    console.error('Error fetching seats:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSeats()
})

watch(() => props.year, () => {
  fetchSeats()
})
</script>

<style scoped>
.seats-display {
  width: 100%;
  min-height: 400px;
  padding: 20px;
  font-family: 'Nunito', sans-serif;
}

.loading-state,
.error-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
  color: #6c757d;
}

.error-state {
  color: #dc3545;
}

.seats-content {
  width: 100%;
}

.seats-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e9ecef;
}

.seats-header h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.totals-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.total-seats,
.total-votes {
  font-size: 16px;
  color: #6c757d;
  margin: 0;
}

.total-seats strong,
.total-votes strong {
  color: #1a1a1a;
  font-size: 18px;
}

.seats-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.seat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  transition: all 0.2s ease;
}

.seat-item:hover {
  background: #e9ecef;
  border-color: #3b82f6;
  transform: translateX(4px);
}

.party-info {
  flex: 1;
}

.party-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.seat-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.seat-count {
  font-size: 32px;
  font-weight: 800;
  color: #3b82f6;
  line-height: 1;
}

.seat-label {
  font-size: 14px;
  color: #6c757d;
  text-transform: lowercase;
}
</style>

