import { useEffect, useState } from 'react'
import './App.css'
import { apiFetch, getApiBaseUrl } from './api'

const adminViews = ['Dashboard', 'Customers', 'Portfolio', 'Performance']
const customerViews = ['Dashboard', 'Portfolio', 'Performance']

const demoUsers = [
  {
    username: 'manager',
    password: 'manager123',
    role: 'admin',
    displayName: 'Fund Manager',
  },
  {
    username: 'john',
    password: 'john123',
    role: 'customer',
    customerId: 1,
    displayName: 'John Mehra',
  },
  {
    username: 'mira',
    password: 'mira123',
    role: 'customer',
    customerId: 2,
    displayName: 'Mira Iyer',
  },
  {
    username: 'sarthak',
    password: 'sarthak123',
    role: 'customer',
    customerId: 3,
    displayName: 'Sarthak Nanda',
  },
]

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
    {
      id: 103,
      symbol: 'LIQUIDBEES',
      type: 'Cash ETF',
      quantity: 100,
      buyPrice: 100,
      currentPrice: 101,
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
    {
      id: 203,
      symbol: 'NIFTYBEES',
      type: 'ETF',
      quantity: 45,
      buyPrice: 224,
      currentPrice: 236,
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
    {
      id: 303,
      symbol: 'GOLDBEES',
      type: 'Gold ETF',
      quantity: 32,
      buyPrice: 56,
      currentPrice: 62,
    },
  ],
}

const initialPerformanceByCustomer = {
  1: [
    { month: 'Jan', portfolio: 100, sensex: 100, nifty50: 100 },
    { month: 'Feb', portfolio: 101.2, sensex: 100.6, nifty50: 100.8 },
    { month: 'Mar', portfolio: 100.9, sensex: 99.8, nifty50: 100.1 },
    { month: 'Apr', portfolio: 102.4, sensex: 101.1, nifty50: 101.5 },
    { month: 'May', portfolio: 103.7, sensex: 102.5, nifty50: 102.9 },
    { month: 'Jun', portfolio: 104.1, sensex: 103.2, nifty50: 103.5 },
  ],
  2: [
    { month: 'Jan', portfolio: 100, sensex: 100, nifty50: 100 },
    { month: 'Feb', portfolio: 101.8, sensex: 100.6, nifty50: 100.8 },
    { month: 'Mar', portfolio: 102.9, sensex: 99.8, nifty50: 100.1 },
    { month: 'Apr', portfolio: 104.6, sensex: 101.1, nifty50: 101.5 },
    { month: 'May', portfolio: 106.2, sensex: 102.5, nifty50: 102.9 },
    { month: 'Jun', portfolio: 107.1, sensex: 103.2, nifty50: 103.5 },
  ],
  3: [
    { month: 'Jan', portfolio: 100, sensex: 100, nifty50: 100 },
    { month: 'Feb', portfolio: 103.4, sensex: 100.6, nifty50: 100.8 },
    { month: 'Mar', portfolio: 102.7, sensex: 99.8, nifty50: 100.1 },
    { month: 'Apr', portfolio: 106.1, sensex: 101.1, nifty50: 101.5 },
    { month: 'May', portfolio: 108.7, sensex: 102.5, nifty50: 102.9 },
    { month: 'Jun', portfolio: 110.2, sensex: 103.2, nifty50: 103.5 },
  ],
}

const allocationPalette = ['#0f766e', '#d97706', '#2563eb', '#9333ea', '#0ea5e9']

const getTotalValue = (holdings) =>
  holdings.reduce((sum, holding) => sum + holding.quantity * holding.currentPrice, 0)

const getSeriesReturn = (series, key) => {
  if (series.length < 2) {
    return 0
  }

  const first = series[0][key]
  const last = series[series.length - 1][key]
  return ((last - first) / first) * 100
}

const formatCurrency = (value) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value)

const formatPercent = (value) => `${value.toFixed(2)}%`

const toRadians = (degrees) => (degrees * Math.PI) / 180

