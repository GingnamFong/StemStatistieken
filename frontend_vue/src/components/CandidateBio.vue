<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CandidateService } from '@/services/CandidateService'

const route = useRoute()
const router = useRouter()

const candidate = ref(null)
const error = ref(null)
const loading = ref(true)

const selectedYear = computed(() => {
  const yearParam = route.query.year
  if (yearParam && ['2021', '2023', '2025'].includes(yearParam)) {
    return parseInt(yearParam)
  }
  return 2023 // default
})

const electionId = computed(() => `TK${selectedYear.value}`)
const candidateId = computed(() => route.params.candidateId)

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    candidate.value = await CandidateService.getCandidate(electionId.value, candidateId.value)
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
  parts.push(`${fullName.value} is kandidaat voor ${candidate.value.partyName || 'onbekende partij'}.`)

  if (candidate.value.residence) {
    parts.push(`De kandidaat woont in ${candidate.value.residence}.`)
  }

  if (candidate.value.votes !== undefined && candidate.value.votes > 0) {
    parts.push(`Tijdens de verkiezingen heeft ${fullName.value} ${candidate.value.votes.toLocaleString('nl-NL')} stemmen behaald.`)
  }

  if (candidate.value.candidateIdentifier) {
    parts.push(`De kandidaat heeft kandidaatnummer: ${candidate.value.candidateIdentifier}.`)
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

                <span>Tweede Kamer {{ selectedYear }}</span>
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

/* Page Container */
.page-container {
  max-width: 1000px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

.error-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 48px 24px;
  margin-top: 24px;
}

.error-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
  color: #e74c3c;
}

.error-icon {
  width: 64px;
  height: 64px;
  color: #e74c3c;
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

.candidate-detail-container {
  margin-top: 24px;
}

.detail-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.detail-header {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 24px;
  color: white;
}

.candidate-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.candidate-avatar svg {
  width: 48px;
  height: 48px;
  color: white;
}

.candidate-title {
  flex: 1;
}

.candidate-title h2 {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
}

.party-badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.detail-content {
  padding: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
}

.section-icon {
  width: 24px;
  height: 24px;
  color: #3b82f6;
}

.bio-section {
  margin-bottom: 40px;
}

.bio-text {
  font-size: 16px;
  line-height: 1.8;
  color: #475569;
  margin: 0;
}

.info-section {
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.info-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.info-value.votes-highlight {
  color: #3b82f6;
  font-size: 20px;
}

.detail-footer {
  padding: 24px 40px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #1e293b;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}

.back-button:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.back-button svg {
  width: 20px;
  height: 20px;
}

/* Responsive Design */
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

  .page-container {
    padding: 0 20px 40px;
  }

  .detail-header {
    flex-direction: column;
    text-align: center;
    padding: 32px 24px;
  }

  .candidate-title h2 {
    font-size: 24px;
  }

  .detail-content {
    padding: 24px;
  }

  .section-title {
    font-size: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .detail-footer {
    padding: 20px 24px;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .detail-header {
    padding: 24px 20px;
  }

  .detail-content {
    padding: 20px;
  }

  .bio-text {
    font-size: 15px;
  }
}
</style>

