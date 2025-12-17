import { API_BASE_URL } from '../config/api.js'

/**
 * Service for stemwijzer (voting guide) functionality.
 * Handles API calls for calculating matches and saving favorite parties.
 */
export const StemwijzerService = {
  /**
   * Calculates match scores between user answers and party positions.
   * @param {Map<number, string>} answers - Map of questionId to answer
   * @returns {Promise<Array>} List of match results sorted by percentage
   */
  async calculateMatches(answers) {
    const response = await fetch(`${API_BASE_URL}/api/stemwijzer/calculate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(answers)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      const errorMessage = errorData.message || errorData.error || 'Failed to calculate matches'
      throw new Error(errorMessage)
    }

    const results = await response.json()

    // Check if response is an error object
    if (results.error) {
      throw new Error(results.message || results.error)
    }

    return results
  },

  /**
   * Sets the favorite party for a user based on stemwijzer results.
   * @param {number} userId - User ID
   * @param {string} partyId - Party ID to set as favorite
   * @returns {Promise<Object>} Response data
   */
  async setFavoriteParty(userId, partyId) {
    const response = await fetch(`${API_BASE_URL}/api/stemwijzer/favorite-party/${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ partyId })
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`Server error: ${response.status} - ${errorText}`)
    }

    return await response.json()
  }
}

export default StemwijzerService
