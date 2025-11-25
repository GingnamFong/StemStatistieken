import { API_BASE_URL } from '../config/api.js'

/**
 * Service voor provincie data ophalen van de backend API.
 * Haalt provincies, resultaten en kieskringen op via REST endpoints.
 */
export const ProvincieService = {
  /**
   * Haalt alle provincies op voor een verkiezing.
   * @param {string} electionId - Verkiezing ID (bijv. 'TK2023')
   * @returns {Promise<Array>} Lijst van provincies
   */
  async getAllProvincies(electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies?electionId=${electionId}`)
    if (!response.ok) throw new Error('Failed to get provincies')
    return await response.json()
  },

  /**
   * Haalt verkiezingsresultaten op voor een specifieke provincie.
   * @param {string} provincieNaam - Naam van de provincie
   * @param {string} electionId - Verkiezing ID (bijv. 'TK2023')
   * @returns {Promise<Object>} Resultaten met totaalStemmen en partijen
   */
  async getProvincieResultaten(provincieNaam, electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/resultaten?electionId=${electionId}`)
    if (!response.ok) throw new Error(`Failed to get results for ${provincieNaam}`)
    return await response.json()
  },

  /**
   * Haalt alle kieskringen op die bij een provincie horen.
   * @param {string} provincieNaam - Naam van de provincie
   * @returns {Promise<Array>} Lijst van kieskringen
   */
  async getKieskringenInProvincie(provincieNaam) {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/kieskringen`)
    if (!response.ok) throw new Error(`Failed to get kieskringen for ${provincieNaam}`)
    return await response.json()
  }
}

export default ProvincieService
