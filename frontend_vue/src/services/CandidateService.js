import { API_BASE_URL } from '../config/api.js'

export const CandidateService = {
  async loadCandidateLists(electionId, folderName = null) {
    const params = folderName ? `?folderName=${encodeURIComponent(folderName)}` : ''
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/candidates/lists${params}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (!response.ok) throw new Error('Failed to load candidate data')
    return await response.json()
  },

  async getCandidate(electionId, candidateId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/candidates/${candidateId}`)
    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('Kandidaat niet gevonden')
      }
      throw new Error(`Failed to get candidate ${candidateId}`)
    }
    return await response.json()
  }
}

export default CandidateService

