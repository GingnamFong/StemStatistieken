<template>
  <div>
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

// ✅ make it reactive
const chartConfig = computed(() => ({
  data: {
    labels: props.data.map(p => p.name),
    datasets: [
      {
        data: props.data.map(p => p.votes),
        backgroundColor: partyColors.slice(0, props.data.length),
      },
    ],
  },
  options: {
    responsive: true,
    plugins: {
      legend: { position: 'bottom' },
      labels: {
        boxWidth: 14,
        font: {size: 11},
        padding: 8,
      },
    },
  },
}))
</script>
