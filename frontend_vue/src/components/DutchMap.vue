
<template>
  <section class="map-section">
    <h2>Interactieve Nederlandse kieskringkaart</h2>
    <p>Beweeg de muis over de kieskringen om verkiezingsgegevens te bekijken</p>

  </section>

  <div class="map-container">
    <img src="/src/assets/kieskringen.svg" alt="Dutch Electoral Map" class="dutch-map"/>
  </div>
</template>

<script setup>

</script>

<style scoped>
.map-container {
  width: 100%;
  height: 600px;
  border: 2px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dutch-map {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.map-section {
  margin: 2rem 0;
  padding: 1rem;
}

.map-section h2 {
  text-align: center;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.map-section p {
  text-align: center;
  color: #666;
  margin-bottom: 1rem;
}
/* Responsive design */
@media (max-width: 768px) {
  .map-container {
    height: 400px;
  }
}

</style>
