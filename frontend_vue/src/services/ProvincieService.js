import { API_BASE_URL } from '../config/api.js'

/**
 * Service for retrieving province data from the backend API.
 * Retrieves provinces, results and constituencies via REST endpoints.
 */
export const ProvincieService = {
  /**
   * Retrieves all provinces for an election.
   * @param {string} electionId - Election ID (e.g. 'TK2023')
   * @returns {Promise<Array>} List of provinces
   */
  async getAllProvincies(electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies?electionId=${electionId}`)
    if (!response.ok) throw new Error('Failed to get provincies')
    return await response.json()
  },

  /**
   * Retrieves election results for a specific province.
   * @param {string} provincieNaam - Name of the province
   * @param {string} electionId - Election ID (e.g. 'TK2023')
   * @returns {Promise<Object>} Results with total votes and parties
   */
  async getProvincieResultaten(provincieNaam, electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/resultaten?electionId=${electionId}`)
    if (!response.ok) throw new Error(`Failed to get results for ${provincieNaam}`)
    return await response.json()
  },

  /**
   * Retrieves all constituencies that belong to a province.
   * @param {string} provincieNaam - Name of the province
   * @returns {Promise<Array>} List of constituencies
   */
  async getKieskringenInProvincie(provincieNaam) {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/kieskringen`)
    if (!response.ok) throw new Error(`Failed to get kieskringen for ${provincieNaam}`)
    return await response.json()
  }
}

export default ProvincieService
