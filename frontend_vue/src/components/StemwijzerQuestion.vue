<script setup>
import { ref, watch } from 'vue'
import { ANSWER_OPTIONS, IMPORTANCE_LEVELS } from '@/data/stemwijzerQuestions.js'

const props = defineProps({
  question: {
    type: Object,
    required: true
  },
  selectedAnswer: {
    type: String,
    default: null
  },
  selectedImportance: {
    type: String,
    default: IMPORTANCE_LEVELS.BELANGRIJK
  },
  canGoBack: {
    type: Boolean,
    default: false
  },
  isLastQuestion: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['answer', 'go-back', 'next'])

const currentAnswer = ref(props.selectedAnswer)
const currentImportance = ref(props.selectedImportance || IMPORTANCE_LEVELS.BELANGRIJK)

// Watch for prop changes when navigating between questions
watch(() => props.selectedAnswer, (newVal) => {
  currentAnswer.value = newVal
})

watch(() => props.selectedImportance, (newVal) => {
  currentImportance.value = newVal || IMPORTANCE_LEVELS.BELANGRIJK
})

function selectAnswer(answer) {
  currentAnswer.value = answer
}

function selectImportance(importance) {
  currentImportance.value = importance
}

function submitAnswer() {
  if (currentAnswer.value) {
    emit('answer', {
      answer: currentAnswer.value,
      importance: currentImportance.value
    })
  }
}

function handleGoBack() {
  emit('go-back')
}

const answerOptions = [
  { value: ANSWER_OPTIONS.HELEMAAL_EENS, label: 'Helemaal eens', icon: '✓✓', class: 'helemaal-eens' },
  { value: ANSWER_OPTIONS.EENS, label: 'Eens', icon: '✓', class: 'eens' },
  { value: ANSWER_OPTIONS.NEUTRAAL, label: 'Neutraal', icon: '—', class: 'neutraal' },
  { value: ANSWER_OPTIONS.ONEENS, label: 'Oneens', icon: '✗', class: 'oneens' },
  { value: ANSWER_OPTIONS.HELEMAAL_ONEENS, label: 'Helemaal oneens', icon: '✗✗', class: 'helemaal-oneens' }
]

const importanceOptions = [
  { value: IMPORTANCE_LEVELS.MINDER_BELANGRIJK, label: 'Minder belangrijk', stars: 1 },
  { value: IMPORTANCE_LEVELS.BELANGRIJK, label: 'Belangrijk', stars: 2 },
  { value: IMPORTANCE_LEVELS.ZEER_BELANGRIJK, label: 'Zeer belangrijk', stars: 3 }
]
</script>

<template>
  <div class="question-card">
    <div class="question-meta">
      <span class="question-category">{{ question.category }}</span>
      <span v-if="question.subcategory" class="question-subcategory">{{ question.subcategory }}</span>
    </div>
    <h3 class="question-text">{{ question.question }}</h3>

    <!-- 5-punt antwoordschaal -->
    <div class="answer-section">
      <p class="section-label">Wat is uw mening?</p>
      <div class="answer-buttons">
        <button
          v-for="option in answerOptions"
          :key="option.value"
          @click="selectAnswer(option.value)"
          class="answer-btn"
          :class="[`answer-btn-${option.class}`, { active: currentAnswer === option.value }]"
        >
          <span class="answer-icon">{{ option.icon }}</span>
          <span class="answer-label">{{ option.label }}</span>
        </button>
      </div>
    </div>

    <!-- Importantie/Gewicht sectie -->
    <div class="importance-section" :class="{ visible: currentAnswer }">
      <p class="section-label">Hoe belangrijk is dit onderwerp voor u?</p>
      <div class="importance-buttons">
        <button
          v-for="option in importanceOptions"
          :key="option.value"
          @click="selectImportance(option.value)"
          class="importance-btn"
          :class="{ active: currentImportance === option.value }"
        >
          <span class="importance-stars">
            <span v-for="n in 3" :key="n" class="star" :class="{ filled: n <= option.stars }">★</span>
          </span>
          <span class="importance-label">{{ option.label }}</span>
        </button>
      </div>
    </div>

    <!-- Navigatie -->
    <div class="question-navigation">
      <button
        @click="handleGoBack"
        class="nav-btn nav-btn-back"
        :disabled="!canGoBack"
      >
        <svg viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
        </svg>
        Vorige
      </button>

      <button
        v-if="currentAnswer"
        @click="submitAnswer"
        class="nav-btn nav-btn-next"
        :class="{ 'nav-btn-finish': isLastQuestion }"
      >
        {{ isLastQuestion ? 'Bekijk resultaten' : 'Volgende' }}
        <svg v-if="!isLastQuestion" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
        <svg v-else viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.question-card {
  background: white;
  padding: 48px 40px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.question-category {
  display: inline-block;
  padding: 6px 16px;
  background: #3b82f6;
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.question-subcategory {
  display: inline-block;
  padding: 6px 16px;
  background: #f1f5f9;
  color: #64748b;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.question-text {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 32px;
  line-height: 1.4;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 5-punt antwoordschaal */
.answer-section {
  margin-bottom: 32px;
}

.answer-buttons {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.answer-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Nunito', sans-serif;
}

.answer-btn:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.answer-icon {
  font-size: 20px;
  font-weight: bold;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.05);
}

.answer-label {
  text-align: center;
  line-height: 1.2;
}

/* Helemaal eens - donkergroen */
.answer-btn-helemaal-eens {
  color: #047857;
}
.answer-btn-helemaal-eens.active {
  background: #d1fae5;
  border-color: #047857;
}
.answer-btn-helemaal-eens.active .answer-icon {
  background: #047857;
  color: white;
}

/* Eens - lichtgroen */
.answer-btn-eens {
  color: #059669;
}
.answer-btn-eens.active {
  background: #ecfdf5;
  border-color: #059669;
}
.answer-btn-eens.active .answer-icon {
  background: #059669;
  color: white;
}

/* Neutraal - grijs */
.answer-btn-neutraal {
  color: #64748b;
}
.answer-btn-neutraal.active {
  background: #f1f5f9;
  border-color: #64748b;
}
.answer-btn-neutraal.active .answer-icon {
  background: #64748b;
  color: white;
}

/* Oneens - lichtrood */
.answer-btn-oneens {
  color: #dc2626;
}
.answer-btn-oneens.active {
  background: #fef2f2;
  border-color: #dc2626;
}
.answer-btn-oneens.active .answer-icon {
  background: #dc2626;
  color: white;
}

/* Helemaal oneens - donkerrood */
.answer-btn-helemaal-oneens {
  color: #b91c1c;
}
.answer-btn-helemaal-oneens.active {
  background: #fee2e2;
  border-color: #b91c1c;
}
.answer-btn-helemaal-oneens.active .answer-icon {
  background: #b91c1c;
  color: white;
}

/* Importantie sectie */
.importance-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
  opacity: 0.4;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.importance-section.visible {
  opacity: 1;
  pointer-events: auto;
}

.importance-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.importance-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Nunito', sans-serif;
}

.importance-btn:hover {
  border-color: #fbbf24;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.importance-btn.active {
  border-color: #f59e0b;
  background: #fffbeb;
}

.importance-stars {
  display: flex;
  gap: 4px;
}

.star {
  font-size: 24px;
  color: #e2e8f0;
  transition: color 0.2s ease;
}

.star.filled {
  color: #f59e0b;
}

.importance-btn.active .star.filled {
  color: #d97706;
}

.importance-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.importance-btn.active .importance-label {
  color: #92400e;
}

/* Navigatie */
.question-navigation {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #64748b;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Nunito', sans-serif;
}

.nav-btn:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #f8fafc;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-btn svg {
  width: 20px;
  height: 20px;
}

.nav-btn-next {
  margin-left: auto;
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

.nav-btn-next:hover:not(:disabled) {
  background: #2563eb;
  border-color: #2563eb;
  color: white;
}

.nav-btn-finish {
  background: #059669;
  border-color: #059669;
}

.nav-btn-finish:hover:not(:disabled) {
  background: #047857;
  border-color: #047857;
  color: white;
}

/* Responsive */
@media (max-width: 768px) {
  .question-card {
    padding: 32px 24px;
  }

  .question-text {
    font-size: 20px;
  }

  .answer-buttons {
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }

  .answer-btn {
    padding: 12px 4px;
    font-size: 11px;
  }

  .answer-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .importance-buttons {
    grid-template-columns: 1fr;
  }

  .importance-btn {
    flex-direction: row;
    justify-content: flex-start;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .answer-buttons {
    grid-template-columns: repeat(3, 1fr);
  }

  .answer-buttons .answer-btn:nth-child(4),
  .answer-buttons .answer-btn:nth-child(5) {
    grid-column: span 1;
  }

  /* Stack last two buttons in a new row */
  .answer-buttons {
    grid-template-columns: repeat(6, 1fr);
  }

  .answer-btn:nth-child(1) { grid-column: 1 / 3; }
  .answer-btn:nth-child(2) { grid-column: 3 / 5; }
  .answer-btn:nth-child(3) { grid-column: 5 / 7; }
  .answer-btn:nth-child(4) { grid-column: 1 / 4; }
  .answer-btn:nth-child(5) { grid-column: 4 / 7; }
}
</style>
