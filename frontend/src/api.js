const apiBaseUrl =
  import.meta.env.VITE_SUPABASE_FUNCTIONS_URL ||
  import.meta.env.VITE_API_BASE_URL ||
  '/api'

const normalizePath = (path) => (path.startsWith('/') ? path : `/${path}`)

export const apiFetch = (path, options = {}, authContext = {}) => {
  const headers = new Headers(options.headers || {})
  const { token, role, customerId, fundManagerId } = authContext

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  if (role) {
    headers.set('X-User-Role', role)
  }
  if (customerId) {
    headers.set('X-Customer-Id', String(customerId))
  }
  if (fundManagerId) {
    headers.set('X-Fund-Manager-Id', String(fundManagerId))
  }

  return fetch(`${apiBaseUrl}${normalizePath(path)}`, {
    ...options,
    headers,
  })
}

export const getApiBaseUrl = () => apiBaseUrl
