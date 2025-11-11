<template>
  <section class="register-page">
    <!-- Left side -->
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

    <!-- Right side (form) -->
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
              <label for="name">Voornaam</label>
              <input type="text" id="name" v-model="form.name" placeholder="voer uw voornaam in"/>
              <span v-if="errors.name" class="error-text">{{ errors.name }}</span>
            </div>
            <div class="form-group">
              <label for="lastName">Achternaam</label>
              <input type="text" id="lastName" v-model="form.lastName" placeholder="voer uw achternaam in"/>
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" v-model="form.email" placeholder="voer uw email in"/>
            <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label for="password">Wachtwoord</label>
            <input type="password" id="password" v-model="form.password" placeholder="voer uw wachtwoord in"/>
            <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
          </div>

          <div class="form-check">
            <input type="checkbox" id="terms" />
            <label for="terms">Ik ga akkoord met de <a href="#" class="terms-link">Algemene Voorwaarden</a></label>
          </div>

          <button type="submit" class="btn-primary">Registreren</button>

        </form>

        <p class="login-link">
          Al een account? <router-link to="/login">Log dan in</router-link>
        </p>
      </div>
    </section>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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

const submitted = ref(false)

function validateForm() {
  let valid = true
  errors.name = ''
  errors.email = ''
  errors.password = ''

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
  } else if (form.password.length < 6) {
    errors.password = 'Minimaal 6 tekens'
    valid = false
  }

  return valid
}

function handleSubmit() {
  submitted.value = false
  if (validateForm()) {
    submitted.value = true
    console.log('Form data:', form)
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  height: calc(100vh + 64px);
  min-height: 100vh;
  width: 100vw;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f8fafc;
  margin: -64px calc(50% - 50vw) 0;
  overflow: hidden;
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
  overflow: hidden;
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
  overflow-y: auto;
}

.register-card {
  width: 100%;
  max-width: 500px;
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
  gap: 12px;
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

.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="password"] {
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

.error-text {
  display: block;
  color: #e74c3c;
  font-size: 14px;
  margin-top: 4px;
}

.form-check {
  margin: 12px 0 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #475569;
}

.form-check input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.form-check label {
  font: 400 14px 'Nunito', sans-serif;
  color: #475569;
  margin: 0;
}

.terms-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.terms-link:hover {
  color: #2563eb;
  text-decoration: underline;
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

.btn-primary:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.btn-primary:active {
  transform: translateY(0);
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
    gap: 0;
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
</style>
