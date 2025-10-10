<template>
  <section class="login-page">
      <!-- Left message -->
    <section class="login-left">
      <div class="logo">📊 StemStatistieken</div>
      <h1>
        Bekijk snel en gemakkelijk<br />
        de verkiezingsresultaten<br />
        en discussieer mee!
      </h1>
    </section>

    <!-- Right card (login) -->
    <section class="login-right">
      <div class="login-card">
        <div class="login-logo">📊</div>
        <h2>Welkom terug</h2>
        <p class="subtitle">Log in om verder te gaan!</p>

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

          <button type="submit" class="btn-primary">Inloggen</button>
        </form>

        <p class="register-link">
          Geen account? <a href="/register">Registreer hier</a>
        </p>
      </div>
    </section>
  </section>

</template>

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

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100%;
  font-family: 'Inter', sans-serif;
  background-color: #f4f5f7;
}

/* LEFT SIDE */
.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  background-color: #f5f6f7;
  color: #000;
}

.login-left .logo {
  font-weight: 700;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.login-left h1 {
  font-size: 2rem;
  line-height: 1.3;
  font-weight: 700;
}

/* RIGHT SIDE */
.login-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  border-left: 1px solid #eee;
}

.login-card {
  width: 100%;
  max-width: 400px;
  text-align: center;
  padding: 40px 20px;
}

.login-logo {
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

/* FORM */
.login-form {
  text-align: left;
}

.form-group {
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

.error-message {
  color: red;
  font-size: 0.8rem;
  margin-bottom: 12px;
}

/* BUTTON */
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

/* FOOTER LINK */
.register-link {
  margin-top: 12px;
  font-size: 0.9rem;
  color: #555;
}

.register-link a {
  font-weight: 600;
  color: #000;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

/* RESPONSIVE */
@media (max-width: 800px) {
  .login-page {
    flex-direction: column;
  }
  .login-left {
    padding: 40px 20px;
    text-align: center;
    align-items: center;
  }
  .login-right {
    border-left: none;
  }
}
</style>
