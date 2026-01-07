console.log("hello world");

// Base URL for the backend API
import { API_BASE_URL } from '../config/api.js'

/**
 * Submit a new forum post to the backend
 *
 * @param {string} title - The title of the forum post
 * @param {string} content - The main content/body of the post
 */
export async function submitForumPost(title, content) {
  // Validation, Prevent empty or whitespace-only post
  if (!title.trim() || !content.trim()) {
    throw new Error('Titel en tekst zijn verplicht.')
  }

  // Authentication, retrieve JWT token from localStorage
  const token = localStorage.getItem('token')

  // User must be logged in
  if (!token) {
    throw new Error('Je moet ingelogd zijn om een post te plaatsen.')
  }

  // Spacing because of single body field
  const bodyText = title.trim() + '\n\n' + content.trim()

  // API request
  const res = await fetch(`${API_BASE_URL}/api/forum/questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ body: bodyText })
  })

  // Error handling
  if (!res.ok) {
    if (res.status === 401) {
      throw new Error('Je moet ingelogd zijn om een post te plaatsen.')
    }

    let errorMessage = 'Fout bij opslaan in de server.'

    try {
      // try to read error body as text
      const errorText = await res.text()
      if (errorText) {
        try {
          const errorJson = JSON.parse(errorText)
          // Prefer backend error message if available
          errorMessage =
            errorJson.message ||
            errorJson.error ||
            errorText ||
            errorMessage
        } catch {
          // Plain text return
          errorMessage = errorText || errorMessage
        }
      }
    } catch (e) {
      console.error('Error reading response:', e)
    }

    throw new Error(errorMessage)
  }

  // Success, backend returns or doesn't return data
  return await res.json().catch(() => null)
}

/**
 * Handle voting logic on the frontend
 *
 * This function updates:
 * - post. score
 * - post.userVote
 *
 * No API calls here, UI-only logic
 */
export function votePost(posts, postId, voteType) {
  // Find the post the user voted on
  const post = posts.value.find(p => p.id === postId)
  if (!post) return

  // User clicked the same vote again
  if (post.userVote === voteType) {
    if (voteType === 'up') post.score--
    if (voteType === 'down') post.score++

    post.userVote = null
  }
  // User changed vote or voted for the first time
  else {
    if (post.userVote === 'up') post.score--
    if (post.userVote === 'down') post.score++

    // apply new vote
    if (voteType === 'up') post.score++
    if (voteType === 'down') post.score--

    post.userVote = voteType
  }
}

/**
 * Fetch all forum posts from the backend
 */
export async function fetchForumPosts() {
  const token = localStorage.getItem('token')

  // Default headers
  const headers = {
    'Content-Type': 'application/json'
  }

  // adds token if user is logged in
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(`${API_BASE_URL}/api/forum/questions`, { headers })

  if (!res.ok) {
    const errorText = await res.text()
    throw new Error(
      `Kon forumberichten niet laden: ${res.status} ${errorText}`
    )
  }

  const data = await res.json()

  // Check: backend should return an array
  if (!Array.isArray(data)) {
    return []
  }

  // convert backend posts to frontend posts
  return data.map(mapPostFromApi)
}

/**
 * Convert backend post format to frontend format
 *
 * @param {Object} p - Raw backend post object
 */
function mapPostFromApi(p) {
  // Backend stores title + content in one field
  const bodyParts = p.body?.split('\n\n') || ['']

  const title = bodyParts[0] || 'Geen titel'
  const content = bodyParts.slice(1).join('\n\n').trim()

  // Convert timestamp string into data object
  const createdAt = p.createdAt
    ? new Date(p.createdAt)
    : new Date()

  // Author name handling
  // backend author can be: string, object with name, object with firstName/lastName
  let authorName = 'Anoniem'

  if (typeof p.author === 'string') {
    authorName = p.author
  } else if (p.author?.name) {
    authorName = p.author.name
    if (p.author.lastName) {
      authorName += ` ${p.author.lastName}`
    }
  } else if (p.author?.firstName) {
    authorName = p.author.firstName
    if (p.author.lastName) {
      authorName += ` ${p.author.lastName}`
    }
  }

  // Final frontend ready (to) post object
  return {
    id: p.id,
    title,
    content,
    author: authorName,
    score: 0, // Initial score until votes are loaded
    comments: p.comments?.length || 0,
    createdAt,
    userVote: null // will be set when user interacts
  }
}





