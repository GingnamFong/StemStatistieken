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

  // IMPORTANT: If we're NOT on localhost, use AWS server
  // This prevents the browser's private network access block
  if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
    // Detect AWS server (13.48.214.231)
    if (hostname === '13.48.214.231' || hostname.includes('13.48.214.231')) {
      return 'http://13.48.214.231:8081'
    }

    // Detect stemstatistieken.me domain (with or without www)
    if (hostname === 'stemstatistieken.me' || 
        hostname === 'www.stemstatistieken.me' || 
        hostname.includes('stemstatistieken.me')) {
      return 'http://13.48.214.231:8081'
    }

    // Fallback: detect based on current origin
    if (origin === 'https://hva-frontend.onrender.com') {
      return 'https://hva-backend-c647.onrender.com'
    }

    // If we're on any other domain (not localhost), use AWS server
    return 'http://13.48.214.231:8081'
  }

  // Only use localhost if we're actually on localhost
  return import.meta.env.VITE_API_URL || 'http://localhost:8081'
}

export const API_BASE_URL = getApiBaseUrl()

// Debug log (always log in production to help diagnose issues)
console.log('API Configuration:', {
  API_BASE_URL,
  hostname: window.location.hostname,
  origin: window.location.origin,
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  VITE_API_URL: import.meta.env.VITE_API_URL
})

