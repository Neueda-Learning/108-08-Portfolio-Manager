import { useTheme } from "../../context/ThemeContext";

function ThemeToggle({ className = "", compact = false }) {
  const { theme, toggleTheme, isDark } = useTheme();

  return (
    <button
      type="button"
      className={`theme-toggle ${compact ? "compact" : ""} ${className}`.trim()}
      onClick={toggleTheme}
      aria-label={`Switch to ${isDark ? "light" : "dark"} mode`}
      aria-pressed={isDark}
      title={`${isDark ? "Light" : "Dark"} mode`}
    >
      <span className="theme-toggle-track" aria-hidden="true">
        <span className="theme-toggle-thumb">
          <span className="theme-toggle-icon">{theme === "dark" ? "☾" : "☼"}</span>
        </span>
      </span>
      <span className="theme-toggle-label">{isDark ? "Dark" : "Light"} mode</span>
    </button>
  );
}

export default ThemeToggle;
