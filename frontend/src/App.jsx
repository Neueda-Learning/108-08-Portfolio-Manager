import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Sidebar from "./components/common/Sidebar";
import ManagerSidebar from "./components/common/ManagerSidebar";
import MobileHeader from "./components/common/MobileHeader";
import ProtectedRoute from "./components/common/ProtectedRoute";
import ManagerRoute from "./components/common/ManagerRoute";
import DashboardPage from "./pages/DashboardPage";
import HoldingsPage from "./pages/HoldingsPage";
import LoginPage from "./pages/LoginPage";
import ManagerCustomersPage from "./pages/ManagerCustomersPage";
import ManagerCustomerDashboardPage from "./pages/ManagerCustomerDashboardPage";
import ManagerCustomerHoldingsPage from "./pages/ManagerCustomerHoldingsPage";
import { useAuth } from "./context/AuthContext";

const SIDEBAR_STORAGE_KEY = "portfoliom_sidebar_collapsed";

function App() {
  const { isAuthenticated, isFundManager } = useAuth();
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem(SIDEBAR_STORAGE_KEY) === "true");
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(collapsed));
  }, [collapsed]);

  return (
    <div className={`app-shell ${isAuthenticated ? "with-sidebar" : ""}`}>
      {isAuthenticated && (
        <>
          <MobileHeader onOpenSidebar={() => setMobileOpen(true)} />
          {isFundManager ? (
            <ManagerSidebar
              collapsed={collapsed}
              onToggleCollapse={() => setCollapsed((prev) => !prev)}
              mobileOpen={mobileOpen}
              onCloseMobile={() => setMobileOpen(false)}
            />
          ) : (
            <Sidebar
              collapsed={collapsed}
              onToggleCollapse={() => setCollapsed((prev) => !prev)}
              mobileOpen={mobileOpen}
              onCloseMobile={() => setMobileOpen(false)}
            />
          )}
        </>
      )}
      <main className={`app-content ${isAuthenticated && collapsed ? "sidebar-collapsed" : ""}`}>
        <div className="container">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/holdings"
              element={
                <ProtectedRoute>
                  <HoldingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/manager"
              element={
                <ManagerRoute>
                  <ManagerCustomersPage />
                </ManagerRoute>
              }
            />
            <Route
              path="/manager/customers/:customerId"
              element={
                <ManagerRoute>
                  <ManagerCustomerDashboardPage />
                </ManagerRoute>
              }
            />
            <Route
              path="/manager/customers/:customerId/holdings"
              element={
                <ManagerRoute>
                  <ManagerCustomerHoldingsPage />
                </ManagerRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

export default App;
