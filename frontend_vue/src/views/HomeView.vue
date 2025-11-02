<script setup>
import TheWelcome from '../components/TheWelcome.vue'
import YearBanner from '../components/YearBanner.vue'
import DutchMapKiesKring from '../components/DutchMapKiesKring.vue'
import DutchMapProvincie from '../components/DutchMapProvincie.vue'
import MapToggle from '../components/MapToggle.vue'
import { ref } from 'vue'

const currentView = ref('kieskringen')

const handleViewChange = (view) => {
  currentView.value = view
}

const handleProvincieSelected = (provincieNaam) => {
  console.log('Provincie geselecteerd:', provincieNaam)
}
</script>

<template>
  <main>
    <!-- Year banner with properly quoted attribute -->
    <YearBanner backgroundImageUrl="/images/banner.png" />

    <!-- Welcome component -->
    <TheWelcome />

    <MapToggle @view-changed="handleViewChange" />

    <DutchMapKiesKring v-if="currentView === 'kieskringen'" />
    <DutchMapProvincie v-if="currentView === 'provincies'" @provincie-selected="handleProvincieSelected" />

  </main>
</template>

<style scoped>
main {
  padding: 20px;
}
</style>
