console.log("hello world");

import { API_BASE_URL } from '../config/api.js'
import { computed } from 'vue'


export async function submitForumPost(title, content) {
  // Validation (same logic, but service-level)
  if (!title.trim() || !content.trim()) {
    throw new Error('Titel en tekst zijn verplicht.')
  }

  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Je moet ingelogd zijn om een post te plaatsen.')
  }

  const bodyText = title.trim() + '\n\n' + content.trim()

  const res = await fetch(`${API_BASE_URL}/api/forum/questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ body: bodyText })
  })

  if (!res.ok) {
    if (res.status === 401) {
      throw new Error('Je moet ingelogd zijn om een post te plaatsen.')
    }

    let errorMessage = 'Fout bij opslaan in de server.'

    try {
      const errorText = await res.text()
      if (errorText) {
        try {
          const errorJson = JSON.parse(errorText)
          errorMessage =
            errorJson.message ||
            errorJson.error ||
            errorText ||
            errorMessage
        } catch {
          errorMessage = errorText || errorMessage
        }
      }
    } catch (e) {
      console.error('Error reading response:', e)
    }

    throw new Error(errorMessage)
  }

  // If needed later, you can return data
  return await res.json().catch(() => null)
}

// VotePost placed in service
export function votePost(posts, postId, voteType) {
  const post = posts.value.find(p => p.id === postId)
  if (!post) return

  if (post.userVote === voteType) {
    if (voteType === 'up') post.score--
    if (voteType === 'down') post.score++
    post.userVote = null
  } else {
    if (post.userVote === 'up') post.score--
    if (post.userVote === 'down') post.score++

    if (voteType === 'up') post.score++
    if (voteType === 'down') post.score--

    post.userVote = voteType
  }
}




