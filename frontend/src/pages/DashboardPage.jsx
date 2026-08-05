import { useCallback, useEffect, useMemo, useState } from "react";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import StatCard from "../components/common/StatCard";
import Skeleton from "../components/common/Skeleton";
import PerformanceCharts from "../components/charts/PerformanceCharts";
import TrendChart from "../components/charts/TrendChart";
import AllocationPieChart from "../components/charts/AllocationPieChart";
import NewsList from "../components/news/NewsList";
import { holdingsService } from "../services/holdingsService";
import { newsService } from "../services/newsService";
import { insightsService } from "../services/insightsService";
import { useToast } from "../context/ToastContext";
import { formatMoney, formatPercent, formatSignedMoney } from "../utils/formatters";

const RANGES = [
  { key: "1d", label: "1D" },
  { key: "1w", label: "1W" },
  { key: "1m", label: "1M" },
  { key: "all", label: "All" },
];

function DashboardPage() {
  const toast = useToast();
  const [performance, setPerformance] = useState(null);
  const [history, setHistory] = useState([]);
  const [range, setRange] = useState("1m");
  const [news, setNews] = useState([]);
  const [summary, setSummary] = useState("");
  const [summaryError, setSummaryError] = useState("");
  const [summaryLoading, setSummaryLoading] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadPerformance = useCallback(async () => {
    const data = await holdingsService.getAll();
    setPerformance(data);
  }, []);

  const loadHistory = useCallback(async (selectedRange) => {
    const points = await holdingsService.getHistory(selectedRange);
    setHistory(points);
  }, []);

  const loadNews = useCallback(async () => {
    const articles = await newsService.getNews();
    setNews(articles);
  }, []);

  useEffect(() => {
    Promise.all([loadPerformance(), loadNews()])
      .catch((error) => toast.error(error.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadPerformance, loadNews]);

  useEffect(() => {
    loadHistory(range).catch(() => setHistory([]));
  }, [range, loadHistory]);

  async function handleSummary() {
    setSummaryLoading(true);
    setSummaryError("");
    setSummary("");
    try {
      const data = await insightsService.getSummary();
      setSummary(data.summary);
    } catch (error) {
      setSummaryError(error.message);
    } finally {
      setSummaryLoading(false);
    }
  }

  const stats = useMemo(() => {
    if (!performance) return null;
    const gainUp = Number(performance.totalGainLoss) >= 0;
    const returnUp = Number(performance.gainLossPercent) >= 0;
    return [
      { label: "Amount Invested", value: formatMoney(performance.totalCostBasis) },
      { label: "Current Value", value: formatMoney(performance.totalCurrentValue) },
      {
        label: "Gain / Loss",
        value: (
          <span className={`stat-pill ${gainUp ? "pill-up" : "pill-down"}`}>
            <span className="pnl-arrow">{gainUp ? "▲" : "▼"}</span>
            {formatSignedMoney(performance.totalGainLoss)}
          </span>
        ),
      },
      {
        label: "Return",
        value: (
          <span className={`stat-pill ${returnUp ? "pill-up" : "pill-down"}`}>
            <span className="pnl-arrow">{returnUp ? "▲" : "▼"}</span>
            {formatPercent(performance.gainLossPercent)}
          </span>
        ),
      },
    ];
  }, [performance]);

  if (loading) {
    return (
      <div>
        <section className="hero">
          <div>
            <Skeleton width="160px" height="24px" />
            <div className="section-gap-sm">
              <Skeleton width="260px" height="13px" />
            </div>
          </div>
        </section>
        <div className="grid stat-grid">
          {[0, 1, 2, 3].map((key) => (
            <Card key={key} className="stat-card">
              <Skeleton width="60%" height="11px" />
              <div className="section-gap-sm">
                <Skeleton width="80%" height="24px" />
              </div>
            </Card>
          ))}
        </div>
        <div className="grid chart-grid section-gap">
          <Card>
            <Skeleton height="240px" />
          </Card>
          <Card>
            <Skeleton height="240px" />
          </Card>
        </div>
      </div>
    );
  }

  const holdings = performance?.holdings || [];

  return (
    <div>
      <section className="hero">
        <div>
          <h1>Dashboard</h1>
          <p className="subtitle">Live overview of your holdings and market value</p>
        </div>
        <Button onClick={handleSummary} loading={summaryLoading}>
          Summary
        </Button>
      </section>

      {(summary || summaryError) && (
        <div className={`summary-banner ${summaryError ? "summary-error" : ""}`}>{summaryError || summary}</div>
      )}

      {stats && (
        <div className="grid stat-grid">
          {stats.map((stat) => (
            <StatCard key={stat.label} label={stat.label} value={stat.value} />
          ))}
        </div>
      )}

      <section className="section-gap-lg">
        <div className="section-heading">
          <h2>Performance</h2>
          <div className="filter-chips">
            {RANGES.map((option) => (
              <button
                key={option.key}
                type="button"
                className={`chip ${range === option.key ? "active" : ""}`}
                onClick={() => setRange(option.key)}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        <div className="grid chart-grid">
          <TrendChart points={history} range={range} />
          <AllocationPieChart holdings={holdings} />
        </div>

        <div className="section-gap">
          <PerformanceCharts holdings={holdings} />
        </div>
      </section>

      <section className="section-gap-lg">
        <div className="section-heading">
          <h2>Market News</h2>
        </div>
        <NewsList articles={news} />
      </section>
    </div>
  );
}

export default DashboardPage;
