export function formatMoney(value) {
  const numericValue = Number(value || 0);
  return `Rs ${numericValue.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

export function formatPercent(value) {
  const numericValue = Number(value || 0);
  return `${numericValue >= 0 ? "+" : ""}${numericValue.toFixed(2)}%`;
}

export function formatSignedMoney(value) {
  const numericValue = Number(value || 0);
  return `${numericValue >= 0 ? "+" : ""}${formatMoney(numericValue)}`;
}

export function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}
