import { API_BASE_URL } from '../config/api.js'

export const UserService = {
  /**
   * Login user
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<Object>} Login response with token and user data
   */
  async login(email, password) {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    })

    if (!response.ok) {
      let errorText = 'Ongeldige e-mail of wachtwoord.'
      try {
        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
          const errorData = await response.json()
          errorText = errorData.message || errorData.error || errorText
        }
        // Plain Text Error
        else {
          const text = await response.text()
          if (text && text.trim()) {
            errorText = text.trim()
          }
        }
      } catch {
        // If parsing fails, use default error message
      }
      throw new Error(errorText)
    }

    return await response.json()
  },

  /**
   * Register new user
   * @param {Object} userData - User registration data
   * @param {string} userData.name - First name
   * @param {string} userData.lastName - Last name
   * @param {string} userData.email - Email address
   * @param {string} userData.password - Password
   * @returns {Promise<void>}
   */
  async register(userData) {
    const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: userData.name,
        lastName: userData.lastName,
        email: userData.email,
        password: userData.password
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || 'Registratie mislukt')
    }
  },

  /**
   * Get user profile by ID
   * @param {string} userId - The user ID
   * @returns {Promise<Object>} User data
   */
  async getUser(userId) {
    const response = await fetch(`${API_BASE_URL}/api/auth/user/${userId}`)

    if (response.status === 404) {
      const errorText = await response.text()
      throw new Error(`User not found: ${errorText}`)
    }

    if (response.status === 401) {
      throw new Error('Unauthorized - Please login again')
    }

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`Failed to get user: ${errorText}`)
    }

    return await response.json()
  },

  /**
   * Update user profile
   * @param {string} userId - The user ID
   * @param {string} token - Authentication token
   * @param {Object} userData - User data to update
   * @param {string} [userData.firstName] - First name
   * @param {string} [userData.lastName] - Last name
   * @param {string} [userData.birthDate] - Birth date
   * @param {string} [userData.favoriteParty] - Favorite party
   * @param {string} [userData.profilePicture] - Profile picture (base64)
   * @returns {Promise<Object>} Updated user data
   */
  async updateUser(userId, token, userData) {
    const response = await fetch(`${API_BASE_URL}/api/auth/user/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        firstName: userData.firstName || null,
        lastName: userData.lastName || null,
        birthDate: userData.birthDate || null,
        favoriteParty: userData.favoriteParty || null,
        profilePicture: userData.profilePicture || null
      })
    })

    if (!response.ok) {
      let errorMessage = 'Kon profiel niet bijwerken.'
      try {
        const contentType = response.headers.get('content-type')
        if (contentType && contentType.includes('application/json')) {
          const errorData = await response.json()
          errorMessage = errorData.message || errorData.error || errorMessage
        } else {
          const errorText = await response.text()
          if (errorText && errorText.trim()) {
            errorMessage = errorText.trim()
          }
        }
      } catch {
        // If parsing fails, use default error message
      }
      throw new Error(errorMessage)
    }

    return await response.json()
  }
}

export default UserService

