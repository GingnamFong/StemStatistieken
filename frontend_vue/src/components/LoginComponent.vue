<template>
  <section class="login-page">
      <!-- Left message -->
    <section class="login-left">
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

    <!-- Right card (login) -->
    <section class="login-right">
      <div class="login-card">
        <div class="login-icon-wrapper">
          <div class="login-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
        </div>
        <h2>Welkom terug</h2>
        <p class="subtitle">Log in om verder te gaan!</p>

        <form @submit.prevent="onSubmit" class="login-form">
          <div class="form-group">
            <label for="email">Email<span class="req">*</span></label>
            <input id="email" v-model="email" type="email" placeholder="voer uw email in"/>
          </div>

          <div class="form-group">
            <label for="password">Wachtwoord<span class="req">*</span></label>
            <input id="password" v-model="password" type="password" placeholder="voer uw wachtwoord in"/>
          </div>

          <div class="help-row">
            <a href="#" @click.prevent class="forgot-link">Wachtwoord vergeten?</a>
          </div>

          <!-- Error message -->
          <div v-if="errorMessage" class="error-message">
            <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{{ errorMessage }}</span>
          </div>

          <button type="submit" class="btn-primary">Inloggen</button>
        </form>

        <p class="register-link">
          Geen account? <router-link to="/register">Registreer hier</router-link>
        </p>
      </div>
    </section>
  </section>

</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '@/config/api.js'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const router = useRouter()

async function onSubmit() {
  errorMessage.value = ''

  if (!email.value.trim() || !password.value.trim()) {
    errorMessage.value = 'Vul e-mail en wachtwoord in.'
    return
  }

  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: email.value,
        password: password.value
      })
    })

    if (res.ok) {
      const data = await res.json()
      console.log('Login response:', data)

      // Store the token from the response
      if (data.token) {
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', data.userId?.toString() || '')
        // Store user data for profile page
        if (data.firstName || data.email) {
          localStorage.setItem('userData', JSON.stringify({
            id: data.userId,
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email,
            birthDate: data.birthDate || null
          }))
        }
        window.dispatchEvent(new CustomEvent('loginStateChanged'))
        router.push('/')
      } else {
        errorMessage.value = 'Ongeldige response van server.'
      }
    } else {
      // Try to read error message from response
      let errorText = 'Ongeldige e-mail of wachtwoord.'
      try {
        // First try to parse as JSON
        const contentType = res.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
          const errorData = await res.json()
          errorText = errorData.message || errorData.error || errorText
        } else {
          // If not JSON, read as text
          const text = await res.text()
          if (text && text.trim()) {
            errorText = text.trim()
          }
        }
      } catch {
        // If parsing fails, try to read as text
        try {
          const text = await res.text()
          if (text && text.trim()) {
            errorText = text.trim()
          }
        } catch (textError) {
          console.error('Error reading response:', textError)
        }
      }
      errorMessage.value = errorText
    }
  } catch (e) {
    console.error(e)
    // Only show network error if it's actually a network issue
    if (e instanceof TypeError && e.message.includes('Failed to fetch')) {
      errorMessage.value = 'Kan geen verbinding maken met de server. Zorg dat de backend draait.'
    } else {
      errorMessage.value = 'Netwerkfout. Probeer later opnieuw.'
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: calc(100vh + 64px);
  width: 100vw;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f8fafc;
  margin: -64px calc(50% - 50vw) 0;
  padding-top: 64px;
  overflow-y: auto;
}

.login-left {
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

.login-left::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/images/banner.png') center/cover;
  opacity: 0.05;
  z-index: 0;
}

.login-left > * {
  position: relative;
  z-index: 1;
}

.login-left .logo {
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

.login-left h1 {
  font: 800 2.5rem/1.3 'Nunito', sans-serif;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 450px;
  text-align: center;
}

.login-icon-wrapper {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.login-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e293b, #334155);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-icon svg {
  width: 40px;
  height: 40px;
  color: white;
}

.login-card h2 {
  font: 800 2rem/1 'Nunito', sans-serif;
  color: #1e293b;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #64748b;
  margin: 0 0 40px;
}

.login-form {
  text-align: left;
  margin-bottom: 24px;
}

.form-group {
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

.help-row {
  text-align: right;
  margin-bottom: 24px;
}

.forgot-link {
  color: #3b82f6;
  text-decoration: none;
  font: 500 14px 'Nunito', sans-serif;
  transition: color 0.2s;
}

.forgot-link:hover {
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

.btn-primary:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.btn-primary:active {
  transform: translateY(0);
}

.register-link {
  font-size: 14px;
  color: #64748b;
}

.register-link a {
  font-weight: 600;
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s;
}

.register-link a:hover {
  color: #2563eb;
  text-decoration: underline;
}

@media (max-width: 800px) {
  .login-page {
    flex-direction: column;
    overflow-x: hidden;
  }
  .login-left {
    padding: 40px 20px;
    text-align: center;
    align-items: center;
    min-height: 40vh;
  }
  .login-left h1 {
    font-size: 2rem;
  }
  .login-right {
    padding: 40px 20px;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .login-page {
    overflow-x: hidden;
  }
  .login-left {
    padding: 32px 16px;
    min-height: 35vh;
  }
  .login-left .logo {
    font-size: 1.2rem;
    margin-bottom: 32px;
  }
  .login-left h1 {
    font-size: 1.75rem;
  }
  .login-right {
    padding: 32px 16px;
    align-items: flex-start;
  }
  .login-card {
    width: 100%;
  }
  .login-card h2 {
    font-size: 1.75rem;
  }
  .form-group input {
    font-size: 16px; /* Prevents zoom on iOS */
  }
}
</style>
