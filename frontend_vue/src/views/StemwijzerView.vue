<template>
  <div class="dashboard">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Stemwijzer</span>
        </div>
        <div class="header-info">
          <div class="header-top-row">
            <div class="header-left">
              <div class="election-badge">
                <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Stemwijzer 2025</span>
              </div>
              <h1 class="page-title">Stemwijzer</h1>
              <p class="page-description">Ontdek welke partij het beste bij jou past door stellingen te beantwoorden</p>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Content Container -->
    <div class="dashboard-container">
      <div class="stemwijzer-wrapper">
        <!-- Main Content -->
        <div class="stemwijzer-main">
          <!-- Category pills -->
          <div v-if="!showResults && !isLoading && !error" class="category-pills-container">
            <div class="category-pills">
              <span
                v-for="cat in uniqueCategories"
                :key="cat"
                class="category-pill"
                :class="{ active: currentQuestion?.category === cat }"
              >
                {{ cat }}
              </span>
            </div>
          </div>

          <!-- Question View -->
          <div v-if="!showResults && !isLoading && !error" class="stemwijzer-content">
            <StemwijzerProgress
              :current="currentQuestionIndex"
              :total="questions.length"
            />

            <!-- Stats bar -->
            <div class="stats-bar">
              <div class="stat">
                <span class="stat-value">{{ answeredCount }}</span>
                <span class="stat-label">beantwoord</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ questions.length - answeredCount }}</span>
                <span class="stat-label">te gaan</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ Math.round(progressPercentage) }}%</span>
                <span class="stat-label">voltooid</span>
              </div>
            </div>

            <StemwijzerQuestion
              :question="currentQuestion"
              :selected-answer="selectedAnswer"
              :selected-importance="selectedImportance"
              :can-go-back="currentQuestionIndex > 0"
              :is-last-question="isLastQuestion"
              @answer="answerQuestion"
              @go-back="goBack"
              @next="goNext"
            />
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>Resultaten worden berekend...</p>
            <p class="loading-detail">We analyseren uw {{ answeredCount }} antwoorden met gewichten...</p>
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="error-state">
            <p class="error-message">Er is een fout opgetreden: {{ error }}</p>
            <button @click="reset" class="btn btn-primary">Opnieuw proberen</button>
          </div>

          <!-- Results View -->
          <StemwijzerResults
            v-else-if="showResults"
            :match-scores="matchScores"
            :category-scores="categoryScores"
            :answers="answers"
            :questions="questions"
            @reset="reset"
          />
        </div>

        <!-- Sidebar -->
        <div class="stemwijzer-sidebar">
          <div class="sidebar-card">
            <h3>Over de Stemwijzer</h3>
            <p>
              De Stemwijzer helpt je om te ontdekken welke politieke partij het beste bij jouw standpunten past.
              Beantwoord de stellingen en geef aan hoe belangrijk elk onderwerp voor jou is.
            </p>
            <div class="sidebar-stats">
              <div class="stat">
                <span class="stat-value">{{ questions.length }}</span>
                <span class="stat-label">Stellingen</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ uniqueCategories.length }}</span>
                <span class="stat-label">Thema's</span>
              </div>
            </div>
          </div>

          <div class="sidebar-card">
            <h3>Hoe werkt het?</h3>
            <ul class="tips-list">
              <li>Geef aan hoe belangrijk het onderwerp voor je is</li>
              <li>Bekijk welke partijen het beste matchen</li>
              <li>Vergelijk je resultaten per thema</li>
              <li>Voeg na afloop je favoriete partij toe aan je profiel</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import StemwijzerService from '@/services/StemwijzerService.js'
import StemwijzerProgress from '@/components/StemwijzerProgress.vue'
import StemwijzerQuestion from '@/components/StemwijzerQuestion.vue'
import StemwijzerResults from '@/components/StemwijzerResults.vue'
import {
  stemwijzerQuestions,
  IMPORTANCE_LEVELS,
  ANSWER_SCORES,
  IMPORTANCE_WEIGHTS
} from '@/data/stemwijzerQuestions.js'

// Questions from data file
const questions = ref(stemwijzerQuestions)

const currentQuestionIndex = ref(0)
// answers now stores { answer: string, importance: string } per question
const answers = ref({})
const showResults = ref(false)
const matchScores = ref([])
const categoryScores = ref({})
const isLoading = ref(false)
const error = ref(null)

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const isLastQuestion = computed(() => currentQuestionIndex.value === questions.value.length - 1)
const selectedAnswer = computed(() => answers.value[currentQuestion.value?.id]?.answer)
const selectedImportance = computed(() => answers.value[currentQuestion.value?.id]?.importance || IMPORTANCE_LEVELS.BELANGRIJK)

