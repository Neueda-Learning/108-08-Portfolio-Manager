import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { authService } from "../services/authService";
import { TOKEN_STORAGE_KEY } from "../config/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [username, setUsername] = useState(null);
  const [name, setName] = useState(null);
  const [email, setEmail] = useState(null);
  const [role, setRole] = useState(null);
  const [managerUsername, setManagerUsername] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }
    authService
      .me()
      .then((data) => {
        setUsername(data.username);
        setName(data.name);
        setEmail(data.email);
        setRole(data.role);
        setManagerUsername(data.managerUsername);
      })
      .catch(() => {
        localStorage.removeItem(TOKEN_STORAGE_KEY);
        setToken(null);
      })
      .finally(() => setLoading(false));
  }, [token]);

  const applySession = useCallback((data) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, data.token);
    setToken(data.token);
    setUsername(data.username);
    setRole(data.role);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUsername(null);
    setName(null);
    setEmail(null);
    setRole(null);
    setManagerUsername(null);
  }, []);

  const value = {
    token,
    username,
    name,
    email,
    role,
    isFundManager: role === "FUND_MANAGER",
    managerUsername,
    loading,
    isAuthenticated: !!token,
    applySession,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
