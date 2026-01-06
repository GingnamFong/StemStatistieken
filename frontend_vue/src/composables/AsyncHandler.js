// src/composables/AsyncHandler.js

/**
 * Runs an async function and automatically handles:
 * - loading state
 * - error message
 */
export async function runAsyncWithState(asyncFn, state) {
  // Get loading and error refs from the state object
  const { loading, error } = state

  // Start loading and clear any old error
  loading.value = true
  error.value = ''

  // Run async function and returns result
  try {
    return await asyncFn()

  } catch (e) {
    // Sends message if something goes wrong
    error.value = e.message || 'Er is een fout opgetreden'

    // Rethrow the error so it can still be handled elsewhere
    throw e

  } finally {
    // stops loading, no matter what happens
    loading.value = false
  }
}


