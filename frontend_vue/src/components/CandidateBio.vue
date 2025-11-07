<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const candidate = ref(null)
const error = ref(null)
const loading = ref(true)

const API_BASE_URL =
  (location.origin === 'https://hva-frontend.onrender.com')
    ? 'https://hva-backend-c647.onrender.com'
    : 'http://localhost:8081'

const electionId = computed(() => route.params.electionId || 'TK2023')
const candidateId = computed(() => route.params.candidateId)

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId.value}/candidates/${candidateId.value}`)
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Kandidaat niet gevonden')
      }
      throw new Error('Failed to load candidate data')
    }
    candidate.value = await response.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

const fullName = computed(() => {
  if (!candidate.value) return ''
  const parts = []
  if (candidate.value.initials) parts.push(candidate.value.initials)
  if (candidate.value.firstName) parts.push(candidate.value.firstName)
  if (candidate.value.lastName) parts.push(candidate.value.lastName)
  return parts.join(' ')
})

const bio = computed(() => {
  if (!candidate.value) return ''

  const parts = []
  parts.push(`${fullName.value} is kandidaat voor de ${candidate.value.partyName || 'onbekende partij'}.`)

  if (candidate.value.residence) {
    parts.push(`De kandidaat woont in ${candidate.value.residence}.`)
  }

  if (candidate.value.votes !== undefined && candidate.value.votes > 0) {
    parts.push(`Tijdens de verkiezingen heeft ${fullName.value} ${candidate.value.votes.toLocaleString('nl-NL')} stemmen behaald.`)
  }

  if (candidate.value.candidateIdentifier) {
    parts.push(`De kandidaat heeft het kandidaatnummer ${candidate.value.candidateIdentifier}.`)
  }

  return parts.join(' ')
})

function goBack() {
  router.push('/Candidate')
}
</script>

<template>
  <div class="candidate-detail-page">
    <!-- Header with breadcrumb -->
    <header class="page-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <router-link to="/Candidate" class="breadcrumb-item">Kandidaten</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Kandidaat Detail</span>
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
              <h1 class="page-title" v-if="candidate">{{ fullName }}</h1>
              <h1 class="page-title" v-else>Kandidaat Detail</h1>
              <p class="page-description" v-if="candidate">
                Kandidaten informatie en biografie
              </p>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <div v-if="error" class="error-container">
        <div class="error-message">
          <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <p>{{ error }}</p>
          <button @click="goBack" class="back-button">Terug naar kandidatenlijst</button>
        </div>
      </div>

      <div v-if="loading && !error" class="loading-container">
        <div class="spinner"></div>
        <p class="loading-text">Kandidaatgegevens laden...</p>
      </div>

      <div v-if="candidate && !loading" class="candidate-detail-container">
        <div class="detail-card">
          <div class="detail-header">
            <div class="candidate-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <div class="candidate-title">
              <h2>{{ fullName }}</h2>
              <div class="party-badge" v-if="candidate.partyName">
                {{ candidate.partyName }}
              </div>
            </div>
          </div>

          <div class="detail-content">
            <section class="bio-section">
              <h3 class="section-title">
                <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                  <polyline points="10 9 9 9 8 9" />
                </svg>
                Biografie
              </h3>
              <p class="bio-text">{{ bio }}</p>
            </section>

            <section class="info-section">
              <h3 class="section-title">
                <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                Kandidaat Informatie
              </h3>
              <div class="info-grid">
                <div class="info-item" v-if="candidate.candidateIdentifier">
                  <span class="info-label">Kandidaatnummer:</span>
                  <span class="info-value">{{ candidate.candidateIdentifier }}</span>
                </div>
                <div class="info-item" v-if="candidate.partyName">
                  <span class="info-label">Partij:</span>
                  <span class="info-value">{{ candidate.partyName }}</span>
                </div>
                <div class="info-item" v-if="candidate.residence">
                  <span class="info-label">Woonplaats:</span>
                  <span class="info-value">{{ candidate.residence }}</span>
                </div>
                <div class="info-item" v-if="candidate.votes !== undefined">
                  <span class="info-label">Aantal stemmen:</span>
                  <span class="info-value votes-highlight">
                    {{ candidate.votes ? candidate.votes.toLocaleString('nl-NL') : '0' }}
                  </span>
                </div>
                <div class="info-item" v-if="candidate.id">
                  <span class="info-label">ID:</span>
                  <span class="info-value">{{ candidate.id }}</span>
                </div>
              </div>
            </section>
          </div>

          <div class="detail-footer">
            <button @click="goBack" class="back-button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6" />
              </svg>
              Terug naar kandidatenlijst
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.candidate-detail-page {
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

</style>
