<template>
  <div class="pie-wrapper">
    <Pie :data="chartConfig.data" :options="chartConfig.options" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import {partyColors} from "@/utils/chartColors.js";
ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  data: Array,
})

const chartConfig = computed(() => {
  if (!props.data || props.data.length === 0) {
    return {
      data: {
        labels: [],
        datasets: [{ data: [], backgroundColor: [], borderColor: '#ffffff', borderWidth: 2 }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        layout: { padding: 8 },
        plugins: { legend: { display: false }, tooltip: { enabled: false } },
      },
    }
  }
  
  const labels = props.data.map(p => p.name)
  const values = props.data.map(p => p.votes)
  const total = values.reduce((a, b) => a + (b || 0), 0)

  return {
    data: {
      labels,
      datasets: [
        {
          data: values,
          backgroundColor: partyColors.slice(0, props.data.length),
          borderColor: '#ffffff',
          borderWidth: 2,
          hoverOffset: 8,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      layout: { padding: 8 },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: '#334155',
            usePointStyle: true,
            pointStyle: 'circle',
            boxWidth: 10,
            boxHeight: 10,
            padding: 16,
            font: { size: 12, weight: '600' },
          },
        },
        tooltip: {
          callbacks: {
            label: (ctx) => {
              const v = ctx.parsed
              const pct = total ? ((v / total) * 100).toFixed(1) : 0
              return `${ctx.label}: ${v.toLocaleString('nl-NL')} (${pct}%)`
            },
          },
        },
      },
      animation: {
        animateRotate: true,
        animateScale: true,
      },
    },
  }
})
</script>

<style scoped>
.pie-wrapper {
  width: 100%;
  height: clamp(240px, 40vh, 360px);
}

/* Chart canvas should fit container */
:deep(canvas) {
  width: 100% !important;
  height: 100% !important;
}
</style>
