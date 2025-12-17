import { API_BASE_URL } from '../config/api.js'

export const AuthService = {
  async login(email, password) {
    const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })
    if (!res.ok) throw new Error(await res.text())

    const data = await res.json()
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    return data
  },

  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
  }
}
