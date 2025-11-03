<template>
  <nav class="navbar">
    <!-- Left side -->
    <div class="navbar-left">
      <router-link to="/" class="brand">MyApp</router-link>
      <button class="hamburger" @click="toggleMenu">☰</button>
    </div>

    <!-- Center -->
    <div :class="['navbar-collapse', { active: menuOpen }]">
      <div class="nav-links">
        <router-link to="/" class="nav-link" @click="closeMenu">Home</router-link>
        <router-link to="/dashboard" class="nav-link" @click="closeMenu">Uitslagen</router-link>
        <router-link to="/Candidate" class="nav-link" @click="closeMenu">kandidaten</router-link>
        <router-link to="/vergelijken" class="nav-link" @click="closeMenu">Vergelijken</router-link>
        <router-link to="/over-ons" class="nav-link" @click="closeMenu">Over Ons</router-link>
      </div>

      <div class="nav-actions">
        <div class="search-wrapper">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search..."
            class="search-input"
            @keyup.enter="performSearch"
          />
          <span class="search-icon">🔍</span>
          <button @click="performSearch" class="search-btn">Zoek</button>
        </div>
      </div>

      <div class="nav-register">
        <!-- Show user icon when logged in -->
        <div v-if="isLoggedIn" class="user-section">
          <img src="/images/user.png" alt="User" class="user-icon" @click="logout" />
        </div>
        <!-- Show login link when not logged in -->
        <router-link v-else to="/login" class="nav-link" @click="closeMenu">Login</router-link>
      </div>
    </div>
  </nav>

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const searchQuery = ref('')
const menuOpen = ref(false)
const router = useRouter()

const loginTrigger = ref(0)

const isLoggedIn = computed(() => {
  loginTrigger.value // This makes it reactive
  return localStorage.getItem('token') !== null
})

function logout() {
  localStorage.removeItem('token')
  loginTrigger.value++
}


function handleLoginStateChange() {
  loginTrigger.value++
}

onMounted(() => {
  window.addEventListener('loginStateChanged', handleLoginStateChange)
})

onUnmounted(() => {
  window.removeEventListener('loginStateChanged', handleLoginStateChange)
})

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

function performSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value } })
    searchQuery.value = ''
    menuOpen.value = false
  }
}
</script>

<style scoped>
.navbar,
.nav-link,
.brand,
.search-input,
.search-btn {
  font-family: 'Nunito', sans-serif;
}
/* Navbar container */
.navbar {
  display: flex;
  left: 50%; /* center horizontally */
  transform: translateX(-50%);
  justify-content: space-between;
  width: 100%;
  max-width: 1650px;
  align-items: center;
  background-color: #3F4383;
  color: white;
  padding: 10px 20px;
  position: fixed;
  flex-wrap: wrap;
  border-bottom-left-radius: 15px; /* rounded bottom corners */
  border-bottom-right-radius: 15px;
  z-index: 1000;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  overflow: hidden;

}
main {
  margin-top: 80px;
}

/* Brand */
.brand {
  font-weight: bold;
  font-size: 1.2em;
  text-decoration: none;
  color: white;
}

/* Hamburger button */
.hamburger {
  display: none;
  background: none;
  border: none;
  font-size: 1.8em;
  cursor: pointer;
  color: white;
}

/* Collapsible menu */
.navbar-collapse {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom-left-radius: 15px;
  border-bottom-right-radius: 15px;
  transition: max-height 0.4s ease, opacity 0.4s ease;
  width: 100%;
  overflow: hidden;
}
.navbar-collapse.active {
  opacity: 1;
  max-height: 500px; /* enough to show all links */
}


.nav-links {
  display: flex;
  gap: 15px;
}

.nav-link {
  text-decoration: none;
  color: white;
}

.nav-link:hover {
  color: #3498db;
  position: relative;
  padding: 5px 0
}
.nav-link::after {
  content: '';
  display: block;
  height: 2px;
  width: 0;
  background: #ffcc00;
  transition: width 0.3s;
}

.nav-link:hover::after {
  width: 100%;
}
.nav-register {
  display: flex;
  align-items: center;
  margin-left: auto; /* pushes it to the far right */
}

.user-icon {
  display: block;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  transition: transform 0.2s;
  cursor: pointer;
}

.user-icon:hover {
  transform: scale(1.1);
}

.nav-actions {
  flex: 1;
  display: flex;
  justify-content: center; /* center the wrapper */
  align-items: center;
}
.search-wrapper {
  position: relative;
  width: 220px; /* smaller width */
  min-width: 150px;
}

.search-input {
  width: 100%;
  padding: 5px 10px 5px 35px;;
  border-radius: 25px;
  border: none;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  font-size: 0.9em;
}
.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #555;
  pointer-events: none; /* click passes through */
}


.search-btn {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background-color: #3498db;
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.85em;
  font-family: 'Nunito', sans-serif;
  transition: background 0.3s;
}

.search-btn:hover {
  background-color: #2980b9;
}

.profile-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
}

/* Responsive */
@media (max-width: 1024px) {
  .hamburger {
    display: block;
  }

  .navbar-collapse {
    display: none;
    flex-direction: column;
    width: 100%;
    background-color: #3F4383;
    border-bottom-left-radius: 15px;
    border-bottom-right-radius: 15px;
    padding: 10px 0;
    opacity: 0;
    max-height: 0;
  }

  .navbar-collapse.active {
    display: flex;
  }

  .nav-links {
    flex-direction: column;
    gap: 10px;
    padding: 0 20px;
  }

  .nav-actions {
    flex-direction: column;
    gap: 10px;
    padding: 0 20px;
    margin-top: 10px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
