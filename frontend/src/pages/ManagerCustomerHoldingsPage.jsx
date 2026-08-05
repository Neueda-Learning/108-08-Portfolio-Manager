import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import DataTable from "../components/common/DataTable";
import Modal from "../components/common/Modal";
import ConfirmDialog from "../components/common/ConfirmDialog";
import Skeleton from "../components/common/Skeleton";
import HoldingForm from "../components/holdings/HoldingForm";
import { managerService } from "../services/managerService";
import { useToast } from "../context/ToastContext";
import { formatMoney, formatPercent, formatSignedMoney } from "../utils/formatters";

const EMPTY_FORM = {
  ticker: "",
  type: "STOCK",
  name: "",
  quantity: "",
  purchasePrice: "",
  purchaseDate: "",
};

function ManagerCustomerHoldingsPage() {
  const { customerId } = useParams();
  const toast = useToast();
  const [performance, setPerformance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingHolding, setEditingHolding] = useState(null);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadHoldings = useCallback(async () => {
    const data = await managerService.getCustomerHoldings(customerId);
    setPerformance(data);
  }, [customerId]);

  useEffect(() => {
    setLoading(true);
    loadHoldings()
      .catch((error) => toast.error(error.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loadHoldings]);

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
        await managerService.updateCustomerHolding(customerId, editingHolding.id, payload);
        toast.success(`${payload.ticker} updated`);
      } else {
        await managerService.addCustomerHolding(customerId, payload);
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
      await managerService.removeCustomerHolding(customerId, pendingDelete.id);
      toast.success(`${pendingDelete.ticker} deleted`);
      setPendingDelete(null);
      await loadHoldings();
    } catch (error) {
      toast.error(error.message);
    } finally {
      setDeleting(false);
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
          <h1>Customer Holdings</h1>
          <p className="subtitle">Manage this customer's holdings, purchase and current value, and profit and loss</p>
        </div>
        <Button onClick={openAddModal}>Add Holding</Button>
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

export default ManagerCustomerHoldingsPage;
