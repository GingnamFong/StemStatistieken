// utils/time.js
export function formatTime(date) {
  const d = date instanceof Date ? date : new Date(date)
  const now = new Date()
  const diff = now - d
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)

  if (days > 0) return `${days} dag${days > 1 ? 'en' : ''} geleden`
  if (hours > 0) return `${hours} uur geleden`
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes > 0) return `${minutes} minuten geleden`
  return 'Zojuist'
}
