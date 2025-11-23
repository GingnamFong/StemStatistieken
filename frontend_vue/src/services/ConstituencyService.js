import { API_BASE_URL } from '../config/api.js'

export const ConstituencyService = {
  async getConstituencies(electionId) {
    const response = await fetch(`${API_BASE_URL}/elections/${electionId}/constituencies`)
    if (!response.ok) throw new Error(`Failed to get constituencies for ${electionId}`)
    return await response.json()
  }
}

export default ConstituencyService

