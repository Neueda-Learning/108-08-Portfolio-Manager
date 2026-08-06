// Falls back to the current page's hostname so the app works out of the box on
// localhost, an EC2 private/public IP, or any future host without needing a
// VITE_API_BASE_URL override in .env.
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}:8083`;

export const TOKEN_STORAGE_KEY = "portfoliom_token";

export const API_ENDPOINTS = {
  portfolios: "/api/portfolios",
  portfolioById: (portfolioId) => `/api/portfolios/${portfolioId}`,
  portfolioPerformance: (portfolioId) => `/api/portfolios/${portfolioId}/performance`,
  holdings: (portfolioId) => `/api/portfolios/${portfolioId}/holdings`,
  holdingById: (portfolioId, holdingId) => `/api/portfolios/${portfolioId}/holdings/${holdingId}`,

  login: "/api/auth/login",
  me: "/api/auth/me",

  allHoldings: "/api/holdings",
  allHoldingsRefresh: "/api/holdings?refresh=true",
  holdingByIdFlat: (id) => `/api/holdings/${id}`,
  holdingsHistory: (range) => `/api/holdings/history?range=${encodeURIComponent(range)}`,
  importCsv: "/api/holdings/import/csv",
  importImage: "/api/holdings/import/image",
  sampleCsv: "/api/holdings/import/csv/sample",
  exportCsv: "/api/holdings/export/csv",
  exportPdf: "/api/holdings/export/pdf",

  news: "/api/news",
  insightsSummary: "/api/insights/summary",

  managerCustomers: "/api/manager/customers",
  managerCustomerById: (id) => `/api/manager/customers/${id}`,
  managerCustomerHoldings: (id) => `/api/manager/customers/${id}/holdings`,
  managerCustomerHoldingById: (id, holdingId) => `/api/manager/customers/${id}/holdings/${holdingId}`,
  managerCustomerHistory: (id, range) =>
    `/api/manager/customers/${id}/holdings/history?range=${encodeURIComponent(range)}`,
};
