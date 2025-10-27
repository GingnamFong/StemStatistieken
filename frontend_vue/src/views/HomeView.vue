<script setup>
import TheWelcome from '../components/TheWelcome.vue'
import YearBanner from '../components/YearBanner.vue'
import DutchMapKiesKring from '../components/DutchMapKiesKring.vue'
</script>

<template>
  <main>
    <!-- Year banner with properly quoted attribute -->
    <YearBanner backgroundImageUrl="/images/banner.png" />

    <!-- Welcome component -->
    <TheWelcome />

    <!-- Interactive map component -->

  </main>
</template>

<style scoped>
main {
  padding: 20px;
}
</style>