// Calculate progress stats
const answeredCount = computed(() => Object.keys(answers.value).length)
const progressPercentage = computed(() => (answeredCount.value / questions.value.length) * 100)

async function answerQuestion({ answer, importance }) {
  answers.value[currentQuestion.value.id] = { answer, importance }
  if (isLastQuestion.value) {
    await calculateMatches()
  } else {
    currentQuestionIndex.value++
  }
}

async function calculateMatches() {
  isLoading.value = true
  error.value = null

  try {
    // Prepare weighted answers for API
    const weightedAnswers = {}
    for (const [questionId, data] of Object.entries(answers.value)) {
      weightedAnswers[questionId] = {
        answer: data.answer,
        importance: data.importance,
        answerScore: ANSWER_SCORES[data.answer],
        importanceWeight: IMPORTANCE_WEIGHTS[data.importance]
      }
    }

    const results = await StemwijzerService.calculateMatches(weightedAnswers)

    matchScores.value = results.partyScores.map(result => ({
      party: {
        id: result.partyId,
        name: result.partyName,
        color: result.partyColor
      },
      match: result.matchPercentage,
      score: result.matchPercentage
    }))

    // Store category breakdown if available
    if (results.categoryBreakdown) {
      categoryScores.value = results.categoryBreakdown
    }

    showResults.value = true
  } catch (err) {
    error.value = err.message || 'Er is een fout opgetreden bij het berekenen van de resultaten'
    console.error('Error calculating matches:', err)
  } finally {
    isLoading.value = false
  }
}

function goBack() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

function goNext() {
  if (!isLastQuestion.value) {
    currentQuestionIndex.value++
  }
}

function reset() {
  currentQuestionIndex.value = 0
  answers.value = {}
  showResults.value = false
  matchScores.value = []
  categoryScores.value = {}
  error.value = null
}

// Get unique categories for display
const uniqueCategories = computed(() => {
  const cats = new Set(questions.value.map(q => q.category))
  return Array.from(cats)
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', sans-serif;
}

/* Header - Same as other pages */
.dashboard-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.dashboard-header::before {
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

/* Dashboard Container */
.dashboard-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

/* Stemwijzer Layout */
.stemwijzer-wrapper {
  display: grid;
  grid-template-columns: 1fr 312px;
  gap: 24px;
  margin-top: 24px;
}

.stemwijzer-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Category Pills Container */
.category-pills-container {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.category-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-pill {
  padding: 6px 14px;
  background: #f1f5f9;
  color: #64748b;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.category-pill.active {
  background: #3b82f6;
  color: white;
}

/* Stemwijzer Content */
.stemwijzer-content {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

/* Stats bar - compact */
.stats-bar {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.stat {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 60px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  margin: 0 auto 24px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-state p {
  font-size: 18px;
  color: #64748b;
  font-weight: 500;
}

.loading-detail {
  font-size: 14px !important;
  margin-top: 8px;
}

/* Error State */
.error-state {
  text-align: center;
  padding: 60px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.error-message {
  font-size: 18px;
  color: #dc2626;
  margin-bottom: 24px;
  font-weight: 500;
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

/* Sidebar */
.stemwijzer-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.sidebar-card h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.sidebar-card p {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.sidebar-stats {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.sidebar-stats .stat {
  display: flex;
  flex-direction: column;
}

.sidebar-stats .stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.sidebar-stats .stat-label {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  padding: 8px 0;
  font-size: 14px;
  color: #64748b;
  border-bottom: 1px solid #f1f5f9;
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list li:before {
  content: '✓ ';
  color: #10b981;
  font-weight: 700;
  margin-right: 8px;
}

/* Responsive */
@media (max-width: 1024px) {
  .stemwijzer-wrapper {
    grid-template-columns: 1fr;
  }

  .stemwijzer-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    padding: 32px 20px 48px;
  }

  .page-title {
    font-size: 32px;
  }

  .page-description {
    font-size: 16px;
  }

  .header-top-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .dashboard-container {
    padding: 0 20px 40px;
  }

  .stemwijzer-wrapper {
    gap: 16px;
  }

  .stemwijzer-content {
    padding: 16px;
  }

  .stats-bar {
    gap: 16px;
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .dashboard-container {
    padding: 0 16px 32px;
  }

  .category-pills-container {
    padding: 12px;
  }

  .stemwijzer-content {
    padding: 12px;
  }
}
</style>

