<script setup>
import { computed, ref } from 'vue'
import StemwijzerService from '@/services/StemwijzerService.js'
import { IMPORTANCE_LEVELS, CATEGORIES } from '@/data/stemwijzerQuestions.js'

const props = defineProps({
  matchScores: {
    type: Array,
    required: true
  },
  categoryScores: {
    type: Object,
    default: () => ({})
  },
  answers: {
    type: Object,
    default: () => ({})
  },
  questions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['reset'])

const topMatch = computed(() => props.matchScores[0])
const isLoggedIn = computed(() => localStorage.getItem('token') !== null)
const isSaving = ref(false)
const saveMessage = ref('')
const selectedPartyForDetails = ref(null)

// Calculate importance stats
const importanceStats = computed(() => {
  const stats = {
    zeerBelangrijk: 0,
    belangrijk: 0,
    minderBelangrijk: 0
  }

  for (const data of Object.values(props.answers)) {
    if (data.importance === IMPORTANCE_LEVELS.ZEER_BELANGRIJK) stats.zeerBelangrijk++
    else if (data.importance === IMPORTANCE_LEVELS.BELANGRIJK) stats.belangrijk++
    else stats.minderBelangrijk++
  }

  return stats
})

// Group questions by category
const questionsByCategory = computed(() => {
  const grouped = {}
  for (const q of props.questions) {
    if (!grouped[q.category]) {
      grouped[q.category] = []
    }
    grouped[q.category].push(q)
  }
  return grouped
})

// Calculate category match for display (simplified version)
const categoryBreakdown = computed(() => {
  if (Object.keys(props.categoryScores).length > 0) {
    return props.categoryScores
  }

  // Fallback: calculate locally if not provided by API
  const breakdown = {}
  for (const category of Object.keys(questionsByCategory.value)) {
    breakdown[category] = {
      questionsCount: questionsByCategory.value[category].length,
      importantCount: questionsByCategory.value[category].filter(
        q => props.answers[q.id]?.importance === IMPORTANCE_LEVELS.ZEER_BELANGRIJK
      ).length
    }
  }
  return breakdown
})

async function saveFavoriteParty() {
  if (!topMatch.value || !isLoggedIn.value) {
    return
  }

  isSaving.value = true
  saveMessage.value = ''

  try {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      saveMessage.value = 'Gebruiker niet gevonden'
      return
    }

    await StemwijzerService.setFavoriteParty(userId, topMatch.value.party.id)
    saveMessage.value = 'Favoriete partij opgeslagen!'

    // Update localStorage userData
    const userData = JSON.parse(localStorage.getItem('userData') || '{}')
    userData.favoriteParty = topMatch.value.party.id
    localStorage.setItem('userData', JSON.stringify(userData))
  } catch (err) {
    if (err.message && err.message.includes('Failed to fetch')) {
      saveMessage.value = 'Kan geen verbinding maken met de server'
    } else {
      saveMessage.value = 'Fout bij opslaan: ' + (err.message || 'Onbekende fout')
    }
    console.error('Error saving favorite party:', err)
  } finally {
    isSaving.value = false
  }
}

function selectPartyForDetails(party) {
  if (selectedPartyForDetails.value?.id === party.id) {
    selectedPartyForDetails.value = null
  } else {
    selectedPartyForDetails.value = party
  }
}

function getMatchColor(percentage) {
  if (percentage >= 75) return '#10b981'
  if (percentage >= 50) return '#f59e0b'
  if (percentage >= 25) return '#f97316'
  return '#ef4444'
}
</script>

