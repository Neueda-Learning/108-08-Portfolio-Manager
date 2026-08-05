import { API_ENDPOINTS } from "../config/api";
import { apiClient } from "./apiClient";

export const managerService = {
  getCustomers: () => apiClient(API_ENDPOINTS.managerCustomers),

  getCustomer: (id) => apiClient(API_ENDPOINTS.managerCustomerById(id)),

  createCustomer: (payload) =>
    apiClient(API_ENDPOINTS.managerCustomers, { method: "POST", body: JSON.stringify(payload) }),

  removeCustomer: (id) => apiClient(API_ENDPOINTS.managerCustomerById(id), { method: "DELETE" }),

  getCustomerHoldings: (id) => apiClient(API_ENDPOINTS.managerCustomerHoldings(id)),

  addCustomerHolding: (id, payload) =>
    apiClient(API_ENDPOINTS.managerCustomerHoldings(id), { method: "POST", body: JSON.stringify(payload) }),

  updateCustomerHolding: (id, holdingId, payload) =>
    apiClient(API_ENDPOINTS.managerCustomerHoldingById(id, holdingId), {
      method: "PUT",
      body: JSON.stringify(payload),
    }),

  removeCustomerHolding: (id, holdingId) =>
    apiClient(API_ENDPOINTS.managerCustomerHoldingById(id, holdingId), { method: "DELETE" }),

  getCustomerHistory: (id, range) => apiClient(API_ENDPOINTS.managerCustomerHistory(id, range)),
};
