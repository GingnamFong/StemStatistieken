<template>
  <div class="dutch-map-wrapper">
    <div class="map-layout" :class="{ 'no-data-section': !showDataSection }">
      <!-- Kaart sectie -->
      <div class="map-section">
        <div class="map-container" ref="mapContainer">
          <div ref="svgContainer" v-html="svgContent"></div>
        </div>
      </div>

      <!-- Data sectie naast de kaart -->
      <div v-if="showDataSection" class="data-section">
        <div v-if="selectedProvincie" class="provincie-info">
          <h3>{{ selectedProvincie.name }}</h3>
          <p v-if="kieskringen && kieskringen.length > 0" class="kieskringen-text">
            <strong>Kieskringen:</strong> {{ kieskringen.map(k => cleanKieskringName(k.naam)).join(', ') }}
          </p>
          <p><strong>Stemmen:</strong> {{ selectedProvincie.stemmen || 'Laden...' }}</p>
          <div v-if="selectedProvincie.resultaten && selectedProvincie.resultaten.length > 0" class="resultaten">
            <h4>Verkiezingsresultaten 2023:</h4>
            <div class="partij-lijst">
              <div
                v-for="partij in selectedProvincie.resultaten"
                :key="partij.naam"
                class="partij-item"
              >
                <span class="partij-naam">{{ partij.naam }}</span>
                <span class="partij-stemmen">{{ partij.stemmen }} ({{ partij.percentage }}%)</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Placeholder als geen provincie geselecteerd -->
        <div v-else class="no-selection">
          <h3>Selecteer een provincie</h3>
          <p>Klik op een provincie in de kaart om verkiezingsresultaten te bekijken</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineEmits } from 'vue'
import { ProvincieService } from '../services/ProvincieService.js'

