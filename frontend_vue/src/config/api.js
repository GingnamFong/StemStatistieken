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

  // IMPORTANT: If we're NOT on localhost, detect deployment environment
  if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
    // Azure deployment - use Azure backend URL
    if (hostname.includes('azurewebsites.net')) {
      return 'https://hva-backend-dghpcwaga7h7bug7.germanywestcentral-01.azurewebsites.net'
    }

    // Render deployment
    if (origin === 'https://hva-frontend.onrender.com') {
      return 'https://hva-backend-c647.onrender.com'
    }

    // Detect stemstatistieken.me domain (with or without www)
    // Use HTTPS if page is loaded over HTTPS, otherwise HTTP
    if (hostname === 'stemstatistieken.me' ||
        hostname === 'www.stemstatistieken.me' ||
        hostname.includes('stemstatistieken.me')) {
      // Use same protocol as the page (HTTPS if page is HTTPS)
      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
      return `${protocol}//${hostname}`
    }

    // Detect AWS server (13.48.214.231)
    if (hostname === '13.48.214.231' || hostname.includes('13.48.214.231')) {
      // Use same protocol as the page
      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
      return `${protocol}//13.48.214.231:8081`
    }

    // If we're on any other domain (not localhost), use same protocol
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    return `${protocol}//13.48.214.231:8081`
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

