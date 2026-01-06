import { computed } from 'vue'

export function useForumFilters(posts, filters, selectedSort) {
  /**
   * Counts how many filters are currently active
   */
  const activeFilterCount = computed(() => {
    let count = 0
    // User types something in searchbar
    if (filters.searchQuery.value.trim()) count++

    // Author name is entered
    if (filters.filterAuthor.value.trim()) count++

    // Data filter (today, week, month, year)
    if (filters.filterDate.value) count++
    // If minimum score is greater than 0

    if (filters.filterMinScore.value > 0) count++
    // If minimum comments is greater than 0

    if (filters.filterMinComments.value > 0) count++
    return count
  })

  /**
   * Returns posts filtered and sorted
   */
  const sortedPosts = computed(() => {
    // Start with all posts
    let filtered = [...posts.value]

    // Search in title and content
    if (filters.searchQuery.value.trim()) {
      const query = filters.searchQuery.value.toLowerCase().trim()

      filtered = filtered.filter(post =>
        post.title?.toLowerCase().includes(query) ||
        post.content?.toLowerCase().includes(query)
      )
    }

    // Author filter by name
    if (filters.filterAuthor.value.trim()) {
      const authorQuery = filters.filterAuthor.value.toLowerCase().trim()
      filtered = filtered.filter(post =>
        post.author?.toLowerCase().includes(authorQuery)
      )
    }

    // Date filter by range (last week, last month, last year, today)
    if (filters.filterDate.value) {
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

      // Calculations based on days/year, weeks and months
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

    // Score filter, only shows posts with enough score
    if (filters.filterMinScore.value > 0) {
      filtered = filtered.filter(post => post.score >= filters.filterMinScore.value)
    }

    // Comment filter, only shows posts with enough comments
    if (filters.filterMinComments.value > 0) {
      filtered = filtered.filter(post => post.comments >= filters.filterMinComments.value)
    }

    // Sort the final list
    switch (selectedSort.value) {

      // Shows newest post first
      case 'new':
        return filtered.sort((a, b) => b.createdAt - a.createdAt)

      // Shows posts with highest score first
      case 'top':
        return filtered.sort((a, b) => b.score - a.score)

      // mix of how new and much likes a post has
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
