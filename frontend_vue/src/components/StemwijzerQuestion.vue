<script setup>
defineProps({
  question: {
    type: Object,
    required: true
  },
  selectedAnswer: {
    type: String,
    default: null
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

function handleAnswer(answer) {
  emit('answer', answer)
}

function handleGoBack() {
  emit('go-back')
}

function handleNext() {
  emit('next')
}
</script>

<template>
  <div class="question-card">
    <div class="question-category">{{ question.category }}</div>
    <h3 class="question-text">{{ question.question }}</h3>

    <div class="answer-buttons">
      <button
        @click="handleAnswer('eens')"
        class="answer-btn answer-btn-eens"
        :class="{ active: selectedAnswer === 'eens' }"
      >
        <span class="answer-icon">✓</span>
        <span>Eens</span>
      </button>

      <button
        @click="handleAnswer('oneens')"
        class="answer-btn answer-btn-oneens"
        :class="{ active: selectedAnswer === 'oneens' }"
      >
        <span class="answer-icon">✗</span>
        <span>Oneens</span>
      </button>

      <button
        @click="handleAnswer('geen-mening')"
        class="answer-btn answer-btn-neutraal"
        :class="{ active: selectedAnswer === 'geen-mening' }"
      >
        <span class="answer-icon">—</span>
        <span>Geen mening</span>
      </button>
    </div>

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
        v-if="selectedAnswer && !isLastQuestion"
        @click="handleNext"
        class="nav-btn nav-btn-next"
      >
        Volgende
        <svg viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
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

.question-category {
  display: inline-block;
  padding: 6px 16px;
  background: #f1f5f9;
  color: #3b82f6;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 24px;
}

.question-text {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 40px;
  line-height: 1.4;
}

.answer-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.answer-btn {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  font-family: 'Nunito', sans-serif;
}

.answer-btn:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.answer-btn-eens {
  color: #059669;
}

.answer-btn-eens.active {
  background: #ecfdf5;
  border-color: #059669;
  color: #059669;
}

.answer-btn-oneens {
  color: #dc2626;
}

.answer-btn-oneens.active {
  background: #fef2f2;
  border-color: #dc2626;
  color: #dc2626;
}

.answer-btn-neutraal {
  color: #64748b;
}

.answer-btn-neutraal.active {
  background: #f1f5f9;
  border-color: #64748b;
  color: #64748b;
}

.answer-icon {
  font-size: 24px;
  font-weight: bold;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
}

.answer-btn-eens.active .answer-icon {
  background: #059669;
  color: white;
}

.answer-btn-oneens.active .answer-icon {
  background: #dc2626;
  color: white;
}

.answer-btn-neutraal.active .answer-icon {
  background: #64748b;
  color: white;
}

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
}

@media (max-width: 768px) {
  .question-card {
    padding: 32px 24px;
  }

  .question-text {
    font-size: 20px;
  }

  .answer-btn {
    padding: 16px 20px;
    font-size: 16px;
  }
}
</style>
