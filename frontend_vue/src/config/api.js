// Central API configuration
// Uses environment variables if available, otherwise falls back to origin-based detection

const getApiBaseUrl = () => {
  // Use environment variable if set (Vite exposes env vars with VITE_ prefix)
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }

  // Fallback: detect based on current origin
  if (location.origin === 'https://hva-frontend.onrender.com') {
    return 'https://hva-backend-c647.onrender.com'
  }

  // Default to local development
  return import.meta.env.VITE_API_URL || 'http://localhost:8081'
}

export const API_BASE_URL = getApiBaseUrl()

