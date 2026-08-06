import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import StatCard from "../components/common/StatCard";
import Skeleton from "../components/common/Skeleton";
import PerformanceCharts from "../components/charts/PerformanceCharts";
import TrendChart from "../components/charts/TrendChart";
import AllocationPieChart from "../components/charts/AllocationPieChart";
import { managerService } from "../services/managerService";
import { useToast } from "../context/ToastContext";
import { formatMoney, formatPercent, formatSignedMoney, formatTime } from "../utils/formatters";

const RANGES = [
  { key: "1d", label: "1D" },
  { key: "1w", label: "1W" },
  { key: "1m", label: "1M" },
  { key: "all", label: "All" },
];

function ManagerCustomerDashboardPage() {
  const { customerId } = useParams();
  const toast = useToast();
  const [customer, setCustomer] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [history, setHistory] = useState([]);
  const [range, setRange] = useState("1m");
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadCustomer = useCallback(async () => {
    const data = await managerService.getCustomer(customerId);
    setCustomer(data);
  }, [customerId]);

  const loadPerformance = useCallback(async (force = false) => {
    const data = await managerService.getCustomerHoldings(customerId, { force });
    setPerformance(data);
  }, [customerId]);

  const loadHistory = useCallback(
    async (selectedRange) => {
      const points = await managerService.getCustomerHistory(customerId, selectedRange);
      setHistory(points);
    },
    [customerId]
  );

  useEffect(() => {
    setLoading(true);
    Promise.all([loadCustomer(), loadPerformance()])
      .catch((error) => toast.error(error.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadCustomer, loadPerformance]);

  useEffect(() => {
    loadHistory(range).catch(() => setHistory([]));
  }, [range, loadHistory]);

  async function handleRefresh() {
    setRefreshing(true);
    try {
      await loadPerformance(true);
      await loadHistory(range);
      toast.success("Live prices refreshed");
    } catch (error) {
      toast.error(error.message);
    } finally {
      setRefreshing(false);
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
          <h1>Customer Dashboard</h1>
          <p className="subtitle">
            {customer ? `${customer.name} · ${customer.username} · ${customer.email}` : "Live overview of this customer's holdings and market value"}
          </p>
          {performance?.pricesAsOf && <p className="meta-line">Prices updated {formatTime(performance.pricesAsOf)}</p>}
        </div>
        <div className="actions">
          <Button variant="ghost" onClick={handleRefresh} loading={refreshing}>
            Refresh Prices
          </Button>
          <Link className="button ghost" to={`/manager/customers/${customerId}/holdings`}>
            Manage Holdings
          </Link>
        </div>
      </section>

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
    </div>
  );
}

export default ManagerCustomerDashboardPage;
