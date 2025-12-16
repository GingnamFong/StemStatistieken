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

  // Detect AWS server (13.48.214.231)
  if (location.hostname === '13.48.214.231' || location.hostname.includes('13.48.214.231')) {
    return 'http://13.48.214.231:8081'
  }

  // Detect stemstatistieken.me domain
  if (location.hostname === 'stemstatistieken.me' || location.hostname.includes('stemstatistieken.me')) {
    return 'http://13.48.214.231:8081'
  }

  // Default to local development
  return ''
}

export const API_BASE_URL = getApiBaseUrl()

