const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const ProvincieService = {
  async getAllProvincies() {
    const response = await fetch(`${API_BASE_URL}/provincies`)
    if (!response.ok) throw new Error('Failed to get provincies')
    return await response.json()
  },

  async getProvincieData(provincieNaam) {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}`)
    if (!response.ok) throw new Error(`Failed to get data for ${provincieNaam}`)
    return await response.json()
  },

  async getProvincieResultaten(provincieNaam) {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/resultaten`)
    if (!response.ok) throw new Error(`Failed to get results for ${provincieNaam}`)
    return await response.json()
  },

  async getKieskringenInProvincie(provincieNaam) {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/kieskringen`)
    if (!response.ok) throw new Error(`Failed to get kieskringen for ${provincieNaam}`)
    return await response.json()
  }
}

export default ProvincieService
