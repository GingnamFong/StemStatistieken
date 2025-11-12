const API_BASE_URL =
  (location.origin === 'https://hva-frontend.onrender.com')
    ? 'https://hva-backend-c647.onrender.com'
    : 'http://localhost:8081'

export const ElectionService = {
  async loadCandidateLists(electionId, folderName = null) {
    const params = folderName ? `?folderName=${encodeURIComponent(folderName)}` : ''
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/candidatelists${params}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) throw new Error('Failed to load candidate data')
    return await response.json()
  },

  async getElection(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}`)
    if (!response.ok) throw new Error(`Failed to get election ${electionId}`)
    return await response.json()
  },

  async getMunicipalities(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/municipalities`)
    if (!response.ok) throw new Error(`Failed to get municipalities for ${electionId}`)
    return await response.json()
  },

  async getConstituencies(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/constituencies`)
    if (!response.ok) throw new Error(`Failed to get constituencies for ${electionId}`)
    return await response.json()
  },

  async getMunicipality(electionId, municipalityId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/municipalities/${encodeURIComponent(municipalityId)}`)
    if (!response.ok) throw new Error(`Failed to get municipality ${municipalityId}`)
    return await response.json()
  }
}

export default ElectionService

