import { computed } from 'vue'

export function useForumFilters(posts, filters, selectedSort) {
  const activeFilterCount = computed(() => {
    let count = 0
    if (filters.searchQuery.value.trim()) count++
    if (filters.filterAuthor.value.trim()) count++
    if (filters.filterDate.value) count++
    if (filters.filterMinScore.value > 0) count++
    if (filters.filterMinComments.value > 0) count++
    return count
  })

  const sortedPosts = computed(() => {
    let filtered = [...posts.value]

    if (filters.searchQuery.value.trim()) {
      const query = filters.searchQuery.value.toLowerCase().trim()
      filtered = filtered.filter(post =>
        post.title?.toLowerCase().includes(query) ||
        post.content?.toLowerCase().includes(query)
      )
    }

    if (filters.filterAuthor.value.trim()) {
      const authorQuery = filters.filterAuthor.value.toLowerCase().trim()
      filtered = filtered.filter(post =>
        post.author?.toLowerCase().includes(authorQuery)
      )
    }

    if (filters.filterDate.value) {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const ranges = {
        today,
        week: new Date(today.getTime() - 7 * 86400000),
        month: new Date(today.getTime() - 30 * 86400000),
        year: new Date(today.getTime() - 365 * 86400000)
      }

      filtered = filtered.filter(post => {
        const postDate = new Date(post.createdAt)
        return postDate >= ranges[filters.filterDate.value]
      })
    }

    if (filters.filterMinScore.value > 0) {
      filtered = filtered.filter(post => post.score >= filters.filterMinScore.value)
    }

    if (filters.filterMinComments.value > 0) {
      filtered = filtered.filter(post => post.comments >= filters.filterMinComments.value)
    }

    switch (selectedSort.value) {
      case 'new':
        return filtered.sort((a, b) => b.createdAt - a.createdAt)
      case 'top':
        return filtered.sort((a, b) => b.score - a.score)
      default:
        return filtered.sort((a, b) =>
          (b.score - a.score) * 0.7 -
          ((b.createdAt - a.createdAt) / 3600000) * 0.3
        )
    }
  })

  return {
    sortedPosts,
    activeFilterCount
  }
}
