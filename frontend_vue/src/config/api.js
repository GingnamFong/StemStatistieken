// Central API configuration
// Uses environment variables if available, otherwise falls back to origin-based detection

const getApiBaseUrl = () => {
  // Use environment variable if set (Vite exposes env vars with VITE_ prefix)
  // This is set during Docker build via build args
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }

  // Runtime detection based on current hostname
  const hostname = window.location.hostname
  const origin = window.location.origin

  // Detect AWS server (13.48.214.231)
  if (hostname === '13.48.214.231' || hostname.includes('13.48.214.231')) {
    return 'http://13.48.214.231:8081'
  }

  // Detect stemstatistieken.me domain
  if (hostname === 'stemstatistieken.me' || hostname.includes('stemstatistieken.me')) {
    return 'http://13.48.214.231:8081'
  }

  // Fallback: detect based on current origin
  if (origin === 'https://hva-frontend.onrender.com') {
    return 'https://hva-backend-c647.onrender.com'
  }

  // Default to local development
  return import.meta.env.VITE_API_URL || 'http://localhost:8081'
}

export const API_BASE_URL = getApiBaseUrl()

// Debug log (only in development)
if (import.meta.env.DEV) {
  console.log('API_BASE_URL:', API_BASE_URL)
  console.log('Current hostname:', window.location.hostname)
  console.log('VITE_API_BASE_URL env:', import.meta.env.VITE_API_BASE_URL)
}

