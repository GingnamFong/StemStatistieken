// src/composables/AsyncHandler.js
export async function runAsyncWithState(asyncFn, state) {
  const { loading, error } = state
  loading.value = true
  error.value = ''
  try {
    return await asyncFn()
  } catch (e) {
    error.value = e.message || 'Er is een fout opgetreden'
    throw e
  } finally {
    loading.value = false
  }
}


