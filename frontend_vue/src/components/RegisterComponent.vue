<template>
  <div class="register-page">
    <!-- Left side -->
    <section class="register-left">
      <div class="logo">📊 Stemstatestieken</div>
      <h1>Bekijk snel en gemakkelijk
        de verkiezingsresultaten
        en discussieer mee!</h1>

    </section>

    <!-- Right side (form) -->
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
            <input type="checkbox" id="terms" />
            <label for="terms">Ik ga akkoord met de <a href="#">Algemene Voorwaarden</a></label>
          </div>

          <button type="submit" class="btn-primary">Registreren</button>

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
  height: 100vh;
  width: 100%;
  background-color: #f4f5f7;
  font-family: 'Inter', sans-serif;
}

/* LEFT SIDE */
.register-left {
  min-width: 50%;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  background-color: #f5f6f7;
  color: #000;
}

.register-left .logo {
  font-weight: 700;
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.register-left h1 {
  font-size: 2rem;
  line-height: 1.3;
  font-weight: 700;
  margin-bottom: 16px;
}

.register-left p {
  color: #555;
  max-width: 380px;
}

/* RIGHT SIDE */
.register-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
  border-left: 1px solid #eee;
}

.register-card {
  width: auto;
  text-align: center;
  padding: 40px 20px;
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

/* FORM */
.register-form {
  text-align: left;
}

.form-row {
  display: flex;
  gap: 12px;
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

span {
  color: red;
  font-size: 0.8rem;
}

/* CHECKBOX */
.form-check {
  margin: 12px 0 24px;
  font-size: 0.85rem;
  color: #444;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-check a {
  color: #000;
  text-decoration: underline;
}

/* BUTTONS */
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

.btn-google {
  width: 100%;
  background: #e5e7eb;
  border: none;
  border-radius: 999px;
  padding: 12px;
  font-weight: 600;
  cursor: pointer;
}

/* FOOTER LINK */
.login-link {
  margin-top: 24px;
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

/* RESPONSIVE FIXES */
@media (max-width: 800px) {
  .register-page {
    flex-direction: column;
  }
  .register-left {
    padding: 40px 20px;
    text-align: center;
    align-items: center;
  }
  .register-right {
    border-left: none;
  }
}
</style>
