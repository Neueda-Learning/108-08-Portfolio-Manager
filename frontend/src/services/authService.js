import { API_ENDPOINTS } from "../config/api";
import { apiClient } from "./apiClient";

export const authService = {
  login: (username, password) =>
    apiClient(API_ENDPOINTS.login, { method: "POST", body: JSON.stringify({ username, password }) }),
  me: () => apiClient(API_ENDPOINTS.me),
};
