<template>
  <div class="register-page">
    <!-- ...unchanged UI... -->
    <section class="register-right">
      <div class="register-card">
        <div class="register-logo">📊</div>
        <h2>Creëer uw account</h2>
        <p class="subtitle">Voeg uw informatie in om een account te kunnen maken!</p>

        <form @submit.prevent="handleSubmit" class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label for="name">Voornaam</label>
              <input type="text" id="name" v-model="form.name" />
              <span v-if="errors.name">{{ errors.name }}</span>
            </div>
            <div class="form-group">
              <label for="lastName">Achternaam</label>
              <input type="text" id="lastName" v-model="form.lastName" />
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" v-model="form.email" />
            <span v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label for="password">Wachtwoord</label>
            <input type="password" id="password" v-model="form.password" />
            <span v-if="errors.password">{{ errors.password }}</span>
          </div>

          <div class="form-check">
            <input type="checkbox" id="terms" v-model="acceptedTerms" />
            <label for="terms">Ik ga akkoord met de <a href="#">Algemene Voorwaarden</a></label>
          </div>

          <div v-if="serverError" style="color:red; font-size:0.9rem; margin-bottom:8px;">
            {{ serverError }}
          </div>

          <button type="submit" class="btn-primary" :disabled="submitting">
            {{ submitting ? 'Bezig...' : 'Registreren' }}
          </button>
        </form>

        <p class="login-link">
          Al een account? <a href="/login">Log dan in</a>
        </p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { API_BASE_URL } from '../config/api.js'

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
    const res = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: form.name,
        lastName: form.lastName,
        email: form.email,
        password: form.password
      })
    })

    if (res.ok) {
      alert('Registratie geslaagd')
      form.name = ''
      form.lastName = ''
      form.email = ''
      form.password = ''
      acceptedTerms.value = false
    } else {
      const text = await res.text()
      serverError.value = text || 'Registratie mislukt'
    }
  } catch {
    serverError.value = 'Netwerkfout. Probeer later opnieuw.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
  width: 100%;
  font-family: 'Inter', sans-serif;
  background-color: #f4f5f7;
}

.register-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  border-left: 1px solid #eee;
}

.register-card {
  width: 100%;
  max-width: 420px;
  text-align: center;
  padding: 40px 20px;
  background: #fff;
}

.register-logo {
  font-size: 1.8rem;
  margin-bottom: 10px;
}

h2 {
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 4px;
  color: #111;
}

.subtitle {
  color: #666;
  margin-bottom: 30px;
  font-size: 0.95rem;
}

.register-form {
  text-align: left;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group {
  flex: 1;
  margin-bottom: 18px;
}

label {
  display: block;
  font-size: 0.9rem;
  margin-bottom: 6px;
  color: #333;
}

input {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-bottom: 1px solid #ccc;
  font-size: 1rem;
  background: transparent;
  outline: none;
}

input:focus {
  border-bottom-color: #000;
}

.form-check {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0 20px;
  font-size: 0.9rem;
}

.btn-primary {
  width: 100%;
  background: #000;
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 12px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 12px;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #222;
}

.login-link {
  margin-top: 12px;
  font-size: 0.9rem;
  color: #555;
}

.login-link a {
  font-weight: 600;
  color: #000;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

@media (max-width: 800px) {
  .register-page {
    flex-direction: column;
  }
  .register-right {
    border-left: none;
  }
  .form-row {
    flex-direction: column;
  }
}
</style>
