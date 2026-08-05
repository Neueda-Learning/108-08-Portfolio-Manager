import ThemeToggle from "./ThemeToggle";

function MobileHeader({ onOpenSidebar }) {
  return (
    <header className="mobile-header">
      <div className="mobile-header-left">
        <button className="hamburger" onClick={onOpenSidebar} aria-label="Open menu">
          &#9776;
        </button>
        <div className="brand">PortfolioM</div>
      </div>
      <ThemeToggle compact className="mobile-theme-toggle" />
    </header>
  );
}

export default MobileHeader;
