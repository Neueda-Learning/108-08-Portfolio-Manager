import { useEffect, useState } from 'react'
import './App.css'
import { apiFetch, getApiBaseUrl } from './api'

const views = ['Dashboard', 'Customers', 'Portfolio', 'Performance']
const initialCustomers = [
  {
    id: 1,
    name: 'John Mehra',
    email: 'john.mehra@example.com',
    riskProfile: 'Conservative',
  },
  {
    id: 2,
    name: 'Mira Iyer',
    email: 'mira.iyer@example.com',
    riskProfile: 'Moderate',
  },
  {
    id: 3,
    name: 'Sarthak Nanda',
    email: 'sarthak.nanda@example.com',
    riskProfile: 'Aggressive',
  },
]

const initialHoldingsByCustomer = {
  1: [
    {
      id: 101,
      symbol: 'HDFCBANK',
      type: 'Equity',
      quantity: 25,
      buyPrice: 1500,
      currentPrice: 1650,
    },
    {
      id: 102,
      symbol: 'SBIGILT',
      type: 'Debt Fund',
      quantity: 40,
      buyPrice: 120,
      currentPrice: 124,
    },
  ],
  2: [
    {
      id: 201,
      symbol: 'INFY',
      type: 'Equity',
      quantity: 35,
      buyPrice: 1420,
      currentPrice: 1560,
    },
    {
      id: 202,
      symbol: 'ICICIPRU',
      type: 'Hybrid Fund',
      quantity: 50,
      buyPrice: 610,
      currentPrice: 635,
    },
  ],
  3: [
    {
      id: 301,
      symbol: 'TATAELXSI',
      type: 'Equity',
      quantity: 20,
      buyPrice: 7600,
      currentPrice: 8125,
    },
    {
      id: 302,
      symbol: 'NIFTYBEES',
      type: 'ETF',
      quantity: 60,
      buyPrice: 221,
      currentPrice: 235,
    },
  ],
}

