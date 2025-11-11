<template>
  <div>

    <div id="onboarding-modal" class="onboarding-modal">
      <div class="modal-content">
        <div v-show="currentStep === 0" class="step">
          <h2>Welkom op uitslagen.nl!</h2>
          <p>Laten we beginnen met een korte rondleiding!</p>
          <button @click="nextStep">Volgende</button>
        </div>

        <div v-show="currentStep === 1" class="step">
          <h2>Uitslagen</h2>
          <p>Op deze website kunt u de landelijke uitslagen zien van de verkiezingen!</p>
          <button @click="nextStep">Volgende</button>
        </div>

        <div v-show="currentStep === 2" class="step">
          <h2>Ontdekken maar!</h2>
          <p>U bent er helemaal klaar voor!</p>
          <button @click="finish">Sluiten</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentStep = ref(0)
// onboarding guide starts at page 0
const completed = ref(false)

onMounted(() => {
  if (localStorage.getItem("onboardingComplete")) {
    completed.value = true
  } else {
    document.getElementById("onboarding-modal").style.display = "flex"
  }
})

function nextStep() {
  currentStep.value++
}

function finish() {
  completed.value = true
  // if true, the onboarding guide will not be shown to the user
  document.getElementById("onboarding-modal").style.display = "none"
  localStorage.setItem("onboardingComplete", "true")
  // Shows the onboarding guide if it's saved in local storage or not
}
// test
</script>

<style scoped>
/* Typography */
h2 {
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 8px 0;
}

p {
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 16px;
  color: #475569;
  margin: 0;
}

/* Overlay */
.onboarding-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: none;
  justify-content: center;
  align-items: center;
  z-index: 1100; /* above navbar */
  padding: 16px;
}

/* Modal box */
.modal-content {
  background: #ffffff;
  padding: 24px 24px 20px 24px;
  border-radius: 16px;
  width: 100%;
  max-width: 440px;
  text-align: center;
  box-shadow: 0 12px 32px rgba(0,0,0,0.15);
  border: 1px solid #e2e8f0;
}

.step { margin: 8px 0; }

.modal-content button {
  margin-top: 18px;
  padding: 10px 18px;
  background-color: #3b82f6;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-family: 'Nunito', sans-serif;
  font-size: 15px;
  font-weight: 700;
  transition: transform 0.15s ease, box-shadow 0.2s ease, background 0.2s ease;
  box-shadow: 0 2px 8px rgba(59,130,246,0.25);
}

.modal-content button:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(59,130,246,0.30);
}

.modal-content button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(59,130,246,0.25);
}
</style>


