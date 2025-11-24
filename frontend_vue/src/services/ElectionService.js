import { API_BASE_URL } from '../config/api.js'

export const ElectionService = {
  async getElection(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}`)
    if (!response.ok) throw new Error(`Failed to get election ${electionId}`)
    return await response.json()
  }
}

export default ElectionService

