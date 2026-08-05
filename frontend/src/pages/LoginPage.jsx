import { useState } from "react";
import { Navigate } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import { useAuth } from "../context/AuthContext";
import { authService } from "../services/authService";

function LoginPage() {
  const { isAuthenticated, isFundManager, applySession } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to={isFundManager ? "/manager" : "/"} replace />;
  }

  async function handlePasswordLogin(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const session = await authService.login(username.trim(), password);
      applySession(session);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-screen">
      <Card className="login-card">
        <div className="brand login-brand">PortfolioM</div>
        <p className="subtitle">Sign in to view your portfolio</p>

        <form onSubmit={handlePasswordLogin}>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="input-with-action">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="input-action-btn"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                aria-pressed={showPassword}
                tabIndex={-1}
              >
                {showPassword ? "\u{1F648}" : "\u{1F441}"}
              </button>
            </div>
          </div>

          {error && <p className="login-error">{error}</p>}

          <div className="actions form-actions">
            <Button type="submit" loading={submitting} className="full-width">
              Sign in
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default LoginPage;
