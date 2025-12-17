// services/ForumService.js
import { API_BASE_URL } from '../config/api.js'
import { authHeaders } from './http.js'

async function parseError(res) {
  const text = await res.text()
  try {
    const json = JSON.parse(text)
    return json.message || json.error || text || `HTTP ${res.status}`
  } catch {
    return text || `HTTP ${res.status}`
  }
}

async function request(path, options) {
  const res = await fetch(`${API_BASE_URL}${path}`, options)
  if (!res.ok) throw new Error(await parseError(res))

  // handle empty response bodies (204 etc.)
  const contentType = res.headers.get('content-type') || ''
  if (contentType.includes('application/json')) return await res.json()
  return await res.text()
}

export const ForumService = {
  // ------------------------
  // Posts
  // ------------------------
  getPosts() {
    return request('/api/forum/questions')
  },

  getPost(postId) {
    return request(`/api/forum/questions/${postId}`)
  },

  createPost(body) {
    return request('/api/forum/questions', {
      method: 'POST',
      headers: authHeaders({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ body })
    })
  },

  async deletePost(postId) {
    const res = await fetch(`${API_BASE_URL}/api/forum/questions/${postId}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
    if (res.status === 204) return true
    if (!res.ok) throw new Error(await parseError(res))
    return true
  },

  // ------------------------
  // Comments (ForumQuestions with parent)
  // ------------------------
  getComments(postId) {
    return request(`/api/forum/${postId}/questions`)
  },

  addComment(postId, body) {
    return request(`/api/forum/${postId}/questions`, {
      method: 'POST',
      headers: authHeaders({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ body })
    })
  },

  // ------------------------
  // Comment Likes
  // Endpoints match CommentLikeController:
  // POST   /api/forum/comments/{commentId}/like
  // DELETE /api/forum/comments/{commentId}/like
  // GET    /api/forum/comments/{commentId}/likes
  // ------------------------
  likeComment(commentId) {
    return request(`/api/forum/comments/${commentId}/like`, {
      method: 'POST',
      headers: authHeaders()
    }) // -> { commentId, likes }
  },

  unlikeComment(commentId) {
    return request(`/api/forum/comments/${commentId}/like`, {
      method: 'DELETE',
      headers: authHeaders()
    }) // -> { commentId, likes }
  },

  getCommentLikes(commentId) {
    return request(`/api/forum/comments/${commentId}/likes`)
  }
}
