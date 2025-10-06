<template>
  <nav class="navbar">
    <!-- Brand + Hamburger -->
    <div class="navbar-left">
      <router-link to="/" class="brand">MyApp</router-link>
      <button class="hamburger" @click="toggleMenu">☰</button>
    </div>

    <!-- Collapsible links + search + profile -->
    <div :class="['navbar-collapse', { 'active': menuOpen }]">
      <div class="nav-links">
        <router-link to="/" class="nav-link" @click="closeMenu">Home</router-link>
        <router-link to="/uitslagen" class="nav-link" @click="closeMenu">Uitslagen</router-link>
        <router-link to="/vergelijken" class="nav-link" @click="closeMenu">Vergelijken</router-link>
        <router-link to="/over-ons" class="nav-link" @click="closeMenu">Over Ons</router-link>
      </div>

      <div class="nav-actions">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search..."
          class="search-input"
          @keyup.enter="performSearch"
        />
        <button @click="performSearch" class="search-btn">Zoek</button>

        <router-link to="/profile" class="profile-link" @click="closeMenu">
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const searchQuery = ref('')
const menuOpen = ref(false)
const router = useRouter()

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
/* Navbar container */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2c3e50;
  color: white;
  padding: 10px 20px;
  position: relative;
  flex-wrap: wrap;
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
  width: 100%;
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
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  padding: 5px 10px;
  border-radius: 4px;
  border: none;
}

.search-btn {
  padding: 5px 10px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
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
    background-color: #34495e;
    padding: 10px 0;
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
