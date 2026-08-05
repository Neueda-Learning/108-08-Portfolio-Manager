import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function Sidebar({ collapsed, onToggleCollapse, mobileOpen, onCloseMobile }) {
  const { username, managerUsername, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <>
      {mobileOpen && <div className="sidebar-backdrop" onClick={onCloseMobile} />}
      <aside className={`sidebar ${collapsed ? "collapsed" : ""} ${mobileOpen ? "mobile-open" : ""}`}>
        <div className="sidebar-header">
          <div className="brand">{collapsed ? "P" : "PortfolioM"}</div>
          <button className="sidebar-toggle" onClick={onToggleCollapse} title="Collapse sidebar" aria-label="Collapse sidebar">
            {collapsed ? "»" : "«"}
          </button>
          <button className="sidebar-close" onClick={onCloseMobile} aria-label="Close menu">
            &#10005;
          </button>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/" end onClick={onCloseMobile} title="Dashboard">
            <span className="nav-icon">&#9638;</span>
            <span className="nav-label">Dashboard</span>
          </NavLink>
          <NavLink to="/holdings" onClick={onCloseMobile} title="Holdings">
            <span className="nav-icon">&#9636;</span>
            <span className="nav-label">Holdings</span>
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          {managerUsername && !collapsed && (
            <p className="meta-line sidebar-message" title={`Managed by ${managerUsername}`}>
              Fund Manager: {managerUsername}
            </p>
          )}
          <div className="sidebar-user" title={username || ""}>
            <span className="nav-label">{username}</span>
          </div>
          <button className="link-btn full-width" onClick={handleLogout} title="Log out">
            <span className="nav-label">Log out</span>
            <span className="nav-icon-only">&#9099;</span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
