import { API_BASE_URL } from '../config/api.js'

export const PollingstationService = {
  /**
   * Get ALL polling stations for a given election year
   * GET /electionresults/{year}/pollingstations
   */
  async getAll(year) {
    const response = await fetch(`${API_BASE_URL}/electionresults/${year}/pollingstations`)
    if (!response.ok) throw new Error(`Failed to load polling stations for ${year}`)
    return await response.json()
  },

  /**
   * Get polling stations filtered by postal code.
   * Your backend: GET /electionresults/{year}/pollingstations/search?from=X&to=Y
   */
  async getByPostalCode(year, postalCode) {
    const clean = postalCode.replace(/\s+/g, '').toUpperCase()

    const response = await fetch(
      `${API_BASE_URL}/electionresults/${year}/pollingstations/search?from=${clean}&to=${clean}`
    )

    if (!response.ok) throw new Error(`Failed to search polling stations for postal code ${clean}`)
    return await response.json()
  },

  /**
   * Get one specific polling station
   * GET /electionresults/{year}/pollingstations/{id}
   */
  async getById(year, stationId) {
    const response = await fetch(
      `${API_BASE_URL}/electionresults/${year}/pollingstations/${encodeURIComponent(stationId)}`
    )
    if (!response.ok) throw new Error(`Failed to load polling station ${stationId}`)
    return await response.json()
  }
}

export default PollingstationService