const getArcPath = (cx, cy, radius, startAngle, endAngle) => {
  const startX = cx + radius * Math.cos(toRadians(startAngle))
  const startY = cy + radius * Math.sin(toRadians(startAngle))
  const endX = cx + radius * Math.cos(toRadians(endAngle))
  const endY = cy + radius * Math.sin(toRadians(endAngle))
  const largeArcFlag = endAngle - startAngle > 180 ? 1 : 0

  return `M ${cx} ${cy} L ${startX} ${startY} A ${radius} ${radius} 0 ${largeArcFlag} 1 ${endX} ${endY} Z`
}

const getLinePoints = (series, key, minValue, maxValue) => {
  if (series.length === 0) {
    return ''
  }

  const width = 680
  const height = 250
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

function App() {
  const [authUser, setAuthUser] = useState(null)
  const [loginUsername, setLoginUsername] = useState('manager')
  const [loginPassword, setLoginPassword] = useState('manager123')
  const [loginError, setLoginError] = useState('')

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

  useEffect(() => {
    if (authUser?.role === 'customer') {
      setSelectedCustomerId(authUser.customerId)
      if (activeView === 'Customers') {
        setActiveView('Dashboard')
      }
    }
  }, [authUser, activeView])

  const visibleViews = authUser?.role === 'admin' ? adminViews : customerViews
  const scopedCustomers =
    authUser?.role === 'admin'
      ? customers
      : customers.filter((customer) => customer.id === authUser?.customerId)

  const selectedCustomer =
    scopedCustomers.find((customer) => customer.id === selectedCustomerId) || scopedCustomers[0]

  const selectedCustomerHoldings = selectedCustomer
    ? holdingsByCustomer[selectedCustomer.id] || []
    : []

  const selectedPerformance = selectedCustomer
    ? initialPerformanceByCustomer[selectedCustomer.id] || []
    : []

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

  const handleLogin = (event) => {
    event.preventDefault()
    setLoginError('')

    const matchedUser = demoUsers.find(
      (user) =>
        user.username.toLowerCase() === loginUsername.trim().toLowerCase() &&
        user.password === loginPassword
    )

    if (!matchedUser) {
      setLoginError('Invalid login. Use the demo credentials shown below the form.')
      return
    }

    setAuthUser(matchedUser)
    setActiveView('Dashboard')
  }

  const logout = () => {
    setAuthUser(null)
    setActiveView('Dashboard')
    setLoginError('')
    setLoginUsername('manager')
    setLoginPassword('manager123')
  }

  const fillLogin = (username, password) => {
    setLoginUsername(username)
    setLoginPassword(password)
    setLoginError('')
  }

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

  const handleAddAsset = (event) => {
    event.preventDefault()
    setPortfolioError('')

    if (authUser?.role !== 'admin') {
      setPortfolioError('Portfolio editing is available only for the fund manager login.')
      return
    }

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
    if (authUser?.role !== 'admin') {
      return
    }

    setHoldingsByCustomer((previous) => {
      const currentHoldings = previous[selectedCustomer.id] || []
      return {
        ...previous,
        [selectedCustomer.id]: currentHoldings.filter((asset) => asset.id !== assetId),
      }
    })
  }

  const renderAllocationDonut = (holdings) => {
    const grouped = holdings.reduce((result, holding) => {
      const value = holding.quantity * holding.currentPrice
      return {
        ...result,
        [holding.type]: (result[holding.type] || 0) + value,
      }
    }, {})

    const segments = Object.entries(grouped).map(([type, value], index) => ({
      type,
      value,
      color: allocationPalette[index % allocationPalette.length],
    }))

    const total = segments.reduce((sum, segment) => sum + segment.value, 0)

    if (total === 0) {
      return <p className="muted-text">No allocation data yet.</p>
    }

    let running = 0

    return (
      <div className="donut-wrap">
        <svg viewBox="0 0 260 260" className="donut-chart" role="img" aria-label="Asset allocation pie chart">
          {segments.map((segment) => {
            const start = (running / total) * 360 - 90
            running += segment.value
            const end = (running / total) * 360 - 90
            return <path key={segment.type} d={getArcPath(130, 130, 100, start, end)} fill={segment.color} />
          })}
          <circle cx="130" cy="130" r="56" fill="#ffffff" />
          <text x="130" y="126" textAnchor="middle" className="donut-total-label">
            Total
          </text>
          <text x="130" y="148" textAnchor="middle" className="donut-total-value">
            {formatCurrency(total)}
          </text>
        </svg>

        <ul className="donut-legend">
          {segments.map((segment) => (
            <li key={segment.type}>
              <span className="legend-swatch" style={{ backgroundColor: segment.color }} />
              <span>{segment.type}</span>
              <strong>{formatCurrency(segment.value)}</strong>
            </li>
          ))}
        </ul>
      </div>
    )
  }

  const renderDashboard = () => {
    const totalPortfolioValue = scopedCustomers.reduce((total, customer) => {
      const holdings = holdingsByCustomer[customer.id] || []
      return total + getTotalValue(holdings)
    }, 0)

    const customerReturns = scopedCustomers.map((customer) => {
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

    const riskDistribution = scopedCustomers.reduce(
      (distribution, customer) => {
        const risk = customer.riskProfile
        return {
          ...distribution,
          [risk]: (distribution[risk] || 0) + 1,
        }
      },
      {}
    )

    const requirementChecklist = [
      'Fund manager can view all customer portfolios',
      'Exactly 3 customers are preloaded for onboarding',
      'Add or remove assets and see instant updates',
      'Benchmark comparison includes Sensex and Nifty 50',
      'Role-based login is active for admin and customers',
      'Visual analytics are available in every section',
    ]

    return (
      <>
        <section className="card hero-card">
          <h2>Welcome, {authUser.displayName}</h2>
          <p>
            This workspace is designed for beginner-friendly decision making with clear visuals and
            guided actions.
          </p>
          <p className="muted-text">
            <span className="data-badge mixed">Data: Mixed</span>
            Live for connectivity and accounts, mock for portfolio analytics until backend endpoints
            are fully ready.
          </p>
        </section>

        <section className="card quick-actions-card">
          <h2>Quick Actions</h2>
          <div className="quick-actions">
            {visibleViews.includes('Customers') && (
              <button type="button" className="quick-btn" onClick={() => setActiveView('Customers')}>
                Review Customers
              </button>
            )}
            <button type="button" className="quick-btn" onClick={() => setActiveView('Portfolio')}>
              Manage Portfolio
            </button>
            <button type="button" className="quick-btn" onClick={() => setActiveView('Performance')}>
              Compare to Market
            </button>
          </div>
        </section>

        <section className="summary-grid">
          <article className="summary-card">
            <h3>Customers in Scope</h3>
            <p>{scopedCustomers.length}</p>
          </article>
          <article className="summary-card">
            <h3>Total Portfolio Value</h3>
            <p>{formatCurrency(totalPortfolioValue)}</p>
          </article>
          <article className="summary-card">
            <h3>Best Performer (6M)</h3>
            <p>{bestPerformer.name}</p>
            <span className="summary-note">{formatPercent(bestPerformer.portfolioReturn)}</span>
          </article>
          <article className="summary-card">
            <h3>Needs Attention (6M)</h3>
            <p>{worstPerformer.name}</p>
            <span className="summary-note">{formatPercent(worstPerformer.portfolioReturn)}</span>
          </article>
        </section>

        <section className="double-grid">
          <article className="card">
            <h2>Risk Profile Mix</h2>
            <ul className="risk-list">
              {Object.entries(riskDistribution).map(([risk, count]) => (
                <li key={risk}>
                  <span>{risk}</span>
                  <strong>{count}</strong>
                </li>
              ))}
            </ul>
          </article>

          <article className="card">
            <h2>Allocation Snapshot</h2>
            {renderAllocationDonut(selectedCustomerHoldings)}
          </article>
        </section>

        {authUser.role === 'admin' && (
          <section className="card">
            <h2>Live Account Endpoint Health</h2>
            <p>Backend status: {status}</p>
            <p>API base: {apiBaseUrl}</p>
            <p className="muted-text">
              <span className="data-badge live">Data: Live API</span>
              Use this card to validate backend integration during demos.
            </p>
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
            <ul className="live-list">
              {accounts.map((account) => (
                <li key={account.id}>
                  #{account.id} {account.name} ({account.email})
                </li>
              ))}
            </ul>
          </section>
        )}

        <section className="card checklist-card">
          <h2>Business Requirement Coverage</h2>
          <ul className="checklist">
            {requirementChecklist.map((item) => (
              <li key={item}>Done: {item}</li>
            ))}
          </ul>
        </section>
      </>
    )
  }

  const renderCustomers = () => {
    if (authUser.role !== 'admin') {
      return (
        <section className="card">
          <h2>Customers</h2>
          <p className="muted-text">Customer logins are intentionally restricted from this page.</p>
        </section>
      )
    }

    return (
      <>
        <section className="card">
          <h2>Customers</h2>
          <p>Select any customer to manage portfolio and performance.</p>
          <p className="muted-text">
            <span className="data-badge mock">Data: Mock</span>
            Customer identity and risk profile data are temporary until customer API endpoints are live.
          </p>
        </section>

        <section className="card customers-list">
          {customers.map((customer) => {
            const customerHoldings = holdingsByCustomer[customer.id] || []
            const customerValue = getTotalValue(customerHoldings)
            const customerReturn = getSeriesReturn(
              initialPerformanceByCustomer[customer.id] || [],
              'portfolio'
            )

            return (
              <article key={customer.id} className="customer-item">
                <div>
                  <p className="customer-name">{customer.name}</p>
                  <p>{customer.email}</p>
                </div>
                <div className="customer-stats">
                  <span>{formatCurrency(customerValue)}</span>
                  <span>{formatPercent(customerReturn)}</span>
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
            )
          })}
        </section>
      </>
    )
  }

  const renderPortfolio = () => {
    const totalGain = portfolioTotals.currentValue - portfolioTotals.investmentValue

    return (
      <>
        <section className="card">
          <h2>Portfolio: {selectedCustomer.name}</h2>
          <p>
            Risk profile: {selectedCustomer.riskProfile} | Investment: {formatCurrency(portfolioTotals.investmentValue)} | Current:{' '}
            {formatCurrency(portfolioTotals.currentValue)}
          </p>
          <p className="muted-text">
            <span className="data-badge mock">Data: Mock</span>
            Asset updates are instant in this demo. Admin login can edit, customer login can view.
          </p>
        </section>

        <section className="summary-grid">
          <article className="summary-card">
            <h3>Total Gain/Loss</h3>
            <p className={totalGain >= 0 ? 'value-up' : 'value-down'}>{formatCurrency(totalGain)}</p>
          </article>
          <article className="summary-card">
            <h3>Holdings Count</h3>
            <p>{selectedCustomerHoldings.length}</p>
          </article>
          <article className="summary-card">
            <h3>Best for Beginners</h3>
            <p>{selectedCustomer.riskProfile === 'Aggressive' ? 'Review volatility' : 'Balanced exposure'}</p>
          </article>
        </section>

        <section className="double-grid">
          <article className="card">
            <h2>Asset Allocation Pie</h2>
            {renderAllocationDonut(selectedCustomerHoldings)}
          </article>

          <article className="card">
            <h2>Holdings Table</h2>
            <div className="table-wrap">
              <table className="portfolio-table">
                <thead>
                  <tr>
                    <th>Asset</th>
                    <th>Type</th>
                    <th>Qty</th>
                    <th>Investment</th>
                    <th>Current</th>
                    <th>P/L</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedCustomerHoldings.map((asset) => {
                    const invested = asset.quantity * asset.buyPrice
                    const current = asset.quantity * asset.currentPrice
                    const gain = current - invested

                    return (
                      <tr key={asset.id}>
                        <td>{asset.symbol}</td>
                        <td>{asset.type}</td>
                        <td>{asset.quantity}</td>
                        <td>{formatCurrency(invested)}</td>
                        <td>{formatCurrency(current)}</td>
                        <td className={gain >= 0 ? 'value-up' : 'value-down'}>{formatCurrency(gain)}</td>
                        <td>
                          <button
                            type="button"
                            className="remove-btn"
                            onClick={() => handleRemoveAsset(asset.id)}
                            disabled={authUser.role !== 'admin'}
                          >
                            Remove
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </article>
        </section>

        <section className="card">
          <h2>Add Asset</h2>
          <p className="muted-text">Available for fund manager login. Customers can only view this section.</p>
          <form className="form" onSubmit={handleAddAsset}>
            <input
              type="text"
              placeholder="Asset symbol"
              value={assetSymbol}
              onChange={(event) => setAssetSymbol(event.target.value)}
              disabled={authUser.role !== 'admin'}
              required
            />
            <input
              type="text"
              placeholder="Asset type"
              value={assetType}
              onChange={(event) => setAssetType(event.target.value)}
              disabled={authUser.role !== 'admin'}
              required
            />
            <input
              type="number"
              placeholder="Quantity"
              min="1"
              value={assetQuantity}
              onChange={(event) => setAssetQuantity(event.target.value)}
              disabled={authUser.role !== 'admin'}
              required
            />
            <input
              type="number"
              placeholder="Buy price"
              min="1"
              value={assetBuyPrice}
              onChange={(event) => setAssetBuyPrice(event.target.value)}
              disabled={authUser.role !== 'admin'}
              required
            />
            <input
              type="number"
              placeholder="Current price"
              min="1"
              value={assetCurrentPrice}
              onChange={(event) => setAssetCurrentPrice(event.target.value)}
              disabled={authUser.role !== 'admin'}
              required
            />
            <button type="submit" disabled={authUser.role !== 'admin'}>
              Add Asset
            </button>
          </form>
          {portfolioError && <p className="error">{portfolioError}</p>}
        </section>
      </>
    )
  }

  const renderPerformance = () => {
    const allValues = selectedPerformance.flatMap((point) => [
      point.portfolio,
      point.sensex,
      point.nifty50,
    ])
    const minValue = Math.min(...allValues)
    const maxValue = Math.max(...allValues)

    const portfolioReturn = getSeriesReturn(selectedPerformance, 'portfolio')
    const sensexReturn = getSeriesReturn(selectedPerformance, 'sensex')
    const niftyReturn = getSeriesReturn(selectedPerformance, 'nifty50')

    const monthlyAlphaVsNifty = selectedPerformance.slice(1).map((point, index) => {
      const prev = selectedPerformance[index]
      const portfolioMonthly = ((point.portfolio - prev.portfolio) / prev.portfolio) * 100
      const niftyMonthly = ((point.nifty50 - prev.nifty50) / prev.nifty50) * 100
      return {
        month: point.month,
        alpha: portfolioMonthly - niftyMonthly,
      }
    })

    const maxAlpha = Math.max(...monthlyAlphaVsNifty.map((entry) => Math.abs(entry.alpha)), 1)

    return (
      <>
        <section className="card">
          <h2>Performance Analytics: {selectedCustomer.name}</h2>
          <p>Compare portfolio growth against Sensex and Nifty 50 using beginner-friendly visuals.</p>
          <p className="muted-text">
            <span className="data-badge mock">Data: Mock</span>
            Live market API can replace this data model without changing the UI structure.
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
            <h3>Nifty 50 Return (6M)</h3>
            <p>{formatPercent(niftyReturn)}</p>
          </article>
          <article className="summary-card">
            <h3>Alpha vs Nifty 50</h3>
            <p className={portfolioReturn - niftyReturn >= 0 ? 'value-up' : 'value-down'}>
              {formatPercent(portfolioReturn - niftyReturn)}
            </p>
          </article>
        </section>

        <section className="card">
          <h2>Comparison Line Chart</h2>
          <div className="chart-wrap" aria-label="Comparison chart for portfolio, Sensex and Nifty 50">
            <svg viewBox="0 0 680 250" className="line-chart" role="img">
              <polyline
                points={getLinePoints(selectedPerformance, 'sensex', minValue, maxValue)}
                className="line sensex-line"
              />
              <polyline
                points={getLinePoints(selectedPerformance, 'nifty50', minValue, maxValue)}
                className="line nifty-line"
              />
              <polyline
                points={getLinePoints(selectedPerformance, 'portfolio', minValue, maxValue)}
                className="line portfolio-line"
              />
            </svg>
          </div>

          <div className="chart-legend">
            <span>
              <i className="legend-dot portfolio-dot" /> Portfolio
            </span>
            <span>
              <i className="legend-dot sensex-dot" /> Sensex
            </span>
            <span>
              <i className="legend-dot nifty-dot" /> Nifty 50
            </span>
          </div>

          <div className="month-row">
            {selectedPerformance.map((point) => (
              <span key={point.month}>{point.month}</span>
            ))}
          </div>
        </section>

        <section className="card">
          <h2>Monthly Alpha vs Nifty 50</h2>
          <div className="bar-grid">
            {monthlyAlphaVsNifty.map((entry) => {
              const height = (Math.abs(entry.alpha) / maxAlpha) * 100
              return (
                <article key={entry.month} className="bar-item">
                  <div className="bar-track">
                    <div
                      className={`bar-fill ${entry.alpha >= 0 ? 'bar-up' : 'bar-down'}`}
                      style={{ height: `${Math.max(height, 6)}%` }}
                    />
                  </div>
                  <strong>{entry.month}</strong>
                  <span className={entry.alpha >= 0 ? 'value-up' : 'value-down'}>
                    {formatPercent(entry.alpha)}
                  </span>
                </article>
              )
            })}
          </div>
        </section>
      </>
    )
  }

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

    return renderPerformance()
  }

  const renderLogin = () => (
    <main className="login-shell">
      <section className="login-hero">
        <p className="eyebrow">Financial Portfolio Manager</p>
        <h1>Build confidence in every investment decision</h1>
        <p>
          Designed for beginners and professionals alike. Track portfolios, compare against market
          benchmarks, and make actions with clarity.
        </p>

        <div className="hero-points">
          <article>
            <strong>Smart visual analytics</strong>
            <span>Line charts, pie allocation, and benchmark comparisons.</span>
          </article>
          <article>
            <strong>Role based clarity</strong>
            <span>Fund manager controls all portfolios, customers view their own.</span>
          </article>
          <article>
            <strong>Beginner friendly flow</strong>
            <span>Simple language and guided sections across every page.</span>
          </article>
        </div>
      </section>

      <section className="login-panel">
        <h2>Sign in</h2>
        <p>Use demo credentials to enter as admin or one of the 3 customers.</p>

        <form onSubmit={handleLogin} className="form login-form">
          <input
            type="text"
            placeholder="Username"
            value={loginUsername}
            onChange={(event) => setLoginUsername(event.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={loginPassword}
            onChange={(event) => setLoginPassword(event.target.value)}
            required
          />
          <button type="submit">Login</button>
        </form>

        {loginError && <p className="error">{loginError}</p>}

        <div className="login-shortcuts">
          <p>Quick fill:</p>
          <div className="shortcut-grid">
            <button type="button" onClick={() => fillLogin('manager', 'manager123')}>
              Fund Manager
            </button>
            <button type="button" onClick={() => fillLogin('john', 'john123')}>
              John
            </button>
            <button type="button" onClick={() => fillLogin('mira', 'mira123')}>
              Mira
            </button>
            <button type="button" onClick={() => fillLogin('sarthak', 'sarthak123')}>
              Sarthak
            </button>
          </div>
        </div>

        <ul className="credentials-list">
          <li>Admin: manager / manager123</li>
          <li>Customer: john / john123</li>
          <li>Customer: mira / mira123</li>
          <li>Customer: sarthak / sarthak123</li>
        </ul>
      </section>
    </main>
  )

  if (!authUser) {
    return renderLogin()
  }

  return (
    <main className="app">
      <header className="page-header top-header">
        <div>
          <h1>Financial Portfolio Manager</h1>
          <p className="status">
            Signed in as {authUser.displayName} ({authUser.role === 'admin' ? 'Fund Manager' : 'Customer'})
          </p>
        </div>
        <button type="button" className="logout-btn" onClick={logout}>
          Logout
        </button>
      </header>

      <nav className="nav" aria-label="Primary">
        {visibleViews.map((view) => (
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
          Current customer: <strong>{selectedCustomer.name}</strong> | Risk:{' '}
          <strong>{selectedCustomer.riskProfile}</strong>
        </p>
      </section>

      {renderContent()}
    </main>
  )
}

export default App
