import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function ManagerRoute({ children }) {
  const { isAuthenticated, isFundManager, loading } = useAuth();

  if (loading) {
    return <div className="container">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isFundManager) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ManagerRoute;
