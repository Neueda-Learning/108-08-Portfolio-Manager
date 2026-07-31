import { useEffect, useMemo, useState } from 'react'
import './App.css'
import { apiFetch, getApiBaseUrl } from './api'
import { hasSupabaseConfig, supabase } from './supabase'

const ROLE_OPTIONS = [
  { label: 'Admin', value: 'ADMIN' },
  { label: 'Fund Manager', value: 'FUND_MANAGER' },
  { label: 'Customer', value: 'CUSTOMER' },
]

function App() {
  const [session, setSession] = useState(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [authMode, setAuthMode] = useState('signin')
  const [selectedRole, setSelectedRole] = useState('ADMIN')
  const [manualCustomerId, setManualCustomerId] = useState('')
  const [manualFundManagerId, setManualFundManagerId] = useState('')
  const [signupCustomerId, setSignupCustomerId] = useState('')
  const [signupFundManagerId, setSignupFundManagerId] = useState('')
  const [authError, setAuthError] = useState('')
  const [authMessage, setAuthMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState('Not connected')
  const [customers, setCustomers] = useState([])
  const [assets, setAssets] = useState([])
  const [portfolios, setPortfolios] = useState([])
  const [selectedCustomerId, setSelectedCustomerId] = useState('')
  const [newCustomer, setNewCustomer] = useState({
    fundManagerId: '',
    name: '',
    email: '',
    phone: '',
  })
  const [newPortfolio, setNewPortfolio] = useState({
    customerId: '',
    portfolioName: '',
    totalInvestment: '',
    currentValue: '',
  })
  const [appError, setAppError] = useState('')
  const apiBaseUrl = getApiBaseUrl()

  useEffect(() => {
    if (!supabase) {
      return undefined
    }
    supabase.auth.getSession().then(({ data }) => setSession(data.session))
    const { data } = supabase.auth.onAuthStateChange((_event, nextSession) => {
      setSession(nextSession)
    })
    return () => data.subscription.unsubscribe()
  }, [])

  const authContext = useMemo(() => {
    const userMetadata = session?.user?.user_metadata || {}
    const customerId = userMetadata.customer_id || (manualCustomerId ? Number(manualCustomerId) : null)
    const fundManagerId = userMetadata.fund_manager_id || (manualFundManagerId ? Number(manualFundManagerId) : null)
    return {
      token: session?.access_token || '',
      role: selectedRole,
      customerId,
      fundManagerId,
    }
  }, [session, selectedRole, manualCustomerId, manualFundManagerId])

  const loadForRole = async () => {
    if (!session) {
      return
    }
    setAppError('')
    setStatus('Loading data...')

    try {
      const assetResponse = await apiFetch('/assets', {}, authContext)
      if (!assetResponse.ok) {
        throw new Error('Failed to load stock universe')
      }
      setAssets(await assetResponse.json())

      if (authContext.role === 'CUSTOMER') {
        if (!authContext.customerId) {
          setStatus('Logged in. Add Customer ID to view portfolio.')
          setPortfolios([])
          return
        }
        const response = await apiFetch(`/customers/${authContext.customerId}/portfolios`, {}, authContext)
        if (!response.ok) {
          throw new Error('Failed to load customer portfolio')
        }
        setPortfolios(await response.json())
        setCustomers([])
        setStatus('Customer portfolio loaded')
        return
      }

      const customerPath = authContext.role === 'FUND_MANAGER' && authContext.fundManagerId
        ? `/fund-managers/${authContext.fundManagerId}/customers`
        : '/customers'
      const customersResponse = await apiFetch(customerPath, {}, authContext)
      if (!customersResponse.ok) {
        throw new Error('Failed to load customers')
      }
      const customerData = await customersResponse.json()
      setCustomers(customerData)
      setStatus('Management data loaded')
      if (customerData.length > 0) {
        const initialCustomerId = customerData[0].id
        setSelectedCustomerId(String(initialCustomerId))
        setNewPortfolio((prev) => ({ ...prev, customerId: String(initialCustomerId) }))
      } else {
        setSelectedCustomerId('')
      }
      setPortfolios([])
    } catch (error) {
      setStatus('Load failed')
      setAppError(error.message || 'Unable to load data')
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadForRole()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session, authContext.role, authContext.customerId, authContext.fundManagerId])

  useEffect(() => {
    const loadSelectedCustomerPortfolios = async () => {
      if (!session || authContext.role === 'CUSTOMER' || !selectedCustomerId) {
        return
      }
      try {
        const response = await apiFetch(`/customers/${selectedCustomerId}/portfolios`, {}, authContext)
        if (!response.ok) {
          throw new Error('Failed to load portfolios for customer')
        }
        setPortfolios(await response.json())
      } catch (error) {
        setAppError(error.message || 'Unable to load portfolios')
      }
    }
    loadSelectedCustomerPortfolios()
  }, [selectedCustomerId, session, authContext])

  const signIn = async (event) => {
    event.preventDefault()
    setAuthError('')
    setAuthMessage('')
    if (!supabase) {
      setAuthError('Supabase configuration is missing in frontend env variables.')
      return
    }
    setLoading(true)
    const { error } = await supabase.auth.signInWithPassword({ email, password })
    setLoading(false)
    if (error) {
      setAuthError(error.message)
    }
  }

  const signUp = async (event) => {
    event.preventDefault()
    setAuthError('')
    setAuthMessage('')
    if (!supabase) {
      setAuthError('Supabase configuration is missing in frontend env variables.')
      return
    }
    if (password.length < 6) {
      setAuthError('Password must be at least 6 characters.')
      return
    }
    if (password !== confirmPassword) {
      setAuthError('Password and confirm password do not match.')
      return
    }

    const metadata = {
      role: selectedRole,
    }
    if (selectedRole === 'CUSTOMER' && signupCustomerId) {
      metadata.customer_id = Number(signupCustomerId)
    }
    if (selectedRole === 'FUND_MANAGER' && signupFundManagerId) {
      metadata.fund_manager_id = Number(signupFundManagerId)
    }

    setLoading(true)
    const { data, error } = await supabase.auth.signUp({
      email,
      password,
      options: { data: metadata },
    })
    setLoading(false)
    if (error) {
      setAuthError(error.message)
      return
    }

    if (!data.session) {
      setAuthMessage('Signup successful. Please verify your email, then sign in.')
      setAuthMode('signin')
    } else {
      setAuthMessage('Signup successful.')
    }
  }

  const signOut = async () => {
    if (supabase) {
      await supabase.auth.signOut()
    }
    setCustomers([])
    setPortfolios([])
    setAssets([])
    setStatus('Logged out')
  }

  const createCustomer = async (event) => {
    event.preventDefault()
    setAppError('')
    const fundManagerIdValue = authContext.role === 'FUND_MANAGER'
      ? authContext.fundManagerId
      : Number(newCustomer.fundManagerId)
    const response = await apiFetch(
      '/customers',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fundManagerId: fundManagerIdValue,
          name: newCustomer.name,
          email: newCustomer.email,
          phone: newCustomer.phone,
        }),
      },
      authContext,
    )
    if (!response.ok) {
      setAppError('Failed to create customer')
      return
    }
    setNewCustomer({ fundManagerId: '', name: '', email: '', phone: '' })
    await loadForRole()
  }

  const createPortfolio = async (event) => {
    event.preventDefault()
    setAppError('')
    const response = await apiFetch(
      '/portfolios',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          customerId: Number(newPortfolio.customerId),
          portfolioName: newPortfolio.portfolioName,
          totalInvestment: Number(newPortfolio.totalInvestment),
          currentValue: Number(newPortfolio.currentValue),
        }),
      },
      authContext,
    )
    if (!response.ok) {
      setAppError('Failed to create portfolio')
      return
    }
    setNewPortfolio((prev) => ({
      ...prev,
      portfolioName: '',
      totalInvestment: '',
      currentValue: '',
    }))
    if (newPortfolio.customerId) {
      setSelectedCustomerId(newPortfolio.customerId)
    }
  }

  const isManagerRole = authContext.role === 'ADMIN' || authContext.role === 'FUND_MANAGER'

  if (!hasSupabaseConfig) {
    return (
      <main className="app">
        <h1>Portfolio Manager</h1>
        <p className="error">
          Missing Supabase Auth configuration. Set VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY.
        </p>
      </main>
    )
  }

  if (!session) {
    return (
      <main className="app">
        <section className="card">
          <h1>{authMode === 'signin' ? 'Portfolio Manager Login' : 'Portfolio Manager Signup'}</h1>
          <p className="meta">API base: {apiBaseUrl}</p>
          <div className="auth-switch">
            <button
              type="button"
              className={authMode === 'signin' ? 'active' : ''}
              onClick={() => {
                setAuthMode('signin')
                setAuthError('')
                setAuthMessage('')
              }}
            >
              Sign in
            </button>
            <button
              type="button"
              className={authMode === 'signup' ? 'active' : ''}
              onClick={() => {
                setAuthMode('signup')
                setAuthError('')
                setAuthMessage('')
              }}
            >
              Sign up
            </button>
          </div>
          <form className="form" onSubmit={authMode === 'signin' ? signIn : signUp}>
            <select value={selectedRole} onChange={(event) => setSelectedRole(event.target.value)}>
              {ROLE_OPTIONS.map((role) => (
                <option key={role.value} value={role.value}>{role.label}</option>
              ))}
            </select>
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
            {authMode === 'signup' && (
              <>
                <input
                  type="password"
                  placeholder="Confirm password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  required
                />
                {selectedRole === 'CUSTOMER' && (
                  <input
                    type="number"
                    placeholder="Customer ID (optional)"
                    value={signupCustomerId}
                    onChange={(event) => setSignupCustomerId(event.target.value)}
                  />
                )}
                {selectedRole === 'FUND_MANAGER' && (
                  <input
                    type="number"
                    placeholder="Fund Manager ID (optional)"
                    value={signupFundManagerId}
                    onChange={(event) => setSignupFundManagerId(event.target.value)}
                  />
                )}
              </>
            )}
            <button type="submit" disabled={loading}>
              {loading ? (authMode === 'signin' ? 'Signing in...' : 'Signing up...') : (authMode === 'signin' ? 'Sign in' : 'Sign up')}
            </button>
          </form>
          {authMessage && <p className="meta">{authMessage}</p>}
          {authError && <p className="error">{authError}</p>}
        </section>
      </main>
    )
  }

  return (
    <main className="app">
      <header className="header">
        <div>
          <h1>Portfolio Manager ({authContext.role})</h1>
          <p className="meta">Signed in as {session.user.email}</p>
          <p className="meta">Status: {status}</p>
        </div>
        <button type="button" className="secondary" onClick={signOut}>Sign out</button>
      </header>

      {authContext.role === 'CUSTOMER' && !authContext.customerId && (
        <section className="card">
          <h2>Link Customer Profile</h2>
          <p className="meta">Enter your customer ID if it is not present in Supabase user metadata.</p>
          <input
            type="number"
            value={manualCustomerId}
            onChange={(event) => setManualCustomerId(event.target.value)}
            placeholder="Customer ID"
          />
        </section>
      )}

      {authContext.role === 'FUND_MANAGER' && !authContext.fundManagerId && (
        <section className="card">
          <h2>Link Fund Manager Profile</h2>
          <p className="meta">Enter your fund manager ID if it is not present in Supabase user metadata.</p>
          <input
            type="number"
            value={manualFundManagerId}
            onChange={(event) => setManualFundManagerId(event.target.value)}
            placeholder="Fund Manager ID"
          />
        </section>
      )}

      <section className="card">
        <h2>MVP Stock Universe (CSV Seeded)</h2>
        {assets.length === 0 ? <p>No stocks loaded yet.</p> : (
          <ul>
            {assets.map((asset) => (
              <li key={asset.id}>
                {asset.symbol} - {asset.name} @ {asset.currentPrice}
              </li>
            ))}
          </ul>
        )}
      </section>

      {isManagerRole && (
        <>
          <section className="card">
            <h2>Create Customer</h2>
            <form className="form" onSubmit={createCustomer}>
              {authContext.role === 'ADMIN' && (
                <input
                  type="number"
                  placeholder="Fund Manager ID"
                  value={newCustomer.fundManagerId}
                  onChange={(event) => setNewCustomer((prev) => ({ ...prev, fundManagerId: event.target.value }))}
                  required
                />
              )}
              <input
                type="text"
                placeholder="Customer Name"
                value={newCustomer.name}
                onChange={(event) => setNewCustomer((prev) => ({ ...prev, name: event.target.value }))}
                required
              />
              <input
                type="email"
                placeholder="Customer Email"
                value={newCustomer.email}
                onChange={(event) => setNewCustomer((prev) => ({ ...prev, email: event.target.value }))}
                required
              />
              <input
                type="text"
                placeholder="Phone"
                value={newCustomer.phone}
                onChange={(event) => setNewCustomer((prev) => ({ ...prev, phone: event.target.value }))}
              />
              <button type="submit">Create Customer</button>
            </form>
          </section>

          <section className="card">
            <h2>Customers</h2>
            {customers.length === 0 ? <p>No customers found.</p> : (
              <>
                <label htmlFor="customerSelect">Select customer to view portfolios</label>
                <select
                  id="customerSelect"
                  value={selectedCustomerId}
                  onChange={(event) => {
                    setSelectedCustomerId(event.target.value)
                    setNewPortfolio((prev) => ({ ...prev, customerId: event.target.value }))
                  }}
                >
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      #{customer.id} - {customer.name}
                    </option>
                  ))}
                </select>
              </>
            )}
          </section>

          <section className="card">
            <h2>Create Portfolio</h2>
            <form className="form" onSubmit={createPortfolio}>
              <input
                type="number"
                placeholder="Customer ID"
                value={newPortfolio.customerId}
                onChange={(event) => setNewPortfolio((prev) => ({ ...prev, customerId: event.target.value }))}
                required
              />
              <input
                type="text"
                placeholder="Portfolio name"
                value={newPortfolio.portfolioName}
                onChange={(event) => setNewPortfolio((prev) => ({ ...prev, portfolioName: event.target.value }))}
                required
              />
              <input
                type="number"
                step="0.01"
                placeholder="Total investment"
                value={newPortfolio.totalInvestment}
                onChange={(event) => setNewPortfolio((prev) => ({ ...prev, totalInvestment: event.target.value }))}
                required
              />
              <input
                type="number"
                step="0.01"
                placeholder="Current value"
                value={newPortfolio.currentValue}
                onChange={(event) => setNewPortfolio((prev) => ({ ...prev, currentValue: event.target.value }))}
                required
              />
              <button type="submit">Create Portfolio</button>
            </form>
          </section>
        </>
      )}

      <section className="card">
        <h2>Portfolios {authContext.role === 'CUSTOMER' ? '(View Only)' : ''}</h2>
        {portfolios.length === 0 ? (
          <p>No portfolios available.</p>
        ) : (
          <ul>
            {portfolios.map((portfolio) => (
              <li key={portfolio.id}>
                #{portfolio.id} - {portfolio.portfolioName} | Investment: {portfolio.totalInvestment} | Current: {portfolio.currentValue}
              </li>
            ))}
          </ul>
        )}
      </section>

      {appError && <p className="error">{appError}</p>}
    </main>
  )
}

export default App
