<script setup>
import { RouterView } from 'vue-router'
import TopNavbar from './components/TopNavbar.vue'
import OnboardingGuide from "@/components/OnboardingGuide.vue"
</script>

<template>
  <TopNavbar />

  <!-- wrapper for page content -->
  <main class="page-content">
    <RouterView />
  </main>

  <OnboardingGuide />
</template>

<style>
/* --- Global page layout --- */
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

/* Push page content below the fixed navbar */
.page-content {
  padding-top: 80px; /* match your navbar height */
  padding-left: 20px;
  padding-right: 20px;
  min-height: calc(100vh - 80px); /* ensures proper spacing for tall pages */
  box-sizing: border-box;
}

/* Optional: subtle background to make content stand out */
.page-content {
  background-color: #f7f8fa;
}
</style>
