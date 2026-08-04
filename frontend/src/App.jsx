import { useCallback, useEffect, useMemo, useState } from 'react'
import { Loader2, LogOut, Shield, TrendingUp, Users } from 'lucide-react'
import { apiFetch, getApiBaseUrl } from './api'
import { hasSupabaseConfig, supabase } from './supabase'
import { Badge } from './components/ui/badge'
import { Button } from './components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './components/ui/card'
import { Input } from './components/ui/input'
import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs'

const formatCurrency = (value) => {
  const numeric = Number(value ?? 0)
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(Number.isFinite(numeric) ? numeric : 0)
}

const formatPercent = (value) => {
  const numeric = Number(value ?? 0)
  return `${numeric.toFixed(2)}%`
}

const normalizeRole = (user) => {
  const metadata = {
    ...(user?.user_metadata || {}),
    ...(user?.app_metadata || {}),
  }
  const role = String(metadata.role || '').trim().toUpperCase()
  if (role === 'ADMIN' || role === 'FUND_MANAGER' || role === 'CUSTOMER') {
    return role
  }
  return null
}

const getMetadataNumber = (user, keys) => {
  const metadata = {
    ...(user?.user_metadata || {}),
    ...(user?.app_metadata || {}),
  }
  for (const key of keys) {
    const raw = metadata[key]
    if (raw === null || raw === undefined || raw === '') {
      continue
    }
    const value = Number(raw)
    if (Number.isFinite(value)) {
      return value
    }
  }
  return null
}

