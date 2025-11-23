const API_BASE_URL =
  (location.origin === 'https://hva-frontend.onrender.com')
    ? 'https://hva-backend-c647.onrender.com'
    : 'http://localhost:8081'

export const ProvincieService = {
  async getAllProvincies(electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies?electionId=${electionId}`)
    if (!response.ok) throw new Error('Failed to get provincies')
    return await response.json()
  },

  async getProvincieResultaten(provincieNaam, electionId = 'TK2023') {
    const response = await fetch(`${API_BASE_URL}/provincies/${encodeURIComponent(provincieNaam)}/resultaten?electionId=${electionId}`)
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