<template>
  <div class="results-content">
    <div class="results-header">
      <h3 class="results-title">Jouw resultaten</h3>
      <p class="results-subtitle">Gebaseerd op {{ Object.keys(answers).length }} stellingen met gewogen antwoorden</p>
    </div>

    <!-- Importance summary -->
    <div class="importance-summary">
      <div class="importance-stat">
        <span class="importance-icon">★★★</span>
        <span class="importance-count">{{ importanceStats.zeerBelangrijk }}</span>
        <span class="importance-label">zeer belangrijk</span>
      </div>
      <div class="importance-stat">
        <span class="importance-icon">★★☆</span>
        <span class="importance-count">{{ importanceStats.belangrijk }}</span>
        <span class="importance-label">belangrijk</span>
      </div>
      <div class="importance-stat">
        <span class="importance-icon">★☆☆</span>
        <span class="importance-count">{{ importanceStats.minderBelangrijk }}</span>
        <span class="importance-label">minder belangrijk</span>
      </div>
    </div>

    <!-- Top match card -->
    <div v-if="topMatch" class="top-match-card">
      <div class="top-match-badge">Beste match</div>
      <div class="top-match-party" :style="{ borderLeftColor: topMatch.party.color }">
        <div class="top-match-info">
          <h4 class="top-match-name">{{ topMatch.party.name }}</h4>
          <div class="top-match-score" :style="{ color: getMatchColor(topMatch.match) }">
            {{ topMatch.match }}% match
          </div>
        </div>
        <div class="top-match-bar">
          <div
            class="top-match-fill"
            :style="{
              width: `${topMatch.match}%`,
              backgroundColor: topMatch.party.color
            }"
          ></div>
        </div>

        <!-- Match quality indicator -->
        <div class="match-quality">
          <span v-if="topMatch.match >= 80" class="quality-badge excellent">Uitstekende match!</span>
          <span v-else-if="topMatch.match >= 60" class="quality-badge good">Goede match</span>
          <span v-else-if="topMatch.match >= 40" class="quality-badge moderate">Redelijke match</span>
          <span v-else class="quality-badge low">Lage match</span>
        </div>

        <div class="top-match-actions">
          <div v-if="isLoggedIn">
            <button
              @click="saveFavoriteParty"
              class="btn-favorite"
              :disabled="isSaving"
            >
              <span v-if="!isSaving">⭐ Als favoriet instellen</span>
              <span v-else>Opslaan...</span>
            </button>
            <p v-if="saveMessage" class="save-message" :class="{ success: saveMessage.includes('opgeslagen') }">
              {{ saveMessage }}
            </p>
          </div>
          <div v-else class="login-prompt">
            <p class="login-prompt-text">
              <router-link to="/login" class="login-link">Log in</router-link> om deze partij toe te voegen als uw favoriete partij
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Category breakdown panel -->
    <div class="category-breakdown">
      <h4 class="breakdown-title">Jouw prioriteiten per categorie</h4>
      <div class="category-grid">
        <div
          v-for="(data, category) in categoryBreakdown"
          :key="category"
          class="category-item"
        >
          <div class="category-header">
            <span class="category-name">{{ category }}</span>
            <span v-if="data.importantCount > 0" class="category-important">
              {{ data.importantCount }}× ★★★
            </span>
          </div>
          <div class="category-questions">
            {{ data.questionsCount }} {{ data.questionsCount === 1 ? 'vraag' : 'vragen' }}
          </div>
        </div>
      </div>
    </div>

    <!-- All results list -->
    <div class="results-list">
      <h4 class="list-title">Alle partijen</h4>
      <div
        v-for="(match, index) in matchScores"
        :key="match.party.name"
        class="result-item"
        :class="{ 'top-three': index < 3, expanded: selectedPartyForDetails?.id === match.party.id }"
        :style="{ borderLeftColor: match.party.color }"
        @click="selectPartyForDetails(match.party)"
      >
        <div class="result-main">
          <div class="result-rank" :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
            <span v-if="index === 0">🥇</span>
            <span v-else-if="index === 1">🥈</span>
            <span v-else-if="index === 2">🥉</span>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="result-info">
            <div class="result-party-name">{{ match.party.name }}</div>
            <div class="result-bar">
              <div
                class="result-fill"
                :style="{
                  width: `${match.match}%`,
                  backgroundColor: match.party.color
                }"
              ></div>
            </div>
          </div>
          <div class="result-score" :style="{ color: getMatchColor(match.match) }">
            {{ match.match }}%
          </div>
        </div>

        <!-- Expanded details (placeholder for future enhancement) -->
        <div v-if="selectedPartyForDetails?.id === match.party.id" class="result-details">
          <p class="details-hint">
            Klik op een andere partij om te vergelijken, of
            <router-link :to="`/candidates?party=${match.party.id}`" class="details-link">
              bekijk kandidaten
            </router-link>
          </p>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="results-actions">
      <button @click="$emit('reset')" class="btn btn-primary">
        Opnieuw beginnen
      </button>
    </div>
  </div>
