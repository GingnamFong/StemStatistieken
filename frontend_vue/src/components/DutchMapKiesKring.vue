<template>
  <div class="map-container" ref="container">
    <!-- injected SVG -->
    <div ref="svgContainer" v-html="svgContent"></div>

    <!-- Tooltip -->
    <div
      v-if="tooltip.visible"
      class="tooltip"
      :style="{ top: tooltip.y + 'px', left: tooltip.x + 'px' }"
    >
      <strong>{{ tooltip.name }}</strong>
      <ul v-if="tooltip.topParties.length">
        <li v-for="p in tooltip.topParties" :key="p.id">
          {{ p.name }} — {{ p.votes.toLocaleString() }}
          <span v-if="tooltip.validVotes && p.votes">
            ({{ ((p.votes / tooltip.validVotes) * 100).toFixed(1) }}%)
          </span>
        </li>
      </ul>
      <div v-else class="no-data">No data</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { API_BASE_URL } from '@/config/api'
import { watch } from 'vue'
import svgRaw from "@/assets/kieskringen.svg?raw"

const emit = defineEmits(['regionSelected'])
const container = ref(null)
const svgContainer = ref(null)
const svgContent = ref('')

const constituencies = ref([])

const props = defineProps({
  year: {
    type: Number,
    required: true
  }
})



const tooltip = ref({
  visible: false,
  name: '',
  topParties: [],
  validVotes: 0,
  x: 0,
  y: 0,
})

// normalize names so "’s-Gravenhage" ~= "s-gravenhage"
function norm(s) {
  return (s || '')
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[’'`´]/g, '')      // quotes
    .replace(/\s+/g, ' ')        // collapse spaces
    .trim()
}

// sum parties across all municipalities in a constituency
function aggregateParties(c) {
  const map = new Map()
  let total = 0
  for (const m of c?.municipalities || []) {
    total += m.validVotes || 0
    for (const p of m.allParties || []) {
      const cur = map.get(p.id) || { id: p.id, name: p.name, votes: 0 }
      cur.votes += p.votes || 0
      map.set(p.id, cur)
    }
  }
  const sorted = [...map.values()].sort((a, b) => b.votes - a.votes)
  return { totalVotes: total, sorted }
}

function attachInteractivity() {
  const svgEl = svgContainer.value?.querySelector('svg')
  if (!svgEl) return

  // style fallback if the file had inline fills
  svgEl.querySelectorAll('path').forEach((p) => {
    // Use dataset.cid to cache the matched constituency id
    // Try match by id number first (kieskring-19 -> 19)
    const idText = p.getAttribute('id') || ''
    const numeric = idText.match(/\d+/)?.[0]
    let match =
      constituencies.value.find((c) => String(c.id) === String(numeric)) || null

    // If no match by number, try by <title> text (e.g., "Groningen")
    if (!match) {
      const titleText = p.querySelector('title')?.textContent || ''
      const byName = constituencies.value.find(
        (c) => norm(c.name) === norm(titleText)
      )
      if (byName) match = byName
    }

    if (match) {
      console.log('Path ID:', idText, '→ Matched:', match?.name || '❌ none')
      p.dataset.cid = match.id // stash for quick lookup
    }

    // Mouse events
    p.addEventListener('mousemove', (e) => {
      const cid = p.dataset.cid
      const c = constituencies.value.find((x) => String(x.id) === String(cid))
      const name =
        c?.name ||
        p.querySelector('title')?.textContent ||
        idText ||
        'Unknown region'

      const rect = container.value?.getBoundingClientRect?.() || {
        left: 0,
        top: 0,
      }
      const offsetX = e.clientX - rect.left
      const offsetY = e.clientY - rect.top

      const { totalVotes, sorted } = aggregateParties(c)

      tooltip.value = {
        visible: true,
        name,
        validVotes: totalVotes,
        topParties: sorted.slice(0, 3),
        x: offsetX + 12,
        y: offsetY + 12,
      }
    })

    p.addEventListener('mouseleave', () => {
      tooltip.value.visible = false
    })

    p.addEventListener('click', () => {
      const cid = p.dataset.cid
      const c = constituencies.value.find((x) => String(x.id) === String(cid))
      if (c) emit('regionSelected', c)
    })
  })
}

async function loadData() {
  // 1) load backend data
  try {
    const res = await fetch(
      `${API_BASE_URL}/elections/TK${props.year}/constituencies`
    )
    if (!res.ok) throw new Error('Network error')
    constituencies.value = await res.json()
  } catch (err) {
    console.error('Failed to load constituencies:', err)
  }

  svgContent.value = svgRaw

  // 3) after injection, attach logic
  await nextTick()
  attachInteractivity()
}

// Load initial data
  onMounted(loadData)

// Watch year change
  watch(() => props.year, async () => {
    await loadData()
})
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 520px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* IMPORTANT: styles must pierce the injected SVG */
:deep(svg) {
  width: 85%;
  max-width: 650px;
  height: auto;
}

:deep(svg path:hover) {
  fill: #ffd24d !important;
  stroke: #555 !important;
  stroke-width: 1.1 !important;
}

.tooltip {
  position: absolute;
  background: rgba(0, 0, 0, 0.92);
  color: #fff;
  padding: 8px 10px;
  border-radius: 6px;
  pointer-events: none;
  font-size: 0.85rem;
  min-width: 180px;
  z-index: 50;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.tooltip strong {
  color: #ffd24d;
}
.tooltip ul {
  margin: 6px 0 0;
  padding: 0;
  list-style: none;
}
.tooltip li { margin: 3px 0; font-size: 0.82rem; }
.no-data { font-size: 0.8rem; opacity: 0.8; }
</style>