function App() {
  const apiBaseUrl = getApiBaseUrl()
  const [session, setSession] = useState(null)
  const [authReady, setAuthReady] = useState(false)
  const [authError, setAuthError] = useState('')

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [authMode, setAuthMode] = useState('login')
  const [loggingIn, setLoggingIn] = useState(false)
  const [signingUp, setSigningUp] = useState(false)
  const [signUpName, setSignUpName] = useState('')
  const [signUpRole, setSignUpRole] = useState('CUSTOMER')
  const [signUpCustomerId, setSignUpCustomerId] = useState('')
  const [signUpFundManagerId, setSignUpFundManagerId] = useState('')
  const [authNotice, setAuthNotice] = useState('')

  const [role, setRole] = useState(null)
  const [customerId, setCustomerId] = useState(null)
  const [fundManagerId, setFundManagerId] = useState(null)

  const [loadingWorkspace, setLoadingWorkspace] = useState(false)
  const [workspaceError, setWorkspaceError] = useState('')

  const [customerProfile, setCustomerProfile] = useState(null)
  const [customerPortfolios, setCustomerPortfolios] = useState([])
  const [customerPortfolioId, setCustomerPortfolioId] = useState('')
  const [customerHoldings, setCustomerHoldings] = useState([])
  const [customerAnalytics, setCustomerAnalytics] = useState(null)

  const [managerCustomers, setManagerCustomers] = useState([])
  const [managerCustomerId, setManagerCustomerId] = useState('')
  const [managerPortfolios, setManagerPortfolios] = useState([])
  const [managerPortfolioId, setManagerPortfolioId] = useState('')
  const [managerHoldings, setManagerHoldings] = useState([])
  const [managerAnalytics, setManagerAnalytics] = useState(null)
  const [managerTab, setManagerTab] = useState('portfolio')

  const [customerForm, setCustomerForm] = useState({ name: '', email: '', phone: '' })
  const [purchaseForm, setPurchaseForm] = useState({ assetSymbol: '', quantity: '', price: '' })
  const [managerActionState, setManagerActionState] = useState('')

  const [adminTab, setAdminTab] = useState('overview')
  const [adminAdmins, setAdminAdmins] = useState([])
  const [adminFundManagers, setAdminFundManagers] = useState([])
  const [adminCustomers, setAdminCustomers] = useState([])
  const [adminAssets, setAdminAssets] = useState([])
  const [adminPortfolios, setAdminPortfolios] = useState([])
  const [fundManagerForm, setFundManagerForm] = useState({ name: '', email: '', phone: '' })
  const [assetForm, setAssetForm] = useState({ symbol: '', name: '', assetType: 'EQUITY', currentPrice: '' })
  const [adminActionState, setAdminActionState] = useState('')

  const user = session?.user || null

  const authContext = useMemo(
    () => ({
      token: session?.access_token,
      role,
      customerId,
      fundManagerId,
    }),
    [session, role, customerId, fundManagerId]
  )

  const apiJson = useCallback(
    async (path, options = {}, extraAuth = {}) => {
      const response = await apiFetch(path, options, { ...authContext, ...extraAuth })
      const text = await response.text()
      let payload = null
      if (text) {
        try {
          payload = JSON.parse(text)
        } catch {
          payload = text
        }
      }

      if (!response.ok) {
        const errorMessage = typeof payload === 'string' ? payload : payload?.message || 'Request failed'
        throw new Error(`${response.status} ${errorMessage}`)
      }
      return payload
    },
    [authContext]
  )

  const loadCustomerPortfolioDetails = useCallback(
    async (portfolioId) => {
      if (!portfolioId) {
        setCustomerHoldings([])
        setCustomerAnalytics(null)
        return
      }
      const [holdings, analytics] = await Promise.all([
        apiJson(`/portfolios/${portfolioId}/holdings`),
        apiJson(`/portfolios/${portfolioId}/analytics?benchmark=NIFTY`),
      ])
      setCustomerHoldings(Array.isArray(holdings) ? holdings : [])
      setCustomerAnalytics(analytics || null)
    },
    [apiJson]
  )

  const loadCustomerWorkspace = useCallback(async () => {
    if (!customerId) {
      throw new Error('Customer ID missing in Supabase metadata. Expected customer_id or customerId.')
    }

    const [profile, portfolios] = await Promise.all([
      apiJson(`/customers/${customerId}`),
      apiJson(`/customers/${customerId}/portfolios`),
    ])

    const list = Array.isArray(portfolios) ? portfolios : []
    setCustomerProfile(profile || null)
    setCustomerPortfolios(list)

    const firstPortfolioId = list[0]?.id ? String(list[0].id) : ''
    setCustomerPortfolioId(firstPortfolioId)

    await loadCustomerPortfolioDetails(firstPortfolioId)
  }, [apiJson, customerId, loadCustomerPortfolioDetails])

  const loadManagerPortfolioDetails = useCallback(
    async (portfolioId) => {
      if (!portfolioId) {
        setManagerHoldings([])
        setManagerAnalytics(null)
        return
      }
      const [holdings, analytics] = await Promise.all([
        apiJson(`/portfolios/${portfolioId}/holdings`),
        apiJson(`/portfolios/${portfolioId}/analytics?benchmark=NIFTY`),
      ])
      setManagerHoldings(Array.isArray(holdings) ? holdings : [])
      setManagerAnalytics(analytics || null)
    },
    [apiJson]
  )

  const loadManagerCustomerDetails = useCallback(
    async (nextCustomerId) => {
      if (!nextCustomerId) {
        setManagerPortfolios([])
        setManagerPortfolioId('')
        setManagerHoldings([])
        setManagerAnalytics(null)
        return
      }

      const portfolios = await apiJson(`/customers/${nextCustomerId}/portfolios`)
      const list = Array.isArray(portfolios) ? portfolios : []
      setManagerPortfolios(list)

      const firstPortfolioId = list[0]?.id ? String(list[0].id) : ''
      setManagerPortfolioId(firstPortfolioId)
      await loadManagerPortfolioDetails(firstPortfolioId)
    },
    [apiJson, loadManagerPortfolioDetails]
  )

  const loadFundManagerWorkspace = useCallback(async () => {
    if (!fundManagerId) {
      throw new Error('Fund manager ID missing in Supabase metadata. Expected fund_manager_id or fundManagerId.')
    }

    const customers = await apiJson(`/fund-managers/${fundManagerId}/customers`)
    const customerList = Array.isArray(customers) ? customers : []
    setManagerCustomers(customerList)

    const firstCustomerId = customerList[0]?.id ? String(customerList[0].id) : ''
    setManagerCustomerId(firstCustomerId)

    await loadManagerCustomerDetails(firstCustomerId)
  }, [apiJson, fundManagerId, loadManagerCustomerDetails])

  const loadAdminWorkspace = useCallback(async () => {
    const [admins, fundManagers, customers, assets, portfolios] = await Promise.all([
      apiJson('/admins'),
      apiJson('/fund-managers'),
      apiJson('/customers'),
      apiJson('/assets'),
      apiJson('/portfolios'),
    ])

    setAdminAdmins(Array.isArray(admins) ? admins : [])
    setAdminFundManagers(Array.isArray(fundManagers) ? fundManagers : [])
    setAdminCustomers(Array.isArray(customers) ? customers : [])
    setAdminAssets(Array.isArray(assets) ? assets : [])
    setAdminPortfolios(Array.isArray(portfolios) ? portfolios : [])
  }, [apiJson])

  const refreshWorkspace = useCallback(async () => {
    if (!session || !role) {
      return
    }

    setWorkspaceError('')
    setLoadingWorkspace(true)
    try {
      if (role === 'CUSTOMER') {
        await loadCustomerWorkspace()
      } else if (role === 'FUND_MANAGER') {
        await loadFundManagerWorkspace()
      } else if (role === 'ADMIN') {
        await loadAdminWorkspace()
      }
    } catch (error) {
      setWorkspaceError(error.message)
    } finally {
      setLoadingWorkspace(false)
    }
  }, [session, role, loadCustomerWorkspace, loadFundManagerWorkspace, loadAdminWorkspace])

  useEffect(() => {
    if (!hasSupabaseConfig || !supabase) {
      setAuthReady(true)
      setAuthError('Missing Supabase config. Set VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY.')
      return
    }

    supabase.auth.getSession().then(({ data, error }) => {
      if (error) {
        setAuthError(error.message)
      }
      const nextSession = data?.session || null
      setSession(nextSession)
      if (nextSession?.user) {
        const nextRole = normalizeRole(nextSession.user)
        setRole(nextRole)
        setCustomerId(getMetadataNumber(nextSession.user, ['customer_id', 'customerId']))
        setFundManagerId(getMetadataNumber(nextSession.user, ['fund_manager_id', 'fundManagerId']))
      } else {
        setRole(null)
        setCustomerId(null)
        setFundManagerId(null)
      }
      setAuthReady(true)
    })

    const { data: subscription } = supabase.auth.onAuthStateChange((_event, nextSession) => {
      setSession(nextSession)
      if (nextSession?.user) {
        const nextRole = normalizeRole(nextSession.user)
        setRole(nextRole)
        setCustomerId(getMetadataNumber(nextSession.user, ['customer_id', 'customerId']))
        setFundManagerId(getMetadataNumber(nextSession.user, ['fund_manager_id', 'fundManagerId']))
      } else {
        setRole(null)
        setCustomerId(null)
        setFundManagerId(null)
      }
    })

    return () => {
      subscription.subscription.unsubscribe()
    }
  }, [])

  useEffect(() => {
    if (!authReady) {
      return
    }
    refreshWorkspace()
  }, [authReady, refreshWorkspace])

  const handleLogin = async (event) => {
    event.preventDefault()
    setAuthError('')
    setAuthNotice('')
    if (!supabase) {
      setAuthError('Supabase client unavailable.')
      return
    }

    setLoggingIn(true)
    const { error } = await supabase.auth.signInWithPassword({ email, password })
    setLoggingIn(false)

    if (error) {
      setAuthError(error.message)
    }
  }

  const handleSignUp = async (event) => {
    event.preventDefault()
    setAuthError('')
    setAuthNotice('')

    if (!supabase) {
      setAuthError('Supabase client unavailable.')
      return
    }

    const trimmedName = signUpName.trim()
    if (!trimmedName) {
      setAuthError('Name is required.')
      return
    }

    if (signUpRole === 'CUSTOMER' && !signUpCustomerId.trim()) {
      setAuthError('Customer signup requires a customer ID provided by your fund manager/admin.')
      return
    }

    if (signUpRole === 'FUND_MANAGER' && !signUpFundManagerId.trim()) {
      setAuthError('Fund manager signup requires a fund manager ID provided by admin.')
      return
    }

    const metadata = {
      name: trimmedName,
      role: signUpRole,
    }

    if (signUpRole === 'CUSTOMER') {
      metadata.customer_id = Number(signUpCustomerId)
    }
    if (signUpRole === 'FUND_MANAGER') {
      metadata.fund_manager_id = Number(signUpFundManagerId)
    }

    setSigningUp(true)
    const { error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        data: metadata,
      },
    })
    setSigningUp(false)

    if (error) {
      setAuthError(error.message)
      return
    }

    setAuthNotice('Signup successful. If email confirmation is enabled, verify your email before logging in.')
    setAuthMode('login')
  }

  const handleLogout = async () => {
    if (supabase) {
      await supabase.auth.signOut()
    }
  }

  const handleManagerCustomerCreate = async (event) => {
    event.preventDefault()
    setManagerActionState('')

    try {
      await apiJson('/customers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: customerForm.name,
          email: customerForm.email,
          phone: customerForm.phone || null,
          fundManagerId,
        }),
      })
      setCustomerForm({ name: '', email: '', phone: '' })
      setManagerActionState('Customer created.')
      await loadFundManagerWorkspace()
    } catch (error) {
      setManagerActionState(error.message)
    }
  }

  const handleManagerPurchase = async (event) => {
    event.preventDefault()
    setManagerActionState('')

    if (!managerPortfolioId) {
      setManagerActionState('Select a portfolio first.')
      return
    }

    try {
      await apiJson(`/portfolios/${managerPortfolioId}/transactions/purchase`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          assetSymbol: purchaseForm.assetSymbol,
          quantity: Number(purchaseForm.quantity),
          price: Number(purchaseForm.price),
        }),
      })
      setPurchaseForm({ assetSymbol: '', quantity: '', price: '' })
      setManagerActionState('Holding updated via purchase transaction.')
      await loadManagerCustomerDetails(managerCustomerId)
    } catch (error) {
      setManagerActionState(error.message)
    }
  }

  const handleAdminFundManagerCreate = async (event) => {
    event.preventDefault()
    setAdminActionState('')

    try {
      await apiJson('/fund-managers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(fundManagerForm),
      })
      setFundManagerForm({ name: '', email: '', phone: '' })
      setAdminActionState('Fund manager created.')
      await loadAdminWorkspace()
    } catch (error) {
      setAdminActionState(error.message)
    }
  }

  const handleAdminAssetCreate = async (event) => {
    event.preventDefault()
    setAdminActionState('')

    try {
      await apiJson('/assets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...assetForm,
          currentPrice: Number(assetForm.currentPrice),
        }),
      })
      setAssetForm({ symbol: '', name: '', assetType: 'EQUITY', currentPrice: '' })
      setAdminActionState('Asset created.')
      await loadAdminWorkspace()
    } catch (error) {
      setAdminActionState(error.message)
    }
  }

  const activeCustomerPortfolio = customerPortfolios.find((item) => String(item.id) === customerPortfolioId)
  const activeManagerCustomer = managerCustomers.find((item) => String(item.id) === managerCustomerId)
  const activeManagerPortfolio = managerPortfolios.find((item) => String(item.id) === managerPortfolioId)

  if (!authReady) {
    return (
      <main className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-6 w-6 animate-spin" />
      </main>
    )
  }

  if (!session) {
    return (
      <main className="mx-auto flex min-h-screen w-full max-w-5xl items-center px-4 py-8">
        <section className="grid w-full gap-6 md:grid-cols-2">
          <Card className="bg-gradient-to-br from-sky-500 to-cyan-500 text-white">
            <CardHeader>
              <CardTitle className="font-display text-3xl">Portfolio Management Platform</CardTitle>
              <CardDescription className="text-white/90">
                Secure role-based workspace for customer analytics, fund manager operations, and admin controls.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <p>Customer: can only view own holdings and analytics.</p>
              <p>Fund Manager: can manage customers and alter holdings.</p>
              <p>Admin: full permissions, account created only via backend workflows.</p>
              <p className="pt-2 text-xs">API Base: {apiBaseUrl}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Authentication</CardTitle>
              <CardDescription>Login or sign up with Supabase. Admin accounts are backend-created only.</CardDescription>
            </CardHeader>
            <CardContent>
              <Tabs value={authMode} onValueChange={setAuthMode}>
                <TabsList className="mb-3 grid w-full grid-cols-2">
                  <TabsTrigger value="login">Login</TabsTrigger>
                  <TabsTrigger value="signup">Sign Up</TabsTrigger>
                </TabsList>

                <TabsContent value="login">
                  <form onSubmit={handleLogin} className="space-y-3">
                    <Input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <Input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                    <Button type="submit" className="w-full" disabled={loggingIn || !hasSupabaseConfig}>
                      {loggingIn ? 'Signing in...' : 'Sign In'}
                    </Button>
                  </form>
                </TabsContent>

                <TabsContent value="signup">
                  <form onSubmit={handleSignUp} className="space-y-3">
                    <Input placeholder="Full Name" value={signUpName} onChange={(e) => setSignUpName(e.target.value)} required />
                    <Input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    <Input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                    <div className="grid gap-2 sm:grid-cols-2">
                      <label className="text-sm">
                        <span className="mb-1 block text-muted-foreground">Account Type</span>
                        <select
                          className="h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                          value={signUpRole}
                          onChange={(e) => setSignUpRole(e.target.value)}
                        >
                          <option value="CUSTOMER">Customer</option>
                          <option value="FUND_MANAGER">Fund Manager</option>
                        </select>
                      </label>

                      {signUpRole === 'CUSTOMER' ? (
                        <Input
                          placeholder="Customer ID"
                          type="number"
                          min="1"
                          value={signUpCustomerId}
                          onChange={(e) => setSignUpCustomerId(e.target.value)}
                          required
                        />
                      ) : (
                        <Input
                          placeholder="Fund Manager ID"
                          type="number"
                          min="1"
                          value={signUpFundManagerId}
                          onChange={(e) => setSignUpFundManagerId(e.target.value)}
                          required
                        />
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Admin signup is intentionally disabled in UI. Admin accounts are provisioned only via backend workflows.
                    </p>
                    <Button type="submit" className="w-full" disabled={signingUp || !hasSupabaseConfig}>
                      {signingUp ? 'Creating account...' : 'Create Account'}
                    </Button>
                  </form>
                </TabsContent>
              </Tabs>
              {authError && <p className="mt-3 text-sm text-red-600">{authError}</p>}
              {authNotice && <p className="mt-2 text-sm text-emerald-700">{authNotice}</p>}
            </CardContent>
          </Card>
        </section>
      </main>
    )
  }

  if (!role) {
    return (
      <main className="mx-auto max-w-3xl px-4 py-10">
        <Card>
          <CardHeader>
            <CardTitle>Role metadata missing</CardTitle>
            <CardDescription>
              This user is authenticated but has no valid role metadata. Expected one of ADMIN, FUND_MANAGER, CUSTOMER.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="mb-4 text-sm text-muted-foreground">
              Add role and account ID mapping in Supabase user metadata/app metadata.
            </p>
            <Button onClick={handleLogout} variant="outline">Sign out</Button>
          </CardContent>
        </Card>
      </main>
    )
  }

  return (
    <main className="mx-auto w-full max-w-7xl px-4 py-6">
      <header className="mb-6 flex flex-wrap items-center justify-between gap-3 rounded-xl border bg-white/85 p-4 backdrop-blur">
        <div>
          <h1 className="font-display text-2xl">Portfolio Management Website</h1>
          <p className="text-sm text-muted-foreground">Signed in as {user?.email}</p>
        </div>
        <div className="flex items-center gap-2">
          <Badge>{role}</Badge>
          <Button variant="outline" onClick={refreshWorkspace} disabled={loadingWorkspace}>Refresh</Button>
          <Button variant="ghost" onClick={handleLogout} className="gap-2"><LogOut className="h-4 w-4" /> Logout</Button>
        </div>
      </header>

      {workspaceError && <p className="mb-4 rounded-md border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">{workspaceError}</p>}

      {loadingWorkspace ? (
        <section className="flex items-center gap-2 text-sm text-muted-foreground"><Loader2 className="h-4 w-4 animate-spin" /> Loading workspace...</section>
      ) : null}

      {role === 'CUSTOMER' && (
        <section className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2"><TrendingUp className="h-5 w-5" /> Customer Dashboard</CardTitle>
              <CardDescription>Only your portfolios and analytics are visible.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-2 text-sm sm:grid-cols-3">
              <div><span className="text-muted-foreground">Name:</span> {customerProfile?.name || 'N/A'}</div>
              <div><span className="text-muted-foreground">Email:</span> {customerProfile?.email || 'N/A'}</div>
              <div><span className="text-muted-foreground">Portfolios:</span> {customerPortfolios.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Your Portfolios</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                {customerPortfolios.map((portfolio) => (
                  <button
                    key={portfolio.id}
                    type="button"
                    onClick={async () => {
                      const selectedId = String(portfolio.id)
                      setCustomerPortfolioId(selectedId)
                      await loadCustomerPortfolioDetails(selectedId)
                    }}
                    className={`rounded-md border p-3 text-left ${
                      customerPortfolioId === String(portfolio.id) ? 'border-primary bg-primary/5' : 'border-border bg-white'
                    }`}
                  >
                    <p className="font-medium">{portfolio.portfolioName}</p>
                    <p className="text-xs text-muted-foreground">Investment: {formatCurrency(portfolio.totalInvestment)}</p>
                    <p className="text-xs text-muted-foreground">Current: {formatCurrency(portfolio.currentValue)}</p>
                  </button>
                ))}
              </div>

              {activeCustomerPortfolio && customerAnalytics && (
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                  <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Current Value</p><p className="font-semibold">{formatCurrency(customerAnalytics.currentValue)}</p></CardContent></Card>
                  <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Profit / Loss</p><p className="font-semibold">{formatCurrency(customerAnalytics.profitLoss)}</p></CardContent></Card>
                  <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Portfolio Return</p><p className="font-semibold">{formatPercent(customerAnalytics.portfolioPerformancePercentage)}</p></CardContent></Card>
                  <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Vs Benchmark</p><p className="font-semibold">{formatPercent(customerAnalytics.outperformancePercentage)}</p></CardContent></Card>
                </div>
              )}

              <div className="overflow-x-auto rounded-md border">
                <table className="w-full min-w-[700px] text-sm">
                  <thead className="bg-muted/50 text-left">
                    <tr>
                      <th className="px-3 py-2">Symbol</th>
                      <th className="px-3 py-2">Asset</th>
                      <th className="px-3 py-2">Type</th>
                      <th className="px-3 py-2">Qty</th>
                      <th className="px-3 py-2">Avg Buy</th>
                      <th className="px-3 py-2">Current Price</th>
                      <th className="px-3 py-2">Invested</th>
                      <th className="px-3 py-2">Current Value</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customerHoldings.map((holding) => (
                      <tr key={holding.id} className="border-t">
                        <td className="px-3 py-2 font-medium">{holding.assetSymbol}</td>
                        <td className="px-3 py-2">{holding.assetName}</td>
                        <td className="px-3 py-2">{holding.assetType}</td>
                        <td className="px-3 py-2">{holding.quantity}</td>
                        <td className="px-3 py-2">{formatCurrency(holding.averageBuyPrice)}</td>
                        <td className="px-3 py-2">{formatCurrency(holding.currentPrice)}</td>
                        <td className="px-3 py-2">{formatCurrency(holding.investedAmount)}</td>
                        <td className="px-3 py-2">{formatCurrency(holding.currentValue)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </section>
      )}

      {role === 'FUND_MANAGER' && (
        <section className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2"><Users className="h-5 w-5" /> Fund Manager Workspace</CardTitle>
              <CardDescription>Manage customers and alter holdings across customer portfolios.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-2 sm:grid-cols-3 text-sm">
              <div><span className="text-muted-foreground">Fund Manager ID:</span> {fundManagerId || 'N/A'}</div>
              <div><span className="text-muted-foreground">Customers:</span> {managerCustomers.length}</div>
              <div><span className="text-muted-foreground">Selected Customer:</span> {activeManagerCustomer?.name || 'None'}</div>
            </CardContent>
          </Card>

          <Tabs value={managerTab} onValueChange={setManagerTab}>
            <TabsList>
              <TabsTrigger value="portfolio">Portfolio Management</TabsTrigger>
              <TabsTrigger value="customers">Customer Onboarding</TabsTrigger>
            </TabsList>

            <TabsContent value="portfolio" className="space-y-4">
              <Card>
                <CardHeader><CardTitle>Customer Portfolios</CardTitle></CardHeader>
                <CardContent className="space-y-3">
                  <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                    {managerCustomers.map((customer) => (
                      <button
                        key={customer.id}
                        type="button"
                        onClick={async () => {
                          const id = String(customer.id)
                          setManagerCustomerId(id)
                          await loadManagerCustomerDetails(id)
                        }}
                        className={`rounded-md border p-3 text-left ${
                          managerCustomerId === String(customer.id) ? 'border-primary bg-primary/5' : 'border-border bg-white'
                        }`}
                      >
                        <p className="font-medium">{customer.name}</p>
                        <p className="text-xs text-muted-foreground">{customer.email}</p>
                      </button>
                    ))}
                  </div>

                  <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
                    {managerPortfolios.map((portfolio) => (
                      <button
                        key={portfolio.id}
                        type="button"
                        onClick={async () => {
                          const id = String(portfolio.id)
                          setManagerPortfolioId(id)
                          await loadManagerPortfolioDetails(id)
                        }}
                        className={`rounded-md border p-3 text-left ${
                          managerPortfolioId === String(portfolio.id) ? 'border-primary bg-primary/5' : 'border-border bg-white'
                        }`}
                      >
                        <p className="font-medium">{portfolio.portfolioName}</p>
                        <p className="text-xs text-muted-foreground">Investment: {formatCurrency(portfolio.totalInvestment)}</p>
                        <p className="text-xs text-muted-foreground">Current: {formatCurrency(portfolio.currentValue)}</p>
                      </button>
                    ))}
                  </div>

                  {activeManagerPortfolio && managerAnalytics && (
                    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                      <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Current Value</p><p className="font-semibold">{formatCurrency(managerAnalytics.currentValue)}</p></CardContent></Card>
                      <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Profit / Loss</p><p className="font-semibold">{formatCurrency(managerAnalytics.profitLoss)}</p></CardContent></Card>
                      <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Portfolio Return</p><p className="font-semibold">{formatPercent(managerAnalytics.portfolioPerformancePercentage)}</p></CardContent></Card>
                      <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Vs Benchmark</p><p className="font-semibold">{formatPercent(managerAnalytics.outperformancePercentage)}</p></CardContent></Card>
                    </div>
                  )}

                  <div className="overflow-x-auto rounded-md border">
                    <table className="w-full min-w-[700px] text-sm">
                      <thead className="bg-muted/50 text-left">
                        <tr>
                          <th className="px-3 py-2">Symbol</th>
                          <th className="px-3 py-2">Asset</th>
                          <th className="px-3 py-2">Qty</th>
                          <th className="px-3 py-2">Avg Buy</th>
                          <th className="px-3 py-2">Invested</th>
                          <th className="px-3 py-2">Current Value</th>
                        </tr>
                      </thead>
                      <tbody>
                        {managerHoldings.map((holding) => (
                          <tr key={holding.id} className="border-t">
                            <td className="px-3 py-2 font-medium">{holding.assetSymbol}</td>
                            <td className="px-3 py-2">{holding.assetName}</td>
                            <td className="px-3 py-2">{holding.quantity}</td>
                            <td className="px-3 py-2">{formatCurrency(holding.averageBuyPrice)}</td>
                            <td className="px-3 py-2">{formatCurrency(holding.investedAmount)}</td>
                            <td className="px-3 py-2">{formatCurrency(holding.currentValue)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <form onSubmit={handleManagerPurchase} className="grid gap-2 rounded-md border p-4 sm:grid-cols-4">
                    <Input placeholder="Asset Symbol" value={purchaseForm.assetSymbol} onChange={(e) => setPurchaseForm((prev) => ({ ...prev, assetSymbol: e.target.value.toUpperCase() }))} required />
                    <Input placeholder="Quantity" type="number" min="0.0001" step="0.0001" value={purchaseForm.quantity} onChange={(e) => setPurchaseForm((prev) => ({ ...prev, quantity: e.target.value }))} required />
                    <Input placeholder="Price" type="number" min="0.01" step="0.01" value={purchaseForm.price} onChange={(e) => setPurchaseForm((prev) => ({ ...prev, price: e.target.value }))} required />
                    <Button type="submit">Alter Holdings</Button>
                  </form>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="customers">
              <Card>
                <CardHeader>
                  <CardTitle>Add Customer</CardTitle>
                  <CardDescription>Creates a customer tied to your fund manager account.</CardDescription>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleManagerCustomerCreate} className="grid gap-2 sm:grid-cols-4">
                    <Input placeholder="Name" value={customerForm.name} onChange={(e) => setCustomerForm((prev) => ({ ...prev, name: e.target.value }))} required />
                    <Input placeholder="Email" type="email" value={customerForm.email} onChange={(e) => setCustomerForm((prev) => ({ ...prev, email: e.target.value }))} required />
                    <Input placeholder="Phone" value={customerForm.phone} onChange={(e) => setCustomerForm((prev) => ({ ...prev, phone: e.target.value }))} />
                    <Button type="submit">Create Customer</Button>
                  </form>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          {managerActionState && <p className="text-sm text-muted-foreground">{managerActionState}</p>}
        </section>
      )}

      {role === 'ADMIN' && (
        <section className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2"><Shield className="h-5 w-5" /> Admin Control Plane</CardTitle>
              <CardDescription>Full-system visibility and management. Admin creation remains backend-only.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-5">
              <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Admins</p><p className="font-semibold">{adminAdmins.length}</p></CardContent></Card>
              <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Fund Managers</p><p className="font-semibold">{adminFundManagers.length}</p></CardContent></Card>
              <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Customers</p><p className="font-semibold">{adminCustomers.length}</p></CardContent></Card>
              <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Assets</p><p className="font-semibold">{adminAssets.length}</p></CardContent></Card>
              <Card><CardContent className="pt-6"><p className="text-xs text-muted-foreground">Portfolios</p><p className="font-semibold">{adminPortfolios.length}</p></CardContent></Card>
            </CardContent>
          </Card>

          <Tabs value={adminTab} onValueChange={setAdminTab}>
            <TabsList>
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="create-fm">Create Fund Manager</TabsTrigger>
              <TabsTrigger value="create-asset">Create Asset</TabsTrigger>
            </TabsList>

            <TabsContent value="overview">
              <Card>
                <CardHeader><CardTitle>Recent Entities</CardTitle></CardHeader>
                <CardContent className="grid gap-4 lg:grid-cols-2">
                  <div>
                    <p className="mb-2 text-sm font-medium">Fund Managers</p>
                    <ul className="space-y-1 text-sm">
                      {adminFundManagers.slice(0, 6).map((item) => (
                        <li key={item.id} className="rounded border px-2 py-1">#{item.id} {item.name} ({item.email})</li>
                      ))}
                    </ul>
                  </div>
                  <div>
                    <p className="mb-2 text-sm font-medium">Assets</p>
                    <ul className="space-y-1 text-sm">
                      {adminAssets.slice(0, 6).map((item) => (
                        <li key={item.id} className="rounded border px-2 py-1">{item.symbol} - {item.name}</li>
                      ))}
                    </ul>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="create-fm">
              <Card>
                <CardHeader><CardTitle>Create Fund Manager</CardTitle></CardHeader>
                <CardContent>
                  <form onSubmit={handleAdminFundManagerCreate} className="grid gap-2 sm:grid-cols-4">
                    <Input placeholder="Name" value={fundManagerForm.name} onChange={(e) => setFundManagerForm((prev) => ({ ...prev, name: e.target.value }))} required />
                    <Input placeholder="Email" type="email" value={fundManagerForm.email} onChange={(e) => setFundManagerForm((prev) => ({ ...prev, email: e.target.value }))} required />
                    <Input placeholder="Phone" value={fundManagerForm.phone} onChange={(e) => setFundManagerForm((prev) => ({ ...prev, phone: e.target.value }))} />
                    <Button type="submit">Create</Button>
                  </form>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="create-asset">
              <Card>
                <CardHeader><CardTitle>Create Asset</CardTitle></CardHeader>
                <CardContent>
                  <form onSubmit={handleAdminAssetCreate} className="grid gap-2 sm:grid-cols-5">
                    <Input placeholder="Symbol" value={assetForm.symbol} onChange={(e) => setAssetForm((prev) => ({ ...prev, symbol: e.target.value.toUpperCase() }))} required />
                    <Input placeholder="Name" value={assetForm.name} onChange={(e) => setAssetForm((prev) => ({ ...prev, name: e.target.value }))} required />
                    <Input placeholder="Type" value={assetForm.assetType} onChange={(e) => setAssetForm((prev) => ({ ...prev, assetType: e.target.value.toUpperCase() }))} required />
                    <Input placeholder="Current Price" type="number" step="0.01" min="0.01" value={assetForm.currentPrice} onChange={(e) => setAssetForm((prev) => ({ ...prev, currentPrice: e.target.value }))} required />
                    <Button type="submit">Create</Button>
                  </form>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>

          {adminActionState && <p className="text-sm text-muted-foreground">{adminActionState}</p>}
        </section>
      )}
    </main>
  )
}

export default App