</template>

<style scoped>
.results-content {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.results-header {
  text-align: center;
  margin-bottom: 32px;
}

.results-title {
  font-size: 36px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 12px;
}

.results-subtitle {
  font-size: 16px;
  color: #64748b;
}

/* Importance summary */
.importance-summary {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  margin-bottom: 32px;
}

.importance-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.importance-icon {
  font-size: 16px;
  color: #f59e0b;
}

.importance-count {
  font-size: 24px;
  font-weight: 700;
  color: #92400e;
}

.importance-label {
  font-size: 12px;
  color: #a16207;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Top match card */
.top-match-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid #10b981;
  margin-bottom: 24px;
}

.top-match-badge {
  display: inline-block;
  padding: 8px 20px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 20px;
}

.top-match-party {
  border-left: 4px solid;
  padding-left: 24px;
}

.top-match-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.top-match-name {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.top-match-score {
  font-size: 24px;
  font-weight: 800;
}

.top-match-bar {
  width: 100%;
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
}

.top-match-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s ease;
}

/* Match quality indicator */
.match-quality {
  margin-bottom: 16px;
}

.quality-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.quality-badge.excellent {
  background: #d1fae5;
  color: #047857;
}

.quality-badge.good {
  background: #dbeafe;
  color: #1d4ed8;
}

.quality-badge.moderate {
  background: #fef3c7;
  color: #92400e;
}

.quality-badge.low {
  background: #fee2e2;
  color: #b91c1c;
}

.top-match-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.btn-favorite {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  border: 2px solid #10b981;
  background: white;
  color: #10b981;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
}

.btn-favorite:hover:not(:disabled) {
  background: #10b981;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.btn-favorite:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.save-message {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  margin: 8px 0 0;
  text-align: center;
}

.save-message.success {
  color: #059669;
}

.login-prompt {
  text-align: center;
}

.login-prompt-text {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.login-link {
  color: #3b82f6;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #2563eb;
  text-decoration: underline;
}

/* Category breakdown */
.category-breakdown {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.breakdown-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
  text-align: center;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.category-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.category-important {
  font-size: 11px;
  color: #f59e0b;
  font-weight: 600;
}

.category-questions {
  font-size: 12px;
  color: #64748b;
}

/* Results list */
.results-list {
  margin-bottom: 32px;
}

.list-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
}

.result-item {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  overflow: hidden;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.result-item.top-three {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.result-main {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
}

.result-rank {
  font-size: 18px;
  font-weight: 700;
  color: #cbd5e1;
  min-width: 40px;
  text-align: center;
}

.result-rank.gold { color: #f59e0b; }
.result-rank.silver { color: #94a3b8; }
.result-rank.bronze { color: #d97706; }

.result-info {
  flex: 1;
}

.result-party-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.result-bar {
  width: 100%;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.result-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.8s ease;
}

.result-score {
  font-size: 18px;
  font-weight: 700;
  min-width: 60px;
  text-align: right;
}

/* Expanded details */
.result-details {
  padding: 16px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.details-hint {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.details-link {
  color: #3b82f6;
  font-weight: 600;
  text-decoration: none;
}

.details-link:hover {
  text-decoration: underline;
}

/* Actions */
.results-actions {
  text-align: center;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

/* Responsive */
@media (max-width: 768px) {
  .results-title {
    font-size: 28px;
  }

  .top-match-card {
    padding: 24px;
  }

  .top-match-name {
    font-size: 20px;
  }

  .top-match-score {
    font-size: 20px;
  }

  .importance-summary {
    flex-direction: column;
    gap: 16px;
  }

  .importance-stat {
    flex-direction: row;
    gap: 12px;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