function App() {
  const [activeView, setActiveView] = useState('Dashboard')
  const [customers] = useState(initialCustomers)
  const [selectedCustomerId, setSelectedCustomerId] = useState(initialCustomers[0].id)
  const [holdingsByCustomer, setHoldingsByCustomer] = useState(initialHoldingsByCustomer)
  const [status, setStatus] = useState('Checking connection...')
  const [accounts, setAccounts] = useState([])
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [assetSymbol, setAssetSymbol] = useState('')
  const [assetType, setAssetType] = useState('Equity')
  const [assetQuantity, setAssetQuantity] = useState('')
  const [assetBuyPrice, setAssetBuyPrice] = useState('')
  const [assetCurrentPrice, setAssetCurrentPrice] = useState('')
  const [portfolioError, setPortfolioError] = useState('')
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

  const selectedCustomer =
    customers.find((customer) => customer.id === selectedCustomerId) || customers[0]
  const selectedCustomerHoldings = holdingsByCustomer[selectedCustomer.id] || []

  const portfolioTotals = selectedCustomerHoldings.reduce(
    (totals, holding) => {
      const investmentValue = holding.quantity * holding.buyPrice
      const currentValue = holding.quantity * holding.currentPrice
      return {
        investmentValue: totals.investmentValue + investmentValue,
        currentValue: totals.currentValue + currentValue,
      }
    },
    { investmentValue: 0, currentValue: 0 }
  )

  const formatCurrency = (value) =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(value)

  const handleAddAsset = (event) => {
    event.preventDefault()
    setPortfolioError('')

    const quantityValue = Number(assetQuantity)
    const buyPriceValue = Number(assetBuyPrice)
    const currentPriceValue = Number(assetCurrentPrice)

    if (!assetSymbol.trim() || !assetType.trim()) {
      setPortfolioError('Asset symbol and type are required.')
      return
    }

    if (quantityValue <= 0 || buyPriceValue <= 0 || currentPriceValue <= 0) {
      setPortfolioError('Quantity and price values must be greater than zero.')
      return
    }

    const newAsset = {
      id: Date.now(),
      symbol: assetSymbol.trim().toUpperCase(),
      type: assetType.trim(),
      quantity: quantityValue,
      buyPrice: buyPriceValue,
      currentPrice: currentPriceValue,
    }

    setHoldingsByCustomer((previous) => {
      const currentHoldings = previous[selectedCustomer.id] || []
      return {
        ...previous,
        [selectedCustomer.id]: [...currentHoldings, newAsset],
      }
    })

    setAssetSymbol('')
    setAssetType('Equity')
    setAssetQuantity('')
    setAssetBuyPrice('')
    setAssetCurrentPrice('')
  }

  const handleRemoveAsset = (assetId) => {
    setHoldingsByCustomer((previous) => {
      const currentHoldings = previous[selectedCustomer.id] || []
      return {
        ...previous,
        [selectedCustomer.id]: currentHoldings.filter((asset) => asset.id !== assetId),
      }
    })
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
          <p>{customers.length}</p>
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

  const renderCustomers = () => (
    <>
      <section className="card">
        <h2>Customers</h2>
        <p>Manage customer profiles and choose a customer for portfolio actions.</p>
      </section>

      <section className="card customers-list">
        {customers.map((customer) => (
          <article key={customer.id} className="customer-item">
            <div>
              <p className="customer-name">{customer.name}</p>
              <p>{customer.email}</p>
            </div>
            <div className="customer-actions">
              <span className="risk-chip">{customer.riskProfile}</span>
              <button
                type="button"
                className={`select-btn ${selectedCustomerId === customer.id ? 'selected' : ''}`}
                onClick={() => setSelectedCustomerId(customer.id)}
              >
                {selectedCustomerId === customer.id ? 'Selected' : 'Select'}
              </button>
            </div>
          </article>
        ))}
      </section>

      <section className="card">
        <h2>Selected Customer</h2>
        <p>Name: {selectedCustomer.name}</p>
        <p>Email: {selectedCustomer.email}</p>
        <p>Risk Profile: {selectedCustomer.riskProfile}</p>
      </section>
    </>
  )

  const renderPortfolio = () => (
    <>
      <section className="card">
        <h2>Portfolio</h2>
        <p>
          Viewing portfolio for {selectedCustomer.name} ({selectedCustomer.riskProfile})
        </p>
      </section>

      <section className="summary-grid">
        <article className="summary-card">
          <h3>Total Investment Value</h3>
          <p>{formatCurrency(portfolioTotals.investmentValue)}</p>
        </article>
        <article className="summary-card">
          <h3>Total Current Value</h3>
          <p>{formatCurrency(portfolioTotals.currentValue)}</p>
        </article>
      </section>

      <section className="card">
        <h2>Customer Holdings</h2>
        {selectedCustomerHoldings.length === 0 ? (
          <p>No holdings yet for this customer.</p>
        ) : (
          <div className="table-wrap">
            <table className="portfolio-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Type</th>
                  <th>Qty</th>
                  <th>Investment</th>
                  <th>Current</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {selectedCustomerHoldings.map((asset) => (
                  <tr key={asset.id}>
                    <td>{asset.symbol}</td>
                    <td>{asset.type}</td>
                    <td>{asset.quantity}</td>
                    <td>{formatCurrency(asset.quantity * asset.buyPrice)}</td>
                    <td>{formatCurrency(asset.quantity * asset.currentPrice)}</td>
                    <td>
                      <button
                        type="button"
                        className="remove-btn"
                        onClick={() => handleRemoveAsset(asset.id)}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="card">
        <h2>Add Asset</h2>
        <form className="form" onSubmit={handleAddAsset}>
          <input
            type="text"
            placeholder="Asset symbol (e.g. RELIANCE)"
            value={assetSymbol}
            onChange={(event) => setAssetSymbol(event.target.value)}
            required
          />
          <input
            type="text"
            placeholder="Asset type (e.g. Equity, ETF)"
            value={assetType}
            onChange={(event) => setAssetType(event.target.value)}
            required
          />
          <input
            type="number"
            placeholder="Quantity"
            min="1"
            value={assetQuantity}
            onChange={(event) => setAssetQuantity(event.target.value)}
            required
          />
          <input
            type="number"
            placeholder="Buy price"
            min="1"
            value={assetBuyPrice}
            onChange={(event) => setAssetBuyPrice(event.target.value)}
            required
          />
          <input
            type="number"
            placeholder="Current price"
            min="1"
            value={assetCurrentPrice}
            onChange={(event) => setAssetCurrentPrice(event.target.value)}
            required
          />
          <button type="submit">Add Asset</button>
        </form>
        {portfolioError && <p className="error">{portfolioError}</p>}
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
      return renderCustomers()
    }

    if (activeView === 'Portfolio') {
      return renderPortfolio()
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
