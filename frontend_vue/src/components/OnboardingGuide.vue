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
h1 {
  color: grey;
}

h2 {
  font-family: "SansSerif";
  font-size: 40px;
}

p {
  font-family: "SansSerif";
  font-size: 20px;
}

/* Overlay */
.onboarding-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.6);
  display: none;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

/* Modal box */
.modal-content {
  background: #fff;
  padding: 30px;
  border-radius: 18px;
  width: 400px;
  text-align: center;
  box-shadow: 0 5px 20px rgba(0,0,0,0.3);
  font-family: Arial, sans-serif;
}

.modal-content button {
  margin-top: 20px;
  padding: 12px 20px;
  background-color: #3F4383;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-family: SansSerif;
  font-size: 18px;
  transition: background-color 0.3s, transform 0.2s;
}

.modal-content button:hover {
  background-color: #8386b5;
  transform: scale(1.05);
}
</style>


