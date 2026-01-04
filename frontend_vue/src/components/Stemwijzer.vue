<script setup>
import { ref, computed, defineOptions } from 'vue'
import StemwijzerService from '@/services/StemwijzerService.js'
import StemwijzerProgress from './StemwijzerProgress.vue'
import StemwijzerQuestion from './StemwijzerQuestion.vue'
import StemwijzerResults from './StemwijzerResults.vue'
import {
  stemwijzerQuestions,
  IMPORTANCE_LEVELS,
  ANSWER_SCORES,
  IMPORTANCE_WEIGHTS
} from '@/data/stemwijzerQuestions.js'

defineOptions({
  name: 'StemwijzerComponent'
})

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

<template>
  <section class="stemwijzer-section">
    <div class="stemwijzer-container">
      <div class="stemwijzer-header">
        <h2 class="section-title">Stemwijzer 2025</h2>
        <p class="section-subtitle">
          Ontdek welke partij het beste bij jou past door {{ questions.length }} stellingen te beantwoorden
        </p>

        <!-- Category pills -->
        <div v-if="!showResults && !isLoading && !error" class="category-pills">
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
  </section>
</template>

<style scoped>
.stemwijzer-section {
  padding: 100px 24px;
  background: linear-gradient(to bottom, #f8fafc 0%, #ffffff 100%);
}

.stemwijzer-container {
  max-width: 800px;
  margin: 0 auto;
}

.stemwijzer-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 42px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.section-subtitle {
  font-size: 18px;
  color: #64748b;
  max-width: 600px;
  margin: 0 auto 24px;
}

/* Category pills */
.category-pills {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
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

/* Stats bar */
.stats-bar {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Responsive */
@media (max-width: 768px) {
  .stemwijzer-section {
    padding: 60px 16px;
  }

  .section-title {
    font-size: 32px;
  }

  .stats-bar {
    gap: 16px;
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 60px 24px;
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
</style>
