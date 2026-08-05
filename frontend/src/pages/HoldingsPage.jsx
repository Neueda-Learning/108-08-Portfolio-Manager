import { useEffect, useRef, useState } from "react";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import DataTable from "../components/common/DataTable";
import Modal from "../components/common/Modal";
import ConfirmDialog from "../components/common/ConfirmDialog";
import Skeleton from "../components/common/Skeleton";
import HoldingForm from "../components/holdings/HoldingForm";
import { holdingsService } from "../services/holdingsService";
import { useToast } from "../context/ToastContext";
import { formatMoney, formatPercent, formatSignedMoney, formatTime } from "../utils/formatters";

const EMPTY_FORM = {
  ticker: "",
  type: "STOCK",
  name: "",
  quantity: "",
  purchasePrice: "",
  purchaseDate: "",
};

function HoldingsPage() {
  const toast = useToast();
  const [performance, setPerformance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingHolding, setEditingHolding] = useState(null);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [importMessage, setImportMessage] = useState("");
  const [csvBusy, setCsvBusy] = useState(false);
  const [imageBusy, setImageBusy] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [exporting, setExporting] = useState("");
  const [refreshing, setRefreshing] = useState(false);

  const csvInputRef = useRef(null);
  const imageInputRef = useRef(null);

  async function loadHoldings(force = false) {
    const data = await holdingsService.getAll({ force });
    setPerformance(data);
  }

  useEffect(() => {
    loadHoldings()
      .catch((error) => toast.error(error.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleRefresh() {
    setRefreshing(true);
    try {
      await loadHoldings(true);
      toast.success("Live prices refreshed");
    } catch (error) {
      toast.error(error.message);
    } finally {
      setRefreshing(false);
    }
  }

  function openAddModal() {
    setEditingHolding(null);
    setFormData(EMPTY_FORM);
    setShowAddModal(true);
  }

  function openEditModal(holding) {
    setEditingHolding(holding);
    setFormData({
      ticker: holding.ticker,
      type: holding.type,
      name: holding.name,
      quantity: holding.quantity,
      purchasePrice: holding.purchasePrice,
      purchaseDate: holding.purchaseDate || "",
    });
    setShowAddModal(true);
  }

  function handleFormChange(field, value) {
    setFormData((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const payload = {
      ticker: formData.ticker.trim().toUpperCase(),
      type: formData.type,
      name: formData.name.trim(),
      quantity: Number(formData.quantity),
      purchasePrice: Number(formData.purchasePrice),
      purchaseDate: formData.purchaseDate || null,
    };

    setSubmitting(true);
    try {
      if (editingHolding) {
        await holdingsService.update(editingHolding.id, payload);
        toast.success(`${payload.ticker} updated`);
      } else {
        await holdingsService.add(payload);
        toast.success(`${payload.ticker} added`);
      }
      setShowAddModal(false);
      await loadHoldings();
    } catch (error) {
      toast.error(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleConfirmDelete() {
    if (!pendingDelete) return;
    setDeleting(true);
    try {
      await holdingsService.remove(pendingDelete.id);
      toast.success(`${pendingDelete.ticker} deleted`);
      setPendingDelete(null);
      await loadHoldings();
    } catch (error) {
      toast.error(error.message);
    } finally {
      setDeleting(false);
    }
  }

  async function handleCsvSelected(event) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    setCsvBusy(true);
    setImportMessage("");
    try {
      const result = await holdingsService.importCsv(file);
      const message =
        `Imported ${result.imported} holding(s).` +
        (result.errors?.length ? ` ${result.errors.length} row(s) skipped.` : "");
      setImportMessage(message);
      toast.success(message);
      await loadHoldings();
    } catch (error) {
      setImportMessage(error.message);
      toast.error(error.message);
    } finally {
      setCsvBusy(false);
    }
  }

  async function handleImageSelected(event) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) return;
    setImageBusy(true);
    setImportMessage("");
    try {
      const result = await holdingsService.importImage(file);
      const message =
        `Imported ${result.imported} holding(s) from the image.` +
        (result.errors?.length ? ` ${result.errors.length} row(s) skipped.` : "");
      setImportMessage(message);
      toast.success(message);
      await loadHoldings();
    } catch (error) {
      setImportMessage(error.message);
      toast.error(error.message);
    } finally {
      setImageBusy(false);
    }
  }

  async function handleExport(format) {
    setExporting(format);
    try {
      if (format === "csv") {
        await holdingsService.exportCsv();
      } else {
        await holdingsService.exportPdf();
      }
      toast.success(`${format.toUpperCase()} export ready`);
    } catch (error) {
      toast.error(error.message);
    } finally {
      setExporting("");
    }
  }

  const holdings = performance?.holdings || [];

  const columns = [
    {
      key: "index",
      title: "#",
      render: (_row, index) => index + 1,
    },
    { key: "ticker", title: "Ticker" },
    { key: "name", title: "Name" },
    {
      key: "purchaseDate",
      title: "Purchase Date",
      render: (holding) => holding.purchaseDate || "-",
    },
    {
      key: "purchasePrice",
      title: "Purchase Value",
      render: (holding) => formatMoney(Number(holding.purchasePrice) * Number(holding.quantity)),
    },
    {
      key: "currentValue",
      title: "Current Value",
      render: (holding) => formatMoney(holding.currentValue),
    },
    {
      key: "gainLossPercent",
      title: "P&L",
      render: (holding) => {
        const up = Number(holding.gainLoss) >= 0;
        return (
          <span className={up ? "pnl-up" : "pnl-down"}>
            <span className="pnl-arrow">{up ? "▲" : "▼"}</span> {formatSignedMoney(holding.gainLoss)} (
            {formatPercent(holding.gainLossPercent)})
          </span>
        );
      },
    },
    {
      key: "actions",
      title: "",
      render: (holding) => (
        <div className="actions">
          <button className="icon-btn" title="Edit" onClick={() => openEditModal(holding)}>
            ✎
          </button>
          <button className="icon-btn danger" title="Delete" onClick={() => setPendingDelete(holding)}>
            ✕
          </button>
        </div>
      ),
    },
  ];

  if (loading) {
    return (
      <div>
        <section className="hero">
          <div>
            <Skeleton width="200px" height="26px" />
            <div className="section-gap-sm">
              <Skeleton width="300px" height="14px" />
            </div>
          </div>
        </section>
        <Card>
          {[0, 1, 2].map((key) => (
            <div key={key} className="section-gap-sm">
              <Skeleton height="16px" />
            </div>
          ))}
        </Card>
      </div>
    );
  }

  return (
    <div>
      <section className="hero">
        <div>
          <h1>Holdings Report</h1>
          <p className="subtitle">All holdings, purchase and current value, and profit and loss</p>
          {performance?.pricesAsOf && (
            <p className="meta-line">Prices updated {formatTime(performance.pricesAsOf)}</p>
          )}
        </div>
        <div className="actions">
          <Button variant="ghost" onClick={handleRefresh} loading={refreshing}>
            Refresh Prices
          </Button>
          <Button variant="ghost" onClick={() => handleExport("csv")} loading={exporting === "csv"}>
            Export CSV
          </Button>
          <Button variant="ghost" onClick={() => handleExport("pdf")} loading={exporting === "pdf"}>
            Export PDF
          </Button>
        </div>
      </section>

      <section className="section-gap-lg">
        <div className="section-heading">
          <h2>Add Holdings</h2>
        </div>
        <Card>
          <div className="import-options">
            <Button onClick={openAddModal}>Add Manually</Button>
            <Button variant="ghost" onClick={() => csvInputRef.current?.click()} loading={csvBusy}>
              Import from CSV
            </Button>
            <Button variant="ghost" onClick={() => imageInputRef.current?.click()} loading={imageBusy}>
              Import from Image
            </Button>
            <button className="link-btn" onClick={() => holdingsService.downloadSampleCsv()}>
              Download Sample CSV
            </button>
          </div>
          <input ref={csvInputRef} type="file" accept=".csv,text/csv" hidden onChange={handleCsvSelected} />
          <input ref={imageInputRef} type="file" accept="image/*" hidden onChange={handleImageSelected} />
          {importMessage && <p className="meta-line">{importMessage}</p>}
          <p className="meta-line">
            Importing a ticker you already hold updates its quantity and average price instead of adding a duplicate row.
          </p>
        </Card>
      </section>

      <section className="section-gap-lg">
        <div className="section-heading">
          <h2>All Holdings</h2>
          <span className="meta-line section-heading-count">
            {holdings.length} holding{holdings.length === 1 ? "" : "s"}
          </span>
        </div>
        <Card padded={false}>
          <DataTable columns={columns} rows={holdings} emptyText="No holdings yet - add one above to get started" />
        </Card>
      </section>

      <Modal
        isOpen={showAddModal}
        title={editingHolding ? "Edit Holding" : "Add Holding"}
        onClose={() => setShowAddModal(false)}
      >
        <HoldingForm
          formData={formData}
          onChange={handleFormChange}
          onSubmit={handleSubmit}
          onCancel={() => setShowAddModal(false)}
          submitLabel={editingHolding ? "Save Changes" : "Add Holding"}
          submitting={submitting}
        />
      </Modal>

      <ConfirmDialog
        isOpen={!!pendingDelete}
        title="Delete holding"
        message={pendingDelete ? `Delete ${pendingDelete.ticker}? This cannot be undone.` : ""}
        confirmLabel="Delete"
        danger
        loading={deleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </div>
  );
}

export default HoldingsPage;
