<script setup>
import { RouterView } from 'vue-router'
import TopNavbar from './components/TopNavbar.vue'
import OnboardingGuide from "@/components/OnboardingGuide.vue";
import HomeView from "@/views/HomeView.vue";
</script>

<template>
  <TopNavbar />

  <main>
    <HomeView />
    <RouterView />
  </main>

  <OnboardingGuide />
</template>

<style>
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

main {
  padding: 20px;
}
</style>
