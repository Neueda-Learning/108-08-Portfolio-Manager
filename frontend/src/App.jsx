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

const initialPerformanceByCustomer = {
  1: [
    { month: 'Jan', portfolio: 100, sensex: 100 },
    { month: 'Feb', portfolio: 101.2, sensex: 100.6 },
    { month: 'Mar', portfolio: 100.9, sensex: 99.8 },
    { month: 'Apr', portfolio: 102.4, sensex: 101.1 },
    { month: 'May', portfolio: 103.7, sensex: 102.5 },
    { month: 'Jun', portfolio: 104.1, sensex: 103.2 },
  ],
  2: [
    { month: 'Jan', portfolio: 100, sensex: 100 },
    { month: 'Feb', portfolio: 101.8, sensex: 100.6 },
    { month: 'Mar', portfolio: 102.9, sensex: 99.8 },
    { month: 'Apr', portfolio: 104.6, sensex: 101.1 },
    { month: 'May', portfolio: 106.2, sensex: 102.5 },
    { month: 'Jun', portfolio: 107.1, sensex: 103.2 },
  ],
  3: [
    { month: 'Jan', portfolio: 100, sensex: 100 },
    { month: 'Feb', portfolio: 103.4, sensex: 100.6 },
    { month: 'Mar', portfolio: 102.7, sensex: 99.8 },
    { month: 'Apr', portfolio: 106.1, sensex: 101.1 },
    { month: 'May', portfolio: 108.7, sensex: 102.5 },
    { month: 'Jun', portfolio: 110.2, sensex: 103.2 },
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
  const selectedPerformance = initialPerformanceByCustomer[selectedCustomer.id] || []

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

  const formatPercent = (value) => `${value.toFixed(2)}%`

  const getSeriesReturn = (series, key) => {
    if (series.length < 2) {
      return 0
    }

    const first = series[0][key]
    const last = series[series.length - 1][key]
    return ((last - first) / first) * 100
  }

  const getLinePoints = (series, key, minValue, maxValue) => {
    if (series.length === 0) {
      return ''
    }

    const width = 560
    const height = 220
    const stepX = series.length > 1 ? width / (series.length - 1) : width
    const range = maxValue - minValue || 1

    return series
      .map((point, index) => {
        const x = index * stepX
        const y = height - ((point[key] - minValue) / range) * height
        return `${x},${y}`
      })
      .join(' ')
  }

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

  const renderDashboard = () => {
    const totalPortfolioValue = customers.reduce((total, customer) => {
      const holdings = holdingsByCustomer[customer.id] || []
      const customerValue = holdings.reduce(
        (sum, holding) => sum + holding.quantity * holding.currentPrice,
        0
      )
      return total + customerValue
    }, 0)

    const riskDistribution = customers.reduce(
      (distribution, customer) => {
        const risk = customer.riskProfile
        return {
          ...distribution,
          [risk]: (distribution[risk] || 0) + 1,
        }
      },
      {}
    )

    const customerReturns = customers.map((customer) => {
      const series = initialPerformanceByCustomer[customer.id] || []
      return {
        name: customer.name,
        portfolioReturn: getSeriesReturn(series, 'portfolio'),
      }
    })

    const bestPerformer = customerReturns.reduce((best, current) =>
      current.portfolioReturn > best.portfolioReturn ? current : best
    )

    const worstPerformer = customerReturns.reduce((worst, current) =>
      current.portfolioReturn < worst.portfolioReturn ? current : worst
    )

    const riskSummary = Object.entries(riskDistribution)
      .map(([risk, count]) => `${risk}: ${count}`)
      .join(' | ')

    const requirementChecklist = [
      'Fund Manager admin view is available',
      'Three customers are loaded at startup',
      'Customer portfolios can be viewed',
      'Portfolio assets can be added and removed',
      'Portfolio updates are reflected immediately',
      'Performance page compares portfolio with Sensex',
    ]

    return (
      <>
        <section className="card">
          <h2>Fund Manager Overview</h2>
          <p>Backend status: {status}</p>
          <p>API base: {apiBaseUrl}</p>
          <p className="muted-text">
            <span className="data-badge mixed">Data: Mixed</span> Live for API connectivity/accounts, mock for portfolio analytics.
          </p>
        </section>

        <section className="summary-grid">
          <article className="summary-card">
            <h3>Total Customers</h3>
            <p>{customers.length}</p>
          </article>
          <article className="summary-card">
            <h3>Total Portfolio Value</h3>
            <p>{formatCurrency(totalPortfolioValue)}</p>
          </article>
          <article className="summary-card">
            <h3>Top Performer (6M)</h3>
            <p>{bestPerformer.name}</p>
            <span className="summary-note">{formatPercent(bestPerformer.portfolioReturn)}</span>
          </article>
          <article className="summary-card">
            <h3>Needs Attention (6M)</h3>
            <p>{worstPerformer.name}</p>
            <span className="summary-note">{formatPercent(worstPerformer.portfolioReturn)}</span>
          </article>
        </section>

        <section className="card">
          <h2>Risk Profile Mix</h2>
          <p>{riskSummary}</p>
          <p className="muted-text">This gives a quick view of how balanced your customer base is by risk appetite.</p>
        </section>

        <section className="card">
          <h2>Create Account (Live API Check)</h2>
          <p className="muted-text"><span className="data-badge live">Data: Live API</span> Saves directly to backend account endpoint.</p>
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
          <h2>Accounts (Live API List)</h2>
          <p className="muted-text"><span className="data-badge live">Data: Live API</span> List fetched from backend account endpoint.</p>
          {accounts.length === 0 ? (
            <p>No accounts yet. Add one above to confirm backend write flow.</p>
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

        <section className="card checklist-card">
          <h2>Business Requirement Coverage</h2>
          <p className="muted-text">Quick checklist for demo/review readiness in this build.</p>
          <ul className="checklist">
            {requirementChecklist.map((item) => (
              <li key={item}>Done: {item}</li>
            ))}
          </ul>
        </section>
      </>
    )
  }

  const renderCustomers = () => (
    <>
      <section className="card">
        <h2>Customers</h2>
        <p>Manage customer profiles and choose a customer for portfolio actions.</p>
        <p className="muted-text">
          <span className="data-badge mock">Data: Mock</span> Tip: select a customer here, then open Portfolio or Performance to continue.
        </p>
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
                {selectedCustomerId === customer.id ? 'Current' : 'Select'}
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
        <p className="muted-text">
          <span className="data-badge mock">Data: Mock</span> Any add or remove action here updates this customer's portfolio instantly in the app.
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
          <p>No holdings yet for this customer. Use Add Asset below to start the portfolio.</p>
        ) : (
          <div className="table-wrap">
            <table className="portfolio-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Type</th>
                  <th>Qty</th>
                  <th>Investment Value</th>
                  <th>Current Value</th>
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
        <p className="muted-text">Use realistic values so performance comparisons remain meaningful.</p>
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

  const renderPerformance = () => {
    const allValues = selectedPerformance.flatMap((point) => [point.portfolio, point.sensex])
    const minValue = Math.min(...allValues)
    const maxValue = Math.max(...allValues)
    const portfolioReturn = getSeriesReturn(selectedPerformance, 'portfolio')
    const sensexReturn = getSeriesReturn(selectedPerformance, 'sensex')
    const alpha = portfolioReturn - sensexReturn

    return (
      <>
        <section className="card">
          <h2>Performance</h2>
          <p>Portfolio trend comparison for {selectedCustomer.name} against Sensex benchmark.</p>
          <p className="muted-text">
            <span className="data-badge mock">Data: Mock</span> Higher blue line means portfolio is outperforming benchmark over that period.
          </p>
        </section>

        <section className="summary-grid">
          <article className="summary-card">
            <h3>Portfolio Return (6M)</h3>
            <p>{formatPercent(portfolioReturn)}</p>
          </article>
          <article className="summary-card">
            <h3>Sensex Return (6M)</h3>
            <p>{formatPercent(sensexReturn)}</p>
          </article>
          <article className="summary-card">
            <h3>Relative Alpha</h3>
            <p>{formatPercent(alpha)}</p>
          </article>
        </section>

        <section className="card">
          <h2>Portfolio vs Sensex</h2>
          <div className="chart-wrap" aria-label="Portfolio and Sensex trend chart">
            <svg viewBox="0 0 560 220" className="line-chart" role="img">
              <polyline
                points={getLinePoints(selectedPerformance, 'sensex', minValue, maxValue)}
                className="line sensex-line"
              />
              <polyline
                points={getLinePoints(selectedPerformance, 'portfolio', minValue, maxValue)}
                className="line portfolio-line"
              />
            </svg>
          </div>

          <div className="chart-legend">
            <span><i className="legend-dot portfolio-dot" /> Portfolio</span>
            <span><i className="legend-dot sensex-dot" /> Sensex</span>
          </div>

          <div className="month-row">
            {selectedPerformance.map((point) => (
              <span key={point.month}>{point.month}</span>
            ))}
          </div>
        </section>

        <section className="card tip-card">
          <h2>How To Read This</h2>
          <p>Portfolio Return: overall growth of selected customer portfolio in this 6-month window.</p>
          <p>Sensex Return: benchmark movement in the same period.</p>
          <p>Relative Alpha: positive means portfolio beat Sensex, negative means it lagged.</p>
        </section>
      </>
    )
  }

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

    if (activeView === 'Performance') {
      return renderPerformance()
    }

    return renderPlaceholder('Page', 'This page is not available.')
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

      <section className="context-bar" aria-live="polite">
        <p>
          Current customer: <strong>{selectedCustomer.name}</strong> | Risk: <strong>{selectedCustomer.riskProfile}</strong>
        </p>
      </section>

      {renderContent()}
    </main>
  )
}

export default App
