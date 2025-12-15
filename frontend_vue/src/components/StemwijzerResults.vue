<script setup>
import { computed, ref } from 'vue'
import { API_BASE_URL } from '@/config/api'

const props = defineProps({
  matchScores: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['reset'])

const topMatch = computed(() => props.matchScores[0])
const isLoggedIn = computed(() => localStorage.getItem('token') !== null)
const isSaving = ref(false)
const saveMessage = ref('')

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

    const response = await fetch(`${API_BASE_URL}/api/stemwijzer/favorite-party/${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        partyId: topMatch.value.party.id
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('Server error:', response.status, errorText)
      throw new Error(`Server error: ${response.status}`)
    }

    const data = await response.json()
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
</script>

<template>
  <div class="results-content">
    <div class="results-header">
      <h3 class="results-title">Jouw resultaten</h3>
      <p class="results-subtitle">De partijen die het beste bij jou passen</p>
    </div>

    <div v-if="topMatch" class="top-match-card">
      <div class="top-match-badge">Beste match</div>
      <div class="top-match-party" :style="{ borderLeftColor: topMatch.party.color }">
        <div class="top-match-info">
          <h4 class="top-match-name">{{ topMatch.party.name }}</h4>
          <div class="top-match-score">{{ topMatch.match }}% match</div>
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

    <div class="results-list">
      <div
        v-for="(match, index) in matchScores"
        :key="match.party.name"
        class="result-item"
        :style="{ borderLeftColor: match.party.color }"
      >
        <div class="result-rank">{{ index + 1 }}</div>
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
        <div class="result-score">{{ match.match }}%</div>
      </div>
    </div>

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
  margin-bottom: 40px;
}

.results-title {
  font-size: 36px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 12px;
}

.results-subtitle {
  font-size: 18px;
  color: #64748b;
}

.top-match-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid #3b82f6;
  margin-bottom: 32px;
}

.top-match-badge {
  display: inline-block;
  padding: 6px 16px;
  background: #3b82f6;
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
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
  font-size: 20px;
  font-weight: 700;
  color: #3b82f6;
}

.top-match-bar {
  width: 100%;
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.top-match-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s ease;
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
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: 2px solid #3b82f6;
  background: white;
  color: #3b82f6;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
}

.btn-favorite:hover:not(:disabled) {
  background: #3b82f6;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.btn-favorite:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.save-message {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  margin: 0;
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

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 20px;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.result-rank {
  font-size: 20px;
  font-weight: 700;
  color: #cbd5e1;
  min-width: 32px;
  text-align: center;
}

.result-info {
  flex: 1;
}

.result-party-name {
  font-size: 18px;
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
  color: #64748b;
  min-width: 60px;
  text-align: right;
}

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
}
</style>
