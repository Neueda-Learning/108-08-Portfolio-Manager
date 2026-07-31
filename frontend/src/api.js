const apiBaseUrl =
  import.meta.env.VITE_SUPABASE_FUNCTIONS_URL ||
  import.meta.env.VITE_API_BASE_URL ||
  '/api'

const normalizePath = (path) => (path.startsWith('/') ? path : `/${path}`)

export const apiFetch = (path, options = {}) => {
  return fetch(`${apiBaseUrl}${normalizePath(path)}`, options)
}

export const getApiBaseUrl = () => apiBaseUrl
