<template>
  <div class="staff-chart-wrapper">
    <div class="chart-scroll">
      <Bar :data="chartConfig.data" :options="chartConfig.options" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
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

// 🎨 Configuratie voor compact verticale staafdiagram
const chartConfig = computed(() => ({
  data: {
    labels: props.data.map((p) => p.name),
    datasets: [
      {
        label: 'Votes',
        data: props.data.map((p) => p.votes),
        backgroundColor: props.data.map((p) => p.color || '#36A2EB'),
        borderRadius: 6,
        barPercentage: 0.6, // maakt de staven smaller
        categoryPercentage: 0.8, // wat extra ruimte ertussen
      },
    ],
  },
  options: {
    responsive: true,
    maintainAspectRatio: false,
    indexAxis: 'x', // verticale staven
    plugins: {
      legend: { display: false },
      title: { display: true, text: 'Votes per Party' },
      tooltip: {
        callbacks: {
          label: (ctx) =>
            `${ctx.dataset.label}: ${ctx.formattedValue} votes`,
        },
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#444',
          font: { size: 11 },
          maxRotation: 60,
          minRotation: 60,
        },
        grid: {
          color: '#f0f0f0',
        },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#666',
          font: { size: 11 },
        },
        grid: {
          color: '#e0e0e0',
        },
      },
    },
  },
}))
</script>

<style scoped>
.staff-chart-wrapper {
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.chart-scroll {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
}

/* Chart.js canvas */
canvas {
  min-width: 800px; /* Scroll bij veel partijen */
  max-height: 380px;
}

/* Scrollbar styling */
.chart-scroll::-webkit-scrollbar {
  height: 8px;
}
.chart-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}
.chart-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
