<template>
  <div>
    <Bar :data="chartConfig.data" :options="chartConfig.options" />
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

// ✅ make it reactive
const chartConfig = computed(() => ({
  data: {
    labels: props.data.map(p => p.name),
    datasets: [
      {
        label: 'Votes',
        data: props.data.map(p => p.votes),
        backgroundColor: '#36A2EB',
      },
    ],
  },
  options: {
    indexAxis: 'y',
    plugins: {
      legend: { display: false },
      title: { display: true, text: 'Top 5 Parties' },
    },
    scales: {
      x: { beginAtZero: true },
    },
  },
}))
</script>
