import { useEffect, useState } from 'react'
import './App.css'
import { apiFetch, getApiBaseUrl } from './api'

function App() {
  const [status, setStatus] = useState('Checking connection...')
  const [accounts, setAccounts] = useState([])
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const apiBaseUrl = getApiBaseUrl()

  const loadAccounts = async () => {
    const response = await apiFetch('/accounts')
    if (!response.ok) {
      throw new Error('Could not load accounts')
    }

    const data = await response.json()
    setAccounts(data)
  }

  useEffect(() => {
    const initialize = async () => {
      try {
        const pingResponse = await apiFetch('/ping')
        if (!pingResponse.ok) {
          throw new Error('Core API not reachable')
        }

        setStatus('Connected to API')
        await loadAccounts()
      } catch {
        setStatus('Connection failed')
        setError('Start the core app or configure Supabase function URL.')
      }
    }

    initialize()
  }, [])

  const handleCreate = async (event) => {
    event.preventDefault()
    setError('')

    try {
      const response = await apiFetch('/accounts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, email }),
      })

      if (!response.ok) {
        throw new Error('Could not create account')
      }

      setName('')
      setEmail('')
      await loadAccounts()
    } catch {
      setError('Failed to save account. Check backend or Supabase logs for details.')
    }
  }

  return (
    <main className="app">
      <h1>Portfolio Manager Connectivity Demo</h1>
      <p className="status">Backend status: {status}</p>
      <p className="status">API base: {apiBaseUrl}</p>

      <section className="card">
        <h2>Create account (sample POST)</h2>
        <form onSubmit={handleCreate} className="form">
          <input
            type="text"
            placeholder="Name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          <button type="submit">Save</button>
        </form>
        {error && <p className="error">{error}</p>}
      </section>

      <section className="card">
        <h2>Accounts (sample GET)</h2>
        {accounts.length === 0 ? (
          <p>No accounts yet.</p>
        ) : (
          <ul>
            {accounts.map((account) => (
              <li key={account.id}>
                #{account.id} - {account.name} ({account.email})
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  )
}

export default App
