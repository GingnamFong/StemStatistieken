<script setup>
import { RouterView } from 'vue-router'
import TopNavbar from './components/TopNavbar.vue'
</script>

<template>
  <!-- Top Navbar -->
  <TopNavbar />

  <!-- Page content -->
  <main>
    <RouterView />
  </main>
</template>

<style>
/* Optional global styles */
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

main {
  padding: 20px;
}
</style>
