<template>
  <section class="register-page">
    <!-- Left message -->
    <section class="register-left">
      <div class="logo">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2zm0 0V9a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v10m-6 0a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2m0 0V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2z" />
        </svg>
        <span>StemStatistieken</span>
      </div>
      <h1>
        Bekijk snel en gemakkelijk<br />
        de verkiezingsresultaten<br />
        en discussieer mee!
      </h1>
    </section>

    <!-- Right card (register) -->
    <section class="register-right">
      <div class="register-card">
        <div class="register-icon-wrapper">
          <div class="register-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
        </div>
        <h2>Creëer uw account</h2>
        <p class="subtitle">Voeg uw informatie in om een account te kunnen maken!</p>

        <form @submit.prevent="handleSubmit" class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label for="name">Voornaam<span class="req">*</span></label>
              <input type="text" id="name" v-model="form.name" placeholder="voer uw voornaam in" />
            </div>
            <div class="form-group">
              <label for="lastName">Achternaam</label>
              <input type="text" id="lastName" v-model="form.lastName" placeholder="voer uw achternaam in" />
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email<span class="req">*</span></label>
            <input type="email" id="email" v-model="form.email" placeholder="voer uw email in" />
          </div>

          <div class="form-group">
            <label for="password">Wachtwoord<span class="req">*</span></label>
            <input type="password" id="password" v-model="form.password" placeholder="voer uw wachtwoord in" />
          </div>

          <div class="form-check">
            <input type="checkbox" id="terms" v-model="acceptedTerms" />
            <label for="terms">Ik ga akkoord met de <a href="#">Algemene Voorwaarden</a></label>
          </div>

          <!-- Error messages -->
          <div v-if="errors.name" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ errors.name }}</span>
          </div>

          <div v-if="errors.email" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ errors.email }}</span>
          </div>

          <div v-if="errors.password" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ errors.password }}</span>
          </div>

          <div v-if="serverError" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ serverError }}</span>
          </div>

          <button type="submit" class="btn-primary" :disabled="submitting">
            {{ submitting ? 'Bezig...' : 'Registreren' }}
          </button>
        </form>

        <p class="login-link">
          Al een account? <router-link to="/login">Log dan in</router-link>
        </p>
      </div>
    </section>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="success-modal-overlay" @click.self="closeSuccessModal">
      <div class="success-modal">
        <div class="success-icon-wrapper">
          <div class="success-icon-circle">
            <span class="success-checkmark">✓</span>
          </div>
        </div>
        <h3>Registratie geslaagd!</h3>
        <p>Uw account is succesvol aangemaakt. U kunt nu inloggen met uw gegevens.</p>
        <button @click="goToLogin" class="btn-success">Naar inloggen</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserService } from '../services/UserService.js'

const form = reactive({
  name: '',
  lastName: '',
  email: '',
  password: ''
})

const errors = reactive({
  name: '',
  email: '',
  password: ''
})

const acceptedTerms = ref(false)
const submitting = ref(false)
const serverError = ref('')
const showSuccessModal = ref(false)
const router = useRouter()

// Must match backend: at least 8 chars, one lowercase, one uppercase, one digit
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/

function validateForm() {
  let valid = true
  errors.name = ''
  errors.email = ''
  errors.password = ''
  serverError.value = ''

  if (!form.name.trim()) {
    errors.name = 'Naam is verplicht'
    valid = false
  }

  if (!form.email.trim()) {
    errors.email = 'Email is verplicht'
    valid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.email = 'Ongeldig emailadres'
    valid = false
  }

  if (!form.password.trim()) {
    errors.password = 'Wachtwoord is verplicht'
    valid = false
  } else if (!PASSWORD_REGEX.test(form.password)) {
    errors.password = 'Minimaal 8 tekens, met hoofdletter, kleine letter en cijfer'
    valid = false
  }

  if (!acceptedTerms.value) {
    serverError.value = 'Accepteer de voorwaarden om door te gaan'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  submitting.value = true
  serverError.value = ''
  try {
    await UserService.register({
      name: form.name,
      lastName: form.lastName,
      email: form.email,
      password: form.password
    })

    form.name = ''
    form.lastName = ''
    form.email = ''
    form.password = ''
    acceptedTerms.value = false
    showSuccessModal.value = true
    // Prevent body scroll when modal is open
    document.body.style.overflow = 'hidden'
  } catch (e) {
    serverError.value = e.message || 'Netwerkfout. Probeer later opnieuw.'
  } finally {
    submitting.value = false
  }
}

function closeSuccessModal() {
  showSuccessModal.value = false
  document.body.style.overflow = ''
}

function goToLogin() {
  closeSuccessModal()
  router.push('/login')
}

// Handle ESC key to close modal
function handleEscape(e) {
  if (e.key === 'Escape' && showSuccessModal.value) {
    closeSuccessModal()
    router.push('/login')
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleEscape)
  // Clean up body overflow style
  document.body.style.overflow = ''
})
</script>

