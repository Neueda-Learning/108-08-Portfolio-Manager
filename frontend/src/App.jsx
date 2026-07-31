import { useEffect, useState } from 'react'
import './App.css'
import { apiFetch, getApiBaseUrl } from './api'

const views = ['Dashboard', 'Customers', 'Portfolio', 'Performance']

function App() {
  const [activeView, setActiveView] = useState('Dashboard')
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

  const renderDashboard = () => (
    <>
      <section className="card">
        <h2>Fund Manager Overview</h2>
        <p>Backend status: {status}</p>
        <p>API base: {apiBaseUrl}</p>
      </section>

      <section className="summary-grid">
        <article className="summary-card">
          <h3>Total Customers</h3>
          <p>{accounts.length}</p>
        </article>
        <article className="summary-card">
          <h3>Total Portfolio Value</h3>
          <p>Pending portfolio endpoint</p>
        </article>
      </section>

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
    </>
  )

  const renderPlaceholder = (title, description) => (
    <section className="card">
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  )

  const renderContent = () => {
    if (activeView === 'Dashboard') {
      return renderDashboard()
    }

    if (activeView === 'Customers') {
      return renderPlaceholder(
        'Customers',
        'Customer list and risk profiles will be added in the next small commit.'
      )
    }

    if (activeView === 'Portfolio') {
      return renderPlaceholder(
        'Portfolio',
        'Customer holdings and add/remove asset flows will be added after customers page.'
      )
    }

    return renderPlaceholder(
      'Performance',
      'Portfolio vs Sensex performance view will be added once portfolio data is available.'
    )
  }

  return (
    <main className="app">
      <header className="page-header">
        <h1>Financial Portfolio Manager</h1>
        <p className="status">Fund Manager Workspace</p>
      </header>

      <nav className="nav" aria-label="Primary">
        {views.map((view) => (
          <button
            key={view}
            type="button"
            className={`nav-link ${activeView === view ? 'active' : ''}`}
            onClick={() => setActiveView(view)}
          >
            {view}
          </button>
        ))}
      </nav>

      {renderContent()}
    </main>
  )
}

export default App
