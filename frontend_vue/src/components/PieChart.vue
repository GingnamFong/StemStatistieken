<template>
  <div>
    <Pie :data="chartConfig.data" :options="chartConfig.options" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  data: Array,
})

// ✅ make it reactive
const chartConfig = computed(() => ({
  data: {
    labels: props.data.map(p => p.name),
    datasets: [
      {
        data: props.data.map(p => p.votes),
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'],
      },
    ],
  },
  options: {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
    },
  },
}))
</script>
