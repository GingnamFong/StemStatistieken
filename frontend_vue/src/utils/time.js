// utils/time.js

/**
 * Converts a date into a more friendly "time ago" text
 * Example: "3 dagen geleden", "2 uur geleden", "Zojuist" etc
 */
export function formatTime(date) {
  // Make sure we work with a data object
  const d = date instanceof Date ? date : new Date(date)

  // Current time
  const now = new Date()

  // Difference between now and given date in milliseconds
  const diff = now - d

  // Convert time difference to hours
  const hours = Math.floor(diff / (1000 * 60 * 60))

  // Convert hours to days
  const days = Math.floor(hours / 24)

  // if it has been at least 1 day
  if (days > 0)
    return `${days} dag${days > 1 ? 'en' : ''} geleden`

  // at least 1 hour
  if (hours > 0)
    return `${hours} uur geleden`

  // convert time differences to minutes
  const minutes = Math.floor(diff / (1000 * 60))

  // at least 1 minute
  if (minutes > 0)
    return `${minutes} minuten geleden`

  // less than a  minute
  return 'Zojuist'
}
