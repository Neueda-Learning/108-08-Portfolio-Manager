import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import DataTable from "../components/common/DataTable";
import Modal from "../components/common/Modal";
import ConfirmDialog from "../components/common/ConfirmDialog";
import Skeleton from "../components/common/Skeleton";
import { managerService } from "../services/managerService";
import { useToast } from "../context/ToastContext";
import { formatMoney, formatPercent, formatSignedMoney } from "../utils/formatters";

const EMPTY_FORM = { username: "", password: "", name: "", email: "" };

function ManagerCustomersPage() {
  const toast = useToast();
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  async function loadCustomers() {
    const data = await managerService.getCustomers();
    setCustomers(data);
  }

  useEffect(() => {
    loadCustomers()
      .catch((error) => toast.error(error.message))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openAddModal() {
    setFormData(EMPTY_FORM);
    setShowAddModal(true);
  }

  function handleFormChange(field, value) {
    setFormData((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    try {
      await managerService.createCustomer({
        username: formData.username.trim(),
        password: formData.password,
        name: formData.name.trim(),
        email: formData.email.trim(),
      });
      toast.success(`Customer '${formData.username.trim()}' created`);
      setShowAddModal(false);
      await loadCustomers();
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
      await managerService.removeCustomer(pendingDelete.id);
      toast.success(`${pendingDelete.username} removed`);
      setPendingDelete(null);
      await loadCustomers();
    } catch (error) {
      toast.error(error.message);
    } finally {
      setDeleting(false);
    }
  }

  const columns = [
    {
      key: "index",
      title: "#",
      render: (_row, index) => index + 1,
    },
    {
      key: "username",
      title: "Customer",
      render: (customer) => (
        <Link className="link-btn" to={`/manager/customers/${customer.id}`}>
          {customer.username}
        </Link>
      ),
    },
    {
      key: "name",
      title: "Name",
    },
    {
      key: "email",
      title: "Email",
    },
    {
      key: "holdingCount",
      title: "Holdings",
    },
    {
      key: "totalCurrentValue",
      title: "Current Value",
      render: (customer) => formatMoney(customer.totalCurrentValue),
    },
    {
      key: "gainLossPercent",
      title: "P&L",
      render: (customer) => {
        const up = Number(customer.totalGainLoss) >= 0;
        return (
          <span className={up ? "pnl-up" : "pnl-down"}>
            <span className="pnl-arrow">{up ? "▲" : "▼"}</span> {formatSignedMoney(customer.totalGainLoss)} (
            {formatPercent(customer.gainLossPercent)})
          </span>
        );
      },
    },
    {
      key: "actions",
      title: "",
      render: (customer) => (
        <div className="actions">
          <Link className="icon-btn" title="View" to={`/manager/customers/${customer.id}`}>
            &#128065;
          </Link>
          <button className="icon-btn danger" title="Remove" onClick={() => setPendingDelete(customer)}>
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
          <h1>Customers</h1>
          <p className="subtitle">Manage customer accounts and view their portfolios</p>
        </div>
        <Button onClick={openAddModal}>Add Customer</Button>
      </section>

      <section className="section-gap-lg">
        <div className="section-heading">
          <h2>All Customers</h2>
          <span className="meta-line section-heading-count">
            {customers.length} customer{customers.length === 1 ? "" : "s"}
          </span>
        </div>
        <Card padded={false}>
          <DataTable columns={columns} rows={customers} emptyText="No customers yet - add one above to get started" />
        </Card>
      </section>

      <Modal isOpen={showAddModal} title="Add Customer" onClose={() => setShowAddModal(false)}>
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div>
              <label htmlFor="customerUsername">Username</label>
              <input
                id="customerUsername"
                value={formData.username}
                onChange={(event) => handleFormChange("username", event.target.value)}
                autoComplete="off"
                required
              />
            </div>
            <div>
              <label htmlFor="customerPassword">Password</label>
              <input
                id="customerPassword"
                type="password"
                value={formData.password}
                onChange={(event) => handleFormChange("password", event.target.value)}
                autoComplete="new-password"
                minLength={8}
                required
              />
            </div>
            <div>
              <label htmlFor="customerName">Name</label>
              <input
                id="customerName"
                value={formData.name}
                onChange={(event) => handleFormChange("name", event.target.value)}
                autoComplete="off"
                required
              />
            </div>
            <div>
              <label htmlFor="customerEmail">Email</label>
              <input
                id="customerEmail"
                type="email"
                value={formData.email}
                onChange={(event) => handleFormChange("email", event.target.value)}
                autoComplete="off"
                required
              />
            </div>
          </div>
          <div className="actions form-actions">
            <Button variant="ghost" type="button" onClick={() => setShowAddModal(false)} disabled={submitting}>
              Cancel
            </Button>
            <Button type="submit" loading={submitting}>
              Add Customer
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={!!pendingDelete}
        title="Remove customer"
        message={
          pendingDelete
            ? `Remove ${pendingDelete.username}? This deletes all of their portfolios and holdings and cannot be undone.`
            : ""
        }
        confirmLabel="Remove"
        danger
        loading={deleting}
        onConfirm={handleConfirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </div>
  );
}

export default ManagerCustomersPage;
