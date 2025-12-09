<template>
  <nav class="navbar">
    <!-- Left side -->
    <div class="navbar-left">
      <router-link to="/" class="brand" aria-label="StemStatistieken">
        <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2zm0 0V9a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v10m-6 0a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2m0 0V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2z" />
        </svg>
        <span>StemStatistieken</span>
      </router-link>
      <button class="hamburger" @click="toggleMenu">☰</button>
    </div>

    <!-- Center -->
    <div :class="['navbar-collapse', { active: menuOpen }]">
      <div class="nav-links">
        <router-link to="/" class="nav-link" @click="closeMenu">Home</router-link>
        <router-link to="/dashboard" class="nav-link" @click="closeMenu">Uitslagen</router-link>
        <router-link to="/Candidate" class="nav-link" @click="closeMenu">Kandidaten</router-link>
        <router-link to="/vergelijken" class="nav-link" @click="closeMenu">Vergelijken</router-link>
        <router-link to="/forum" class="nav-link" @click="closeMenu">Forum</router-link>
        <router-link to="/forum2" class="nav-link" @click="closeMenu">Forum 2</router-link>
        <router-link to="/over-ons" class="nav-link" @click="closeMenu">Over Ons</router-link>
      </div>

      <div class="nav-actions">
        <div class="search-wrapper">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Zoeken..."
            class="search-input"
            @keyup.enter="performSearch"
            aria-label="Zoeken"
          />
          <span class="search-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </span>
          <button @click="performSearch" class="search-btn" aria-label="Zoek">Zoek</button>
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
  justify-content: space-between;
  align-items: center;
  background-color: #1e293b; /* same as banner */
  color: #ffffff;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 1000;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}



/* Brand */
.brand {
  font-weight: 700;
  font-size: 1.3em;
  text-decoration: none;
  color: #ffffff;
  padding: 18px 24px;
  transition: color 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand:hover {
  color: #3b82f6;
}
/* Brand icon (same as login page) */
.brand-icon {
  width: 22px;
  height: 22px;
}

/* Hamburger button */
.hamburger {
  display: none;
  background: none;
  border: none;
  font-size: 1.8em;
  cursor: pointer;
  color: #ffffff;
  padding: 18px 24px;
}

/* Collapsible menu */
.navbar-collapse {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
  transition: max-height 0.4s ease, opacity 0.4s ease;
  overflow: hidden;
}

.navbar-collapse.active {
  opacity: 1;
  max-height: 500px;
}

.nav-links {
  display: flex;
  gap: 4px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: rgba(255, 255, 255, 0.85);
  padding: 18px 16px;
  font-weight: 500;
  font-size: 15px;
  transition: all 0.2s;
  position: relative;
  border-bottom: 2px solid transparent;
}

.nav-link:hover {
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.06);
}

.nav-link.router-link-active {
  color: #ffffff;
  border-bottom-color: #3b82f6;
}
.nav-register {
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.user-icon {
  display: block;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  transition: opacity 0.2s;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.user-icon:hover {
  opacity: 0.8;
}

.nav-actions {
  display: flex;
  align-items: center;
  padding: 0 16px;
}

.search-wrapper {
  position: relative;
  width: 260px;
  min-width: 200px;
}

.search-input {
  width: 100%;
  padding: 8px 80px 8px 36px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 14px;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.08);
  color: white;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.18);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.6);
  pointer-events: none;
}

.search-icon svg {
  width: 18px;
  height: 18px;
  display: block;
}

.search-btn {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  border: 1px solid rgba(255, 255, 255, 0.18);
  background-color: transparent;
  color: #e2e8f0;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  font-family: 'Nunito', sans-serif;
  transition: all 0.2s;
}

.search-btn:hover {
  background-color: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.28);
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

  .navbar {
    flex-wrap: wrap;
  }

  .navbar-collapse {
    display: none;
    flex-direction: column;
    width: 100%;
    background-color: #1e293b;
    padding: 0;
    opacity: 0;
    max-height: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
  }

  .navbar-collapse.active {
    display: flex;
  }

  .nav-links {
    flex-direction: column;
    width: 100%;
    gap: 0;
  }

  .nav-link {
    width: 100%;
    padding: 16px 24px;
    border-bottom: none;
    border-left: 3px solid transparent;
  }

  .nav-link:hover,
  .nav-link.router-link-active {
    background: rgba(255, 255, 255, 0.06);
    border-left-color: #3b82f6;
    color: #ffffff;
  }

  .nav-actions {
    width: 100%;
    padding: 16px 24px;
    justify-content: stretch;
  }

  .search-wrapper {
    width: 100%;
  }

  .nav-register {
    width: 100%;
    padding: 16px 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
  }
}

@media (max-width: 480px) {
  .brand {
    font-size: 1.1em;
    padding: 16px 20px;
  }

  .search-wrapper {
    width: 100%;
  }

  .search-input {
    font-size: 14px;
  }
}
</style>
