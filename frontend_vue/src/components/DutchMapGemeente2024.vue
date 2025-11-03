<template>
  <div class="map-container" ref="container">
    <!-- SVG Map (imported via vite-svg-loader) -->
    <NederlandMap
      class="dutch-map"
      @mousemove="handleMouseMove"
      @mouseleave="hideTooltip"
      @click="handleClick"
    />

    <!-- Tooltip -->
    <div
      v-if="tooltip.visible"
      class="tooltip"
      :style="{ top: tooltip.y + 'px', left: tooltip.x + 'px' }"
    >
      <strong>{{ tooltip.name }}</strong>

      <ul v-if="tooltip.topParties && tooltip.topParties.length">
        <li v-for="p in tooltip.topParties" :key="p.id">
          {{ p.name }} — {{ p.votes.toLocaleString() }} votes
        </li>
      </ul>

      <div v-else style="font-size:0.8rem; opacity:0.8;">No data</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NederlandMap from '@/assets/Nederland_gemeenten_2024.svg'

const container = ref(null)

// tooltip state: note the property name `topParties`
const tooltip = ref({
  visible: false,
  name: '',
  topParties: [],
  x: 0,
  y: 0,
})

// store fetched municipality data
const municipalities = ref([])

onMounted(async () => {
  try {
    const res = await fetch('http://localhost:8081/elections/TK2023/municipalities')
    if (!res.ok) throw new Error('Network error')
    municipalities.value = await res.json()
  } catch (err) {
    console.error('Failed to load municipalities:', err)
  }
})

// Utility to get municipality data by svg path id (robust to "GM" prefix)
function getMunicipalityByPathId(pathId) {
  if (!pathId) return null
  // remove any non-digit prefix (e.g. "GM0482" -> "0482")
  const id = pathId.replace(/^\D+/, '')
  return municipalities.value.find(m => String(m.id) === String(id)) || null
}

function handleMouseMove(e) {
  const path = e.target.closest('path')
  if (!path) {
    tooltip.value.visible = false
    return
  }

  const m = getMunicipalityByPathId(path.id)
  const name = (m && m.name) || path.dataset.name || path.id || 'Unknown'

  // compute tooltip position relative to container so it positions correctly
  const rect = container.value?.getBoundingClientRect?.() || { left: 0, top: 0 }
  const offsetX = e.clientX - rect.left
  const offsetY = e.clientY - rect.top

  tooltip.value.visible = true
  tooltip.value.name = name
  tooltip.value.topParties = (m && m.topParties) ? m.topParties.slice(0, 3) : []
  // place tooltip slightly offset from cursor
  tooltip.value.x = offsetX + 12
  tooltip.value.y = offsetY + 12
}

function hideTooltip() {
  tooltip.value.visible = false
}

function handleClick(e) {
  const path = e.target.closest('path')
  if (!path) return
  const m = getMunicipalityByPathId(path.id)
  const name = (m && m.name) || path.dataset.name || path.id

  alert(`Clicked on ${name}`)
}
</script>

<style scoped>
.map-container {
  position: relative;
  max-width: 1000px;
  margin: 0 auto;
}

/* Ensure the SVG scales but keep pointer events */
.dutch-map {
  width: 100%;
  height: auto;
  display: block;
  cursor: pointer;
}

/* base path styles (these target all paths in the injected SVG) */
path {
  fill: #ccc;
  stroke: #6e6e6e;
  stroke-width: 0.5;
  transition: fill 0.12s;
}

path:hover {
  fill: #ffd24d;
}

/* tooltip */
.tooltip {
  position: absolute;
  transform: translate(0, 0);
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 8px 10px;
  border-radius: 6px;
  pointer-events: none;
  font-size: 0.85rem;
  min-width: 160px;
  z-index: 50;
}

.tooltip ul {
  margin: 6px 0 0;
  padding: 0;
  list-style: none;
}

.tooltip li {
  margin: 3px 0;
  font-size: 0.82rem;
}
</style>
