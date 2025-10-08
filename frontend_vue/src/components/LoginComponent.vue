<template>
  <section class="login-page">
    <div class="login-grid">
      <!-- Left message -->
      <div class="left-panel">
        <h2>
          Bekijk snel en gemakkelijk<br />
          de verkiezingsresultaten!<br />
        </h2>
      </div>

      <!-- Right card -->
      <div class="card">
        <h3>Welkom Terug</h3>
        <p class="sub">Log in</p>

        <form @submit.prevent="onSubmit" class="login-form">
          <label for="email">Email<span class="req">*</span></label>
          <input id="email" v-model="email" type="email"/>

          <label for="password">Wachtwoord<span class="req">*</span></label>
          <input id="password" v-model="password" type="password"/>

          <div class="help-row">
            <a href="#" @click.prevent>Forgot password?</a>
          </div>

          <!-- Error message -->
          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <button type="submit" class="primary">Inloggen</button>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.login-page {
  max-width: 1200px;
  margin: 60px auto;
}

.login-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

.left-panel {
  background: #f1f2f6;
  border-radius: 12px;
  padding: 48px 40px;
  min-height: 420px;
  display: flex;
  align-items: center;
}

.left-panel h2 {
  margin: 0;
  font-weight: 600;
  line-height: 1.35;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 28px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border: 1px solid #eee;
}

.card h3 {
  margin: 8px 0 4px 0;
  font-weight: 600;
}

.sub {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 0.95rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

label {
  font-size: 0.9rem;
}

.req { color: #c0392b; margin-left: 4px; }

input {
  padding: 10px 12px;
  border: 1px solid #cfd3d7;
  border-radius: 6px;
}

.help-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.muted { color: #999; font-size: 0.8rem; }

.primary {
  padding: 12px 14px;
  border: none;
  border-radius: 6px;
  background: #3F4383;
  color: #fff;
  cursor: pointer;
  margin-top: 8px;
}

.primary:hover { opacity: 0.95; }

.error-message {
  color: #c0392b;
  font-size: 0.9rem;
  margin-top: 8px;
  padding: 8px;
  background-color: #fdf2f2;
  border: 1px solid #fecaca;
  border-radius: 4px;
}

@media (max-width: 900px) {
  .login-grid { grid-template-columns: 1fr; }
  .left-panel { min-height: auto; }
}
</style>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const router = useRouter()

function onSubmit() {
  // Clear previous error
  errorMessage.value = ''

  // Fake login, check if email is test@demo.com
  if (email.value === 'test@demo.com' && password.value === 'password') {
    localStorage.setItem('token', 'fake-jwt-token-12345')

    window.dispatchEvent(new CustomEvent('loginStateChanged'))

    router.push('/')
  } else {
    errorMessage.value = 'Ongeldige e-mail of wachtwoord.'
  }
}
</script>
