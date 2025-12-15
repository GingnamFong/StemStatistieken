<script setup>
import { onMounted } from 'vue'
import { ForumService } from '@/services/ForumnService.js'

onMounted(async () => {
  try {
    console.log(await ForumService.getLikeCount(1))
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div>Forum smoke test (check console)</div>
</template>
