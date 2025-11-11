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
import NederlandMap2021 from '@/assets/Nederland_gemeenten_2021.svg'

const container = ref(null)
const emit = defineEmits(['municipalitySelected'])

const tooltip = ref({
  visible: false,
  name: '',
  topParties: [],
  validVotes: 0,
  x: 0,
  y: 0,
})

const municipalities = ref([])

const API_BASE_URL =
  (location.origin === 'https://hva-frontend.onrender.com')
    ? 'https://hva-backend-c647.onrender.com'
    : 'http://localhost:8081'

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
  if (!path) {
    tooltip.value.visible = false
    return
  }

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
}

function handleClick(e) {
  const path = e.target.closest('path')
  if (!path) return
  const m = getMunicipalityByPathId(path.id)
  if (m) emit('municipalitySelected', m)
}
</script>

<style scoped>
@import './DutchMapGemeente2024.vue'; /* ✅ optional reuse if you move CSS there */
</style>
