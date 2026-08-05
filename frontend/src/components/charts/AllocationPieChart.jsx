import { useMemo, useState } from "react";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { Pie } from "react-chartjs-2";
import { useTheme } from "../../context/ThemeContext";
import Card from "../common/Card";
import { formatMoney } from "../../utils/formatters";

ChartJS.register(ArcElement, Tooltip, Legend);

const COLORS = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300", "#4a3aa7", "#e34948"];

function AllocationPieChart({ holdings }) {
  const [mode, setMode] = useState("value");
  const { isDark } = useTheme();
  const legendColor = isDark ? "#c7d0df" : "#52514e";
  const borderColor = isDark ? "#111827" : "#ffffff";

  const data = useMemo(() => {
    const labels = holdings.map((holding) => holding.ticker);
    const values = holdings.map((holding) =>
      mode === "value" ? Number(holding.currentValue || 0) : Number(holding.quantity || 0)
    );
    return {
      labels,
      datasets: [
        {
          data: values,
          backgroundColor: labels.map((_, index) => COLORS[index % COLORS.length]),
          borderColor,
          borderWidth: 2,
        },
      ],
    };
  }, [holdings, mode]);

  const options = {
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "right",
        labels: { color: legendColor, font: { size: 11 }, boxWidth: 10, padding: 10 },
      },
      tooltip: {
        callbacks: {
          label: (context) =>
            mode === "value"
              ? `${context.label}: ${formatMoney(context.parsed)}`
              : `${context.label}: ${context.parsed}`,
        },
      },
    },
  };

  return (
    <Card>
      <div className="chart-header">
        <h3 className="card-title">Allocation</h3>
        <div className="filter-chips">
          <button type="button" className={`chip ${mode === "value" ? "active" : ""}`} onClick={() => setMode("value")}>
            By Value
          </button>
          <button
            type="button"
            className={`chip ${mode === "quantity" ? "active" : ""}`}
            onClick={() => setMode("quantity")}
          >
            By Quantity
          </button>
        </div>
      </div>
      {holdings.length === 0 ? (
        <p className="subtitle">No holdings yet</p>
      ) : (
        <div className="chart-canvas">
          <Pie data={data} options={options} />
        </div>
      )}
    </Card>
  );
}

export default AllocationPieChart;