<style scoped>
.register-page {
  display: flex;
  min-height: calc(100vh + 64px);
  width: 100vw;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f8fafc;
  margin: -64px calc(50% - 50vw) 0;
  padding-top: 64px;
  overflow-y: auto;
}

.register-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  background: linear-gradient(135deg, #1e293b, #334155);
  color: white;
  position: relative;
  min-height: 100vh;
}

.register-left::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/images/banner.png') center/cover;
  opacity: 0.05;
  z-index: 0;
}

.register-left > * {
  position: relative;
  z-index: 1;
}

.register-left .logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font: 700 1.5rem 'Nunito', sans-serif;
  margin-bottom: 60px;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.register-left h1 {
  font: 800 2.5rem/1.3 'Nunito', sans-serif;
  margin: 0;
  letter-spacing: -0.5px;
}

.register-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  padding: 40px;
}

.register-card {
  width: 100%;
  max-width: 450px;
  text-align: center;
}

.register-icon-wrapper {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.register-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e293b, #334155);
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-icon svg {
  width: 40px;
  height: 40px;
  color: white;
}

.register-card h2 {
  font: 800 2rem/1 'Nunito', sans-serif;
  color: #1e293b;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #64748b;
  margin: 0 0 40px;
}

.register-form {
  text-align: left;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 0;
}

.form-group {
  flex: 1;
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font: 600 14px 'Nunito', sans-serif;
  color: #1e293b;
  margin-bottom: 8px;
}

.req {
  color: #e74c3c;
  margin-left: 4px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font: 16px 'Nunito', sans-serif;
  color: #1e293b;
  transition: all 0.2s;
}

.form-group input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  outline: none;
}

.form-group input::placeholder {
  color: #94a3b8;
}

.form-check {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0 24px;
  font-size: 14px;
  color: #1e293b;
}

.form-check input[type="checkbox"] {
  width: auto;
  margin: 0;
  cursor: pointer;
}

.form-check label {
  font: 500 14px 'Nunito', sans-serif;
  color: #1e293b;
  margin: 0;
  cursor: pointer;
}

.form-check label a {
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s;
}

.form-check label a:hover {
  color: #2563eb;
  text-decoration: underline;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #e74c3c;
  font-size: 14px;
  margin-bottom: 24px;
}

.error-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.btn-primary {
  width: 100%;
  padding: 12px 24px;
  background: #1e293b;
  color: white;
  border: none;
  border-radius: 8px;
  font: 600 16px 'Nunito', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 24px;
}

.btn-primary:hover:not(:disabled) {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  font-size: 14px;
  color: #64748b;
}

.login-link a {
  font-weight: 600;
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s;
}

.login-link a:hover {
  color: #2563eb;
  text-decoration: underline;
}

@media (max-width: 800px) {
  .register-page {
    flex-direction: column;
  }
  .register-left {
    padding: 40px 20px;
    text-align: center;
    align-items: center;
    min-height: 40vh;
  }
  .register-left h1 {
    font-size: 2rem;
  }
  .register-right {
    padding: 40px 20px;
  }
  .form-row {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .register-left {
    padding: 32px 16px;
    min-height: 35vh;
  }
  .register-left .logo {
    font-size: 1.2rem;
    margin-bottom: 32px;
  }
  .register-left h1 {
    font-size: 1.75rem;
  }
  .register-right {
    padding: 32px 16px;
  }
  .register-card h2 {
    font-size: 1.75rem;
  }
}

/* Success Modal Styles */
.success-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  animation: fadeIn 0.3s ease-out;
  padding: 16px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.success-modal {
  background: #ffffff;
  border-radius: 16px;
  padding: 40px 32px;
  max-width: 450px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.success-icon-wrapper {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.success-icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e293b, #334155);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.4s ease-out 0.1s both;
}

.success-checkmark {
  font-size: 48px;
  color: white;
  font-weight: bold;
  line-height: 1;
  display: block;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.success-modal h3 {
  font: 800 2rem/1 'Nunito', sans-serif;
  color: #1e293b;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.success-modal p {
  color: #64748b;
  margin: 0 0 40px;
  font-size: 16px;
}

.btn-success {
  width: 100%;
  padding: 12px 24px;
  background: #1e293b;
  color: white;
  border: none;
  border-radius: 8px;
  font: 600 16px 'Nunito', sans-serif;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-success:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.btn-success:active {
  transform: translateY(0);
}

@media (max-width: 480px) {
  .success-modal {
    padding: 32px 24px;
  }

  .success-icon-circle {
    width: 64px;
    height: 64px;
  }

  .success-checkmark {
    font-size: 36px;
  }

  .success-modal h3 {
    font-size: 1.75rem;
  }

  .success-modal p {
    font-size: 14px;
  }
}
</style>
