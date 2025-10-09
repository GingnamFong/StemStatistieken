
<template>
  <div class="dutch-map-wrapper">
    <section class="map-section">
      <h2>Interactieve Nederlandse kieskringkaart</h2>
      <p>Klik op een kieskring om verkiezingsgegevens te bekijken</p>

      <div v-if="selectedKieskring" class="kieskring-info">
        <h3>Kieskring {{ selectedKieskring.number }}</h3>
        <p>{{ selectedKieskring.name }}</p>
        <p>Provincie: {{ selectedKieskring.province }}</p>
        <p>Stemmen: {{ selectedKieskring.stemmen }}</p>
      </div>
    </section>

    <div class="map-container" ref="mapContainer">
      <div ref="svgContainer" v-html="svgContent"></div>
    </div>
  </div>
</template>
<script setup>

</script>
<style scoped>

</style>
