import { API_BASE_URL } from '../config/api.js'

export const PollingStationService = {
  /**
   * Get ALL polling stations for a given election
   * GET /elections/{electionId}/pollingstations
   */
  async getAll(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/pollingstations`)
    if (!response.ok) throw new Error(`Failed to load polling stations for ${electionId}`)
    return await response.json()
  },

  /**
   * Get polling stations by postal code
   * GET /elections/{electionId}/pollingstations/postcode/{postalCode}
   */
  async getByPostalCode(electionId, postalCode) {
    const clean = postalCode.replace(/\s+/g, '').toUpperCase()

    const response = await fetch(
      `${API_BASE_URL}/elections/${electionId}/pollingstations/postcode/${clean}`
    )

    if (!response.ok)
      throw new Error(`No polling station found for postal code ${clean}`)

    return await response.json()
  },

  /**
   * Get polling station by ID
   */
  async getById(electionId, stationId) {
    const encoded = encodeURIComponent(stationId)
    const response = await fetch(
      `${API_BASE_URL}/elections/${electionId}/pollingstations/${encoded}`
    )
    if (!response.ok) throw new Error(`Failed to load polling station ${stationId}`)
    return await response.json()
  }
}

export default PollingStationService
