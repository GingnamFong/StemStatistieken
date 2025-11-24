<template>
  <div class="map-container" ref="container">
    <NederlandMap2021
      class="dutch-map"
      @mousemove="handleMouseMove"
      @mouseleave="hideTooltip"
      @click="handleClick"
    />

    <div
      v-if="tooltip.visible"
      class="tooltip"
      :style="{ top: tooltip.y + 'px', left: tooltip.x + 'px' }"
    >
      <strong>{{ tooltip.name }}</strong>

      <ul v-if="tooltip.topParties && tooltip.topParties.length">
        <li v-for="p in tooltip.topParties" :key="p.id">
          {{ p.name }} — {{ p.votes.toLocaleString() }} votes
          <span v-if="tooltip.validVotes && p.votes">
            ({{ ((p.votes / tooltip.validVotes) * 100).toFixed(1) }}%)
          </span>
        </li>
      </ul>

      <div v-else style="font-size:0.8rem; opacity:0.8;">No data</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { defineEmits } from 'vue'
import NederlandMap2021 from '@/assets/Nederland_gemeente_2021.svg'
import { API_BASE_URL } from '@/config/api'

const container = ref(null)
const emit = defineEmits(['municipalitySelected'])
defineProps({ year: Number })
let lastHoveredPath = null

const tooltip = ref({
  visible: false,
  name: '',
  topParties: [],
  validVotes: 0,
  x: 0,
  y: 0,
})

const municipalities = ref([])



onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/elections/TK2021/municipalities`)
    if (!res.ok) throw new Error('Network error')
    municipalities.value = await res.json()
    console.log('Loaded 2021 municipalities:', municipalities.value)
  } catch (err) {
    console.error('Failed to load 2021 municipalities:', err)
  }
})

function getMunicipalityByPathId(pathId) {
  if (!pathId) return null
  const id = pathId.replace(/^\D+/, '')
  return municipalities.value.find(m => String(m.id) === String(id)) || null
}

function handleMouseMove(e) {
  const path = e.target.closest('path')

  // Remove highlight from previous
  if (lastHoveredPath && lastHoveredPath !== path) {
    lastHoveredPath.classList.remove('hovered-municipality')
  }

  if (!path) {
    tooltip.value.visible = false
    lastHoveredPath = null
    return
  }

  // Add highlight
  path.classList.add('hovered-municipality')
  lastHoveredPath = path

  const m = getMunicipalityByPathId(path.id)
  const name = (m && m.name) || path.dataset.name || path.id || 'Unknown'

  const rect = container.value?.getBoundingClientRect?.() || { left: 0, top: 0 }
  const offsetX = e.clientX - rect.left
  const offsetY = e.clientY - rect.top

  tooltip.value.visible = true
  tooltip.value.name = name
  tooltip.value.validVotes = m?.validVotes || 0
  tooltip.value.topParties = m?.allParties
    ? [...m.allParties].sort((a, b) => b.votes - a.votes).slice(0, 3)
    : []
  tooltip.value.x = offsetX + 12
  tooltip.value.y = offsetY + 12
}

function hideTooltip() {
  tooltip.value.visible = false
  if (lastHoveredPath) {
    lastHoveredPath.classList.remove('hovered-municipality')
    lastHoveredPath = null
  }

}

function handleClick(e) {
  const path = e.target.closest('path')
  if (!path) return
  const m = getMunicipalityByPathId(path.id)
  if (m) emit('municipalitySelected', m)


}
</script>

<style>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  margin: 0 auto;
  justify-content: center;
  align-items: center;
}
.hovered-municipality {
  fill: #F6C700 !important;
  stroke: #000 !important;
  stroke-width: 0.6;
  transition: fill 0.12s, stroke 0.12s;
}


/* Ensure the SVG scales but keep pointer events */
.dutch-map {
  width: 80%;
  max-width: 600px; /* prevents the SVG from being huge */
  height: auto;
  cursor: pointer;
  transition: transform 0.2s ease;
}

/* base path styles (these target all paths in the injected SVG) */
path {
  fill: #e0e0e0;
  stroke: #777;
  stroke-width: 0.4;
  transition: fill 0.12s;
}
.dutch-map:hover {
  transform: scale(1.02);
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
  line-height: 1.3;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}
.tooltip strong {
  font-size: 0.9rem;
  color: #ffd24d;
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

