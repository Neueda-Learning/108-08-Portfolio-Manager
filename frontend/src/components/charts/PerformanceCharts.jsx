import { useTheme } from "../../context/ThemeContext";
import { Bar } from "react-chartjs-2";
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Tooltip, Legend } from "chart.js";
import Card from "../common/Card";
import { formatMoney } from "../../utils/formatters";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

function PerformanceCharts({ holdings }) {
  const { isDark } = useTheme();
  if (!holdings || holdings.length === 0) {
    return null;
  }

  const legendColor = isDark ? "#c7d0df" : "#52514e";
  const chartText = isDark ? "#c7d0df" : "#898781";
  const chartGrid = isDark ? "#2a3342" : "#e7e6e1";
  const investedBar = isDark ? "#5c6477" : "#d6d4cd";
  const valueBar = isDark ? "#6ea8ff" : "#2a78d6";

  const data = {
    labels: holdings.map((holding) => holding.ticker),
    datasets: [
      {
        label: "Amount Invested",
        data: holdings.map((holding) => Number(holding.costBasis || 0)),
        backgroundColor: investedBar,
        borderRadius: 4,
        maxBarThickness: 28,
      },
      {
        label: "Current Value",
        data: holdings.map((holding) => Number(holding.currentValue || 0)),
        backgroundColor: valueBar,
        borderRadius: 4,
        maxBarThickness: 28,
      },
    ],
  };

  const options = {
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: { color: legendColor, font: { size: 12 } },
      },
      tooltip: {
        callbacks: {
          label: (context) => `${context.dataset.label}: ${formatMoney(context.parsed.y)}`,
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: chartText, font: { size: 11 } },
      },
      y: {
        grid: { color: chartGrid },
        ticks: { color: chartText, font: { size: 11 }, callback: (value) => formatMoney(value) },
      },
    },
  };

  return (
    <Card>
      <h3 className="card-title">Investment vs Current Value</h3>
      <div className="chart-canvas chart-canvas-wide">
        <Bar data={data} options={options} />
      </div>
    </Card>
  );
}

export default PerformanceCharts;