const mapContainer = ref(null)
const svgContainer = ref(null)
const svgContent = ref('')
const selectedProvincie = ref(null)
const kieskringen = ref([])
const props = defineProps({
  showDataSection: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['provincie-selected', 'provincieDataForChart'])

onMounted(async () => {
  try {
    const response = await fetch('/images/NederlandSVG.svg')
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    svgContent.value = await response.text()
    setTimeout(() => addPathListeners(), 200)
  } catch {
    svgContent.value = '<div style="text-align: center; padding: 50px; color: #666;">Kon SVG niet laden. Controleer of /images/NederlandSVG.svg bestaat.</div>'
  }
})

const addPathListeners = () => {
  if (!svgContainer.value) return

  const paths = svgContainer.value.querySelectorAll('path[id*="provincie"], path[id*="province"]')

  paths.forEach(path => {
    path.style.fill = '#ffffff'
    path.style.fillOpacity = '1'

    path.addEventListener('mouseenter', (e) => {
      path.style.fill = '#1a237e'
      path.style.fillOpacity = '0.8'
      path.style.cursor = 'default'
      const provincieNaam = getProvincieNameFromPath(path)
      if (provincieNaam) showTooltip(e, provincieNaam)
    })

    path.addEventListener('mouseleave', () => {
      if (selectedProvincie.value?.name !== getProvincieNameFromPath(path)) {
        path.style.fill = '#ffffff'
        path.style.fillOpacity = '1'
      }
      hideTooltip()
    })

      path.addEventListener('click', async () => {
      const provincieNaam = getProvincieNameFromPath(path)
      if (provincieNaam) {
        selectedProvincie.value = { name: provincieNaam, stemmen: 'Laden...', resultaten: [] }
        kieskringen.value = []
        paths.forEach(p => { p.style.fill = '#ffffff'; p.style.fillOpacity = '1' })
        path.style.fill = '#1a237e'
        path.style.fillOpacity = '0.8'
        emit('provincie-selected', provincieNaam)
        await loadProvincieData(provincieNaam)
        
        // Emit data in format voor ChartsPanel
        if (selectedProvincie.value.resultaten && selectedProvincie.value.resultaten.length > 0) {
          const chartData = {
            name: provincieNaam,
            id: provincieNaam.toLowerCase().replace(/\s+/g, '_'),
            validVotes: selectedProvincie.value.stemmen || 0,
            allParties: selectedProvincie.value.resultaten.map(partij => ({
              id: partij.naam.toLowerCase().replace(/\s+/g, '_'),
              name: partij.naam,
              votes: partij.stemmen || 0
            }))
          }
          emit('provincieDataForChart', chartData)
        }
      }
    })
  })
}

const getProvincieNameFromPath = (path) => {
  const id = path.id || path.getAttribute('data-provincie')
  if (!id) return 'Onbekende Provincie'

  const cleanName = id.toLowerCase().replace(/_province$|_provincie$|_prov$/, '').replace(/[_\s-]/g, ' ').trim()

  const provincieMapping = {
    'groningen': 'Groningen', 'friesland': 'Friesland', 'drenthe': 'Drenthe',
    'overijssel': 'Overijssel', 'flevoland': 'Flevoland', 'gelderland': 'Gelderland',
    'utrecht': 'Utrecht', 'noord holland': 'Noord-Holland', 'noord-holland': 'Noord-Holland',
    'north holland': 'Noord-Holland', 'zuid holland': 'Zuid-Holland', 'zuid-holland': 'Zuid-Holland',
    'south holland': 'Zuid-Holland', 'zeeland': 'Zeeland', 'noord brabant': 'Noord-Brabant',
    'noord-brabant': 'Noord-Brabant', 'north brabant': 'Noord-Brabant', 'limburg': 'Limburg'
  }

  return provincieMapping[cleanName] || cleanName.split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

const cleanKieskringName = (name) => {
  if (!name) return name

  // Vervang underscores met spaties
  let cleaned = name.replace(/_/g, ' ')

  cleaned = cleaned.split(' ').map(word => {
    if (word.includes('-')) {
      // Voor namen met streepjes zoals "s-Gravenhage"
      const parts = word.split('-')
      return parts.map(part => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase()).join('-')
    }
    return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
  }).join(' ')

  return cleaned
}

const loadProvincieData = async (provincieNaam) => {
  try {
    const provincieData = await ProvincieService.getProvincieData(provincieNaam)
    selectedProvincie.value = {
      ...selectedProvincie.value,
      stemmen: provincieData.resultaten?.totaalStemmen?.toLocaleString() || '0',
      resultaten: provincieData.resultaten?.partijen || []
    }

    // Haal ook de kieskringen op voor deze provincie
    try {
      const kieskringenData = await ProvincieService.getKieskringenInProvincie(provincieNaam)
      kieskringen.value = kieskringenData || []
    } catch {
      kieskringen.value = []
    }
  } catch {
    selectedProvincie.value = {
      ...selectedProvincie.value,
      stemmen: 'Error',
      resultaten: []
    }
    kieskringen.value = []
  }
}

const showTooltip = (event, text) => {
  const tooltip = document.createElement('div')
  tooltip.className = 'provincie-tooltip'
  tooltip.textContent = text
  tooltip.style.cssText = `
    position: absolute;
    background: rgba(26, 35, 126, 0.9);
    color: white;
    padding: 8px 12px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    pointer-events: none;
    z-index: 1000;
    left: ${event.pageX + 10}px;
    top: ${event.pageY - 10}px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    border: 1px solid rgba(255,255,255,0.2);
  `
  document.body.appendChild(tooltip)
}

const hideTooltip = () => {
  const tooltip = document.querySelector('.provincie-tooltip')
  if (tooltip) {
    tooltip.remove()
  }
}
</script>

<style scoped>
/* Wrapper transparant maken */
.dutch-map-wrapper {
  background: transparent;
}

/* Layout voor kaart en data naast elkaar */
.map-layout {
  display: flex;
  gap: 2rem;
  align-items: flex-start;
  background: transparent;
}

.map-layout.no-data-section {
  display: block; /* Geen flex wanneer data-sectie verborgen is */
  background: transparent;
}

.map-section {
  flex: 1;
  min-width: 0; /* Voorkom overflow */
  background: transparent;
}

.map-layout.no-data-section .map-section {
  width: 100%;
  background: transparent;
}

.map-container {
  width: 100%;
  height: 100%;
  min-height: 500px;
  border: none;
  border-radius: 0;
  overflow: visible;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: default;
}

.data-section {
  flex: 1;
  min-width: 300px; /* Minimum breedte voor data */
}

.map-container > div {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center; /* Centreer de SVG */
}

.provincie-info {
  background: rgba(26, 35, 126, 0.1);
  border: 2px solid #1a237e;
  border-radius: 8px;
  padding: 1.5rem;
  margin: 0;
  text-align: left; /* Links uitlijnen voor naast kaart */
  animation: slideIn 0.3s ease-out;
  height: fit-content;
}

.no-selection {
  background: #f8f9fa;
  border: 2px solid #ddd;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  color: #666;
  height: fit-content;
  cursor: default; /* Normale cursor */
}

/* Fix cursor voor alle data sectie elementen */
.data-section * {
  cursor: default !important;
}

.data-section h3,
.data-section p,
.data-section h4 {
  cursor: default !important;
}

.partij-item {
  cursor: default !important;
}

.partij-naam,
.partij-stemmen {
  cursor: default !important;
}

.provincie-info h3 {
  color: #1a237e;
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
}

.provincie-info p {
  color: #424242;
  margin: 0.25rem 0;
  font-size: 1.1rem;
}

.resultaten {
  margin-top: 1rem;
  text-align: left;
}

.resultaten h4 {
  color: #1a237e;
  margin-bottom: 0.5rem;
}

.partij-lijst {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.partij-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  background: white;
  border-radius: 4px;
  border-left: 4px solid #1a237e;
}

.partij-naam {
  font-weight: 500;
}

.partij-stemmen {
  color: #666;
  font-size: 0.9rem;
}

.kieskringen-text {
  color: #424242;
  font-size: 1rem;
  margin: 0.25rem 0;
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

@media (max-width: 1024px) {
  .map-layout {
    flex-direction: column; /* Stack op kleinere schermen */
    gap: 1rem;
  }

  .map-container {
    height: 500px;
  }

  .data-section {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .map-container {
    height: 400px;
  }

  .provincie-info {
    padding: 1rem;
  }
}

:deep(svg) {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  display: block;
  margin: 0 auto; /* Centreer de SVG */
}

:deep(path) {
  transition: fill 0.2s ease, fill-opacity 0.2s ease;
  cursor: default !important; /* Normale muis cursor in plaats van pointer */
  stroke: none; /* Geen stroke standaard */
  stroke-width: 0;
}

/* Alleen provincie paths krijgen een stroke */
:deep(path[id*="provincie"], path[id*="province"]) {
  stroke: #333; /* Donkere grenzen voor provincies */
  stroke-width: 1;
}

/* Fix cursor voor alle SVG elementen */
:deep(svg) {
  cursor: default !important;
}

:deep(svg *) {
  cursor: default !important;
}

/* Verberg alle interne grenzen - maak gemeenten/kieskringen onzichtbaar */
:deep(path:not([id*="provincie"]):not([id*="province"])) {
  display: none !important; /* Verberg alle interne paths volledig */
}

/* Alleen provincie paths zijn zichtbaar */
:deep(path[id*="provincie"], path[id*="province"]) {
  display: block !important;
}
</style>
