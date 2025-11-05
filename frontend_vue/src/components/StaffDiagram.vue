<template>
  <div class="staff-chart-wrapper">
    <div class="chart-controls">
      <div class="control-group">
        <button class="ctrl-btn" :class="{ active: viewMode === 'votes' }" @click="viewMode = 'votes'">Stemmen</button>
        <button class="ctrl-btn" :class="{ active: viewMode === 'percent' }" @click="viewMode = 'percent'">Percentage</button>
      </div>

      <div class="control-group">
        <button class="ctrl-btn" :class="{ active: sortMode === 'desc' }" @click="sortMode = 'desc'">Hoog → Laag</button>
        <button class="ctrl-btn" :class="{ active: sortMode === 'asc' }" @click="sortMode = 'asc'">Laag → Hoog</button>
      </div>

      
    </div>

    <div class="chart-area" :style="{ height: chartHeightPx + 'px' }">
      <Bar :data="chartConfig.data" :options="chartConfig.options" :plugins="[valueLabelPlugin]" />
    </div>
  </div>
  </template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const props = defineProps({
  data: Array,
})

// Always horizontal for readability
const indexAxis = ref('y')

// Controls
const viewMode = ref('votes') // 'votes' | 'percent'
const sortMode = ref('desc') // 'desc' | 'asc'
// always show all

// Derived dataset based on controls
const processed = computed(() => {
  const base = (props.data || []).map(p => ({
    id: p.id,
    name: p.name,
    votes: p.votes || 0,
    color: p.color || '#3b82f6',
  }))

  const total = base.reduce((a, b) => a + b.votes, 0)
  const withPct = base.map(p => ({ ...p, percent: total ? (p.votes / total) * 100 : 0 }))

  withPct.sort((a, b) => sortMode.value === 'asc' ? (a.votes - b.votes) : (b.votes - a.votes))
  return withPct
})

// Dynamic height: 34px per bar + padding; clamp
const chartHeightPx = computed(() => {
  const rows = Math.max(1, processed.value.length)
  return Math.min(560, Math.max(260, rows * 34 + 80))
})

// 🎨 Configuratie
const chartConfig = computed(() => {
  const labels = processed.value.map((p) => p.name)
  const values = processed.value.map((p) => viewMode.value === 'percent' ? Number(p.percent.toFixed(2)) : p.votes)
  const total = processed.value.reduce((a, b) => a + (b.votes || 0), 0)

  return {
    data: {
      labels,
      datasets: [
        {
          label: viewMode.value === 'percent' ? 'Percentage' : 'Stemmen',
          data: values,
          backgroundColor: processed.value.map((p) => p.color || '#36A2EB'),
          borderRadius: 8,
          borderSkipped: false,
          maxBarThickness: 32,
          barPercentage: 0.72,
          categoryPercentage: 0.82,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: indexAxis.value,
      layout: { padding: { right: 56, left: 8, top: 8, bottom: 8 } },
      plugins: {
        legend: { display: false },
        title: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => {
              const v = ctx.parsed[ctx.datasetIndex === 0 ? (indexAxis.value === 'y' ? 'x' : 'y') : 'y'] || ctx.parsed
              const pct = total ? ((v / total) * 100).toFixed(1) : 0
              if (viewMode.value === 'percent') {
                return `${ctx.label}: ${Number(ctx.formattedValue).toLocaleString('nl-NL')}%`
              }
              return `${ctx.label}: ${v.toLocaleString('nl-NL')} (${pct}%)`
            },
          },
        },
      },
      scales: {
        x: {
          ticks: {
            color: '#334155',
            font: { size: 11 },
            callback: (val) => viewMode.value === 'percent' ? `${val}%` : (Number(val).toLocaleString('nl-NL')),
          },
          grid: {
            color: '#f1f5f9',
          },
        },
        y: {
          beginAtZero: true,
          ticks: {
            color: '#475569',
            font: { size: 11 },
          },
          grid: {
            color: '#e2e8f0',
          },
        },
      },
      animation: { duration: 400 },
    },
  }
})

// Lightweight value label plugin (no external deps)
const valueLabelPlugin = {
  id: 'valueLabel',
  afterDatasetsDraw(chart, args, pluginOptions) {
    const { ctx, chartArea, scales } = chart
    const dataset = chart.getDatasetMeta(0)
    if (!dataset || !dataset.data) return

    ctx.save()
    ctx.font = '600 11px Nunito, system-ui, -apple-system, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif'
    ctx.fillStyle = '#334155'

    dataset.data.forEach((bar, i) => {
      const value = chart.data.datasets[0].data[i]
      if (value == null) return

      // position near end of bar (horizontal bars)
      const x = bar.x
      const y = bar.y
      const text = Array.isArray(value)
        ? String(value[0])
        : (typeof value === 'number' ? value : Number(value))

      // Determine label string based on mode
      // We rely on closure to access viewMode and processed
      const label = viewMode.value === 'percent'
        ? `${Number(text).toFixed(1)}%`
        : `${Number(text).toLocaleString('nl-NL')}`

      const padding = 6
      const measured = ctx.measureText(label).width
      const leftEdge = chartArea.left
      const within = x - leftEdge

      // If bar is too short to fit label inside, place outside to the right
      const placeOutside = within < measured + padding * 2
      const drawX = placeOutside ? Math.min(chartArea.right - measured - 4, x + 6) : x - measured - 6
      const drawY = y + 3 // slight vertical centering tweak

      ctx.fillText(label, drawX, drawY)
    })

    ctx.restore()
  },
}
</script>

<style scoped>
.staff-chart-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: center;
  justify-content: center;
}

.control-group { display: flex; gap: 6px; }

.ctrl-btn {
  background: #ffffff;
  color: #334155;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.ctrl-btn:hover { background: #f8fafc; }
.ctrl-btn.active { background: #3b82f6; border-color: #3b82f6; color: #ffffff; box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25); }

.chart-area { width: 100%; }

/* Fit chart to container */
:deep(canvas) { width: 100% !important; height: 100% !important; }
</style>
