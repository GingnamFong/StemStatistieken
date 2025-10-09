
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
import { ref, onMounted } from 'vue'

const mapContainer = ref(null)
const svgContainer = ref(null)
const svgContent = ref('')
const selectedKieskring = ref(null)

const kieskringenData = {
  1: { number: 1, name: 'Groningen', province: 'Groningen', stemmen:''},
  2: { number: 2, name: 'Leeuwarden', province: 'Friesland', stemmen:'' },
  3: { number: 3, name: 'Assen', province: 'Drenthe', stemmen:'' },
  4: { number: 4, name: 'Zwolle', province: 'Overijssel', stemmen:'' },
  5: { number: 5, name: 'Arnhem', province: 'Gelderland', stemmen:'' },
  6: { number: 6, name: 'Utrecht', province: 'Utrecht', stemmen:'' },
  7: { number: 7, name: 'Haarlem', province: 'Noord-Holland', stemmen:'' },
  8: { number: 8, name: 'Amsterdam', province: 'Noord-Holland', stemmen:'' },
  9: { number: 9, name: 'Den Haag', province: 'Zuid-Holland', stemmen:'' },
  10: { number: 10, name: 'Leiden', province: 'Zuid-Holland', stemmen:'' },
  11: { number: 11, name: 'Rotterdam', province: 'Zuid-Holland', stemmen:'' },
  12: { number: 12, name: 'Dordrecht', province: 'Zuid-Holland', stemmen:'' },
  13: { number: 13, name: 'Middelburg', province: 'Zeeland', stemmen:'213' },
  14: { number: 14, name: 'Tilburg', province: 'Noord-Brabant', stemmen:'' },
  15: { number: 15, name: "'s-Hertogenbosch", province: 'Noord-Brabant', stemmen:'' },
  16: { number: 16, name: 'Maastricht', province: 'Limburg', stemmen:'' },
  17: { number: 17, name: 'Almere', province: 'Flevoland', stemmen:'' },
  18: { number: 18, name: 'Nijmegen', province: 'Gelderland', stemmen:'' },
  19: { number: 19, name: 'Breda', province: 'Noord-Brabant', stemmen:'' }
}

onMounted(async () => {
  try {
    const response = await fetch('/src/assets/kieskringen.svg')
    let svg = await response.text()

    // Make paths clickable
    svg = svg.replace(/<path/g, '<path class="kieskring-path" style="cursor:pointer; fill:#e0e0e0; stroke:#000000; stroke-width:1"')
    svg = svg.replace(/<text/g, '<text class="kieskring-text" style="pointer-events:none"')

    svgContent.value = svg

    // Add event listeners after SVG is rendered
    setTimeout(() => {
      addPathListeners()
    }, 100)
  } catch (error) {
    console.error('Error loading SVG:', error)
  }
})

const addPathListeners = () => {
  if (!svgContainer.value) return

  const paths = svgContainer.value.querySelectorAll('path')
  const texts = svgContainer.value.querySelectorAll('text tspan')

  paths.forEach((path, index) => {
    path.addEventListener('mouseenter', () => {
      path.style.fill = 'rgb(103,215,114)'
      path.style.fillOpacity = '0.7'
    })

    path.addEventListener('mouseleave', () => {
      if (selectedKieskring.value?.index !== index) {
        path.style.fill = '#e0e0e0'
        path.style.fillOpacity = '1'
      }
    })

    path.addEventListener('click', () => {
      handlePathClick(path, index, texts)
    })
  })
}

// find the kieskring number from nearby text
const handlePathClick = (path, index, texts) => {
  let kieskringNumber = null

  texts.forEach(text => {
    const number = parseInt(text.textContent.trim())
    if (number >= 1 && number <= 19) {
      kieskringNumber = number
    }
  })

  if (kieskringNumber && kieskringenData[kieskringNumber]) {
    selectedKieskring.value = { ...kieskringenData[kieskringNumber], index }

    const allPaths = svgContainer.value.querySelectorAll('path')
    allPaths.forEach(p => {
      p.style.fill = '#e0e0e0'
      p.style.fillOpacity = '1'
    })

    // Highlight selected path
    path.style.fill = 'rgb(0,255,3)'
    path.style.fillOpacity = '0.7'
  }
}

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

.map-container > div {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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

.kieskring-info {
  background: #e8f5e9;
  border: 2px solid #4CAF50;
  border-radius: 8px;
  padding: 1.5rem;
  margin: 1rem auto;
  max-width: 500px;
  text-align: center;
  animation: slideIn 0.3s ease-out;
}

.kieskring-info h3 {
  color: #2e7d32;
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
}

.kieskring-info p {
  color: #424242;
  margin: 0.25rem 0;
  font-size: 1.1rem;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .map-container {
    height: 400px;
  }
}

:deep(.kieskring-path) {
  transition: fill 0.2s ease, fill-opacity 0.2s ease;
}

:deep(svg) {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
}
</style>
