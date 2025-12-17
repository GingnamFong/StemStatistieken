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
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #ffffff;
}

/* Push page content below the fixed navbar */
.page-content {
  padding-top: 64px;
  min-height: calc(100vh - 64px);
  box-sizing: border-box;
}
</style>
