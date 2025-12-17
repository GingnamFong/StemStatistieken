import { API_BASE_URL } from '../config/api.js'

export const ElectionService = {
  async getElection(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}`)
    if (!response.ok) throw new Error(`Failed to get election ${electionId}`)
    return await response.json()
  },

  /**
   * Retrieves all available election years from the backend.
   * Extracts years from election IDs (e.g., "TK2021" -> "2021").
   * @returns {Promise<Array<string>>} List of years sorted descending (newest first)
   */
  async getAvailableYears() {
    try {
      const response = await fetch(`${API_BASE_URL}/elections`)
      if (!response.ok) throw new Error('Failed to load elections')
      const electionIds = await response.json()
      // Extract years from election IDs (e.g., "TK2021" -> "2021")
      const years = electionIds
        .map(id => {
          const match = id.match(/TK(\d{4})/)
          return match ? match[1] : null
        })
        .filter(year => year !== null)
        .sort((a, b) => b.localeCompare(a)) // Sort descending (newest first)
      return years
    } catch (error) {
      console.error('Error loading available years:', error)
      // Fallback to hardcoded years if API fails
      return ['2025', '2023', '2021']
    }
  }
}

export default ElectionService

