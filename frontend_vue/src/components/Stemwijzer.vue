<script setup>
import { ref, computed, defineOptions } from 'vue'
import StemwijzerService from '@/services/StemwijzerService.js'
import StemwijzerProgress from './StemwijzerProgress.vue'
import StemwijzerQuestion from './StemwijzerQuestion.vue'
import StemwijzerResults from './StemwijzerResults.vue'

defineOptions({
  name: 'StemwijzerComponent'
})

// Questions for the stemwijzer (voting guide)
const questions = ref([
  {
    id: 1,
    question: 'Nederland moet meer doen om klimaatverandering tegen te gaan, ook als dit hogere kosten betekent.',
    category: 'Klimaat'
  },
  {
    id: 2,
    question: 'De immigratie naar Nederland moet worden beperkt.',
    category: 'Immigratie'
  },
  {
    id: 3,
    question: 'De zorgkosten moeten worden verlaagd, ook als dit betekent dat sommige behandelingen niet meer worden vergoed.',
    category: 'Zorg'
  },
  {
    id: 4,
    question: 'Er moeten meer betaalbare woningen worden gebouwd, ook als dit betekent dat er minder ruimte is voor natuur.',
    category: 'Woningmarkt'
  },
  {
    id: 5,
    question: 'Het minimumloon moet worden verhoogd.',
    category: 'Economie'
  },
  {
    id: 6,
    question: 'Nederland moet meer samenwerken met andere Europese landen.',
    category: 'Europa'
  },
  {
    id: 7,
    question: 'Er moet meer geld naar het onderwijs, ook als dit betekent dat andere uitgaven moeten worden verlaagd.',
    category: 'Onderwijs'
  },
  {
    id: 8,
    question: 'De belastingen voor bedrijven moeten worden verlaagd om de economie te stimuleren.',
    category: 'Economie'
  },
  {
    id: 9,
    question: 'Nederland moet meer doen om de biodiversiteit te beschermen.',
    category: 'Natuur'
  },
  {
    id: 10,
    question: 'Er moet meer worden geïnvesteerd in defensie en veiligheid.',
    category: 'Veiligheid'
  }
])

const currentQuestionIndex = ref(0)
const answers = ref({})
const showResults = ref(false)
const matchScores = ref([])
const isLoading = ref(false)
const error = ref(null)

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const isLastQuestion = computed(() => currentQuestionIndex.value === questions.value.length - 1)
const selectedAnswer = computed(() => answers.value[currentQuestion.value?.id])

async function answerQuestion(answer) {
  answers.value[currentQuestion.value.id] = answer
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
    const results = await StemwijzerService.calculateMatches(answers.value)

    matchScores.value = results.map(result => ({
      party: {
        id: result.partyId,
        name: result.partyName,
        color: result.partyColor
      },
      match: result.matchPercentage,
      score: result.matchPercentage
    }))

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
  error.value = null
}
</script>

<template>
  <section class="stemwijzer-section">
    <div class="stemwijzer-container">
      <div class="stemwijzer-header">
        <div class="header-icon">🗳️</div>
        <h2 class="section-title">Stemwijzer 2025</h2>
        <p class="section-subtitle">
          Ontdek welke partij het beste bij jou past door 10 stellingen te beantwoorden
        </p>
      </div>

      <!-- Question View -->
      <div v-if="!showResults && !isLoading && !error" class="stemwijzer-content">
        <StemwijzerProgress
          :current="currentQuestionIndex"
          :total="questions.length"
        />
        <StemwijzerQuestion
          :question="currentQuestion"
          :selected-answer="selectedAnswer"
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

.header-icon {
  font-size: 48px;
  margin-bottom: 16px;
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
  margin: 0 auto;
}

/* Responsive */
@media (max-width: 768px) {
  .stemwijzer-section {
    padding: 60px 16px;
  }

  .section-title {
    font-size: 32px;
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
</style>
