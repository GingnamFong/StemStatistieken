console.log("hello world");

import { API_BASE_URL } from '../config/api.js'

function getAuthHeaders() {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

export async function fetchForumPosts() {
  const res = await fetch(`${API_BASE_URL}/api/forum/questions`, {
    headers: getAuthHeaders()
  })

  if (!res.ok) {
    throw new Error(await res.text())
  }

  return res.json()
}

export async function createForumPost(body) {
  const token = localStorage.getItem('token')
  if (!token) throw new Error('Niet ingelogd')

  const res = await fetch(`${API_BASE_URL}/api/forum/questions`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ body })
  })

  if (!res.ok) {
    throw new Error(await res.text())
  }

  return res.json()
}


