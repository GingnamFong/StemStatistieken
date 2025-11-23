import { API_BASE_URL } from '../config/api.js'

export const ElectionService = {
  async loadMunicipalities(electionId, folderName = null) {
    const params = folderName ? `?folderName=${encodeURIComponent(folderName)}` : ''
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/municipalities${params}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) throw new Error('Failed to load municipality data')
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

