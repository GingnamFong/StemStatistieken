import { API_BASE_URL } from '../config/api.js'
import { authHeaders } from './http.js'

export const ForumService = {
  async addComment(postId, body) {
    const res = await fetch(`${API_BASE_URL}/api/forum/${postId}/comments`, {
      method: 'POST',
      headers: authHeaders({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ body })
    })
    if (!res.ok) throw new Error(await res.text())
    return await res.json()
  },

  async deleteComment(commentId) {
    const res = await fetch(`${API_BASE_URL}/api/forum/comments/${commentId}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    if (res.status === 204) return true
    throw new Error(await res.text())
  },

  async likeComment(commentId) {
    const res = await fetch(`${API_BASE_URL}/api/forum/comments/${commentId}/like`, {
      method: 'POST',
      headers: authHeaders()
    })
    if (!res.ok) throw new Error(await res.text())
    return await res.json() // {commentId, likes}
  },

  async unlikeComment(commentId) {
    const res = await fetch(`${API_BASE_URL}/api/forum/comments/${commentId}/like`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    if (!res.ok) throw new Error(await res.text())
    return await res.json()
  },

  async getLikeCount(commentId) {
    const res = await fetch(`${API_BASE_URL}/api/forum/comments/${commentId}/likes`)
    if (!res.ok) throw new Error(await res.text())
    return await res.json()
  }
}
