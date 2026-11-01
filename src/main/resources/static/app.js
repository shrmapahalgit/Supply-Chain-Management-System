const state = {
  products: [],
  suppliers: [],
  orders: [],
  view: "dashboard",
  editing: null,
  modalType: null,
  user: null
};

const $ = (id) => document.getElementById(id);
const money = (n) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(Number(n || 0));
const dateText = (v) => {
  if (!v) return "—";
  const d = new Date(v);
  return Number.isNaN(d.getTime()) ? String(v).slice(0, 10) : d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
};
const esc = (s) => String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

let cachedCsrfToken = null;
async function getCsrfToken() {
  const fromCookie = getCookie("XSRF-TOKEN");
  if (fromCookie) {
    cachedCsrfToken = decodeURIComponent(fromCookie);
    return cachedCsrfToken;
  }
  try {
    const res = await fetch("/api/auth/csrf");
    if (res.ok) {
      const data = await res.json();
      cachedCsrfToken = data.token;
      return cachedCsrfToken;
    }
  } catch (e) {
    console.warn("Could not fetch CSRF token:", e);
  }
  return "";
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  const method = (options.method || "GET").toUpperCase();

  // Attach CSRF token on state-changing requests
  if (["POST", "PUT", "DELETE", "PATCH"].includes(method)) {
    const token = await getCsrfToken();
    if (token) {
      headers["X-XSRF-TOKEN"] = token;
    }
  }

  const res = await fetch(path, { ...options, headers });

  // Handle unauthorized responses by redirecting to login
  if (res.status === 401) {
    window.location.href = "/login.html";
    throw new Error("Session expired. Please log in again.");
  }

  const raw = await res.text();
  let data = null;
  try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }

  if (!res.ok) {
    throw new Error(data?.error || data?.message || `Request failed (${res.status})`);
  }
  return data;
}

function toast(message) {
  const t = $("toast");
  if (!t) return;
  t.textContent = message;
  t.classList.add("show");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => t.classList.remove("show"), 3000);
}

// Session verification
async function checkAuth() {
  try {
    const res = await fetch("/api/auth/me");
    if (!res.ok) throw new Error("Auth check failed");
    const data = await res.json();
    if (!data.authenticated || !data.user) {
      window.location.href = "/login.html";
      return false;
    }
    state.user = data.user;
    updateUserProfileWidget(data.user);
    return true;
  } catch {
    window.location.href = "/login.html";
    return false;
  }
}

function updateUserProfileWidget(user) {
  const nameEl = $("user-display-name");
  const roleEl = $("user-role-badge");
  const avatarEl = $("user-avatar");

  if (nameEl) nameEl.textContent = user.fullName || user.email;
  if (roleEl) roleEl.textContent = (user.role || "ROLE_USER").replace("ROLE_", "");

  if (avatarEl) {
    const initials = (user.fullName || user.email)
      .split(" ")
      .filter(Boolean)
      .map(part => part[0])
      .slice(0, 2)
      .join("")
      .toUpperCase();
    avatarEl.textContent = initials || "OP";
  }
}

window.logout = async function () {
  if (!confirm("Are you sure you want to sign out?")) return;
  try {
    await api("/api/auth/logout", { method: "POST" });
  } catch (e) {
    console.warn("Logout error:", e);
  } finally {
    window.location.href = "/login.html";
  }
};

function showView(view) {
  state.view = view;
  document.querySelectorAll(".view").forEach(el => el.classList.add("hidden"));
  const viewEl = $(`${view}-view`);
  if (viewEl) viewEl.classList.remove("hidden");
  document.querySelectorAll(".nav-item").forEach(el => el.classList.toggle("active", el.dataset.view === view));

  const names = {
    dashboard: ["Supply chain overview", "OVERVIEW"],
    products: ["Inventory catalogue", "INVENTORY"],
    suppliers: ["Supplier directory", "SUPPLIERS"],
    orders: ["Purchase orders", "PROCUREMENT"]
  };
  if (names[view]) {
    $("page-title").textContent = names[view][0];
    $("crumb").textContent = names[view][1];
  }
  if (view === "products") renderProducts();
  if (view === "suppliers") renderSuppliers();
  if (view === "orders") renderOrders();
}

document.querySelectorAll(".nav-item").forEach(b => b.addEventListener("click", () => showView(b.dataset.view)));

async function refresh() {
  try {
    const [dashboard, products, suppliers, orders] = await Promise.all([
      api("/api/dashboard"),
      api("/api/products"),
      api("/api/suppliers"),
      api("/api/orders")
    ]);
    state.products = products;
    state.suppliers = suppliers;
    state.orders = orders;

    $("stat-products").textContent = dashboard.products;
    $("stat-suppliers").textContent = dashboard.suppliers;
    $("stat-orders").textContent = dashboard.orders;
    $("stat-low").textContent = dashboard.lowStock;
    $("nav-low").textContent = dashboard.lowStock;

    renderDashboard();
    renderProducts();
    renderSuppliers();
    renderOrders();
  } catch (e) {
    toast("Could not load data: " + e.message);
  }
}

function renderDashboard() {
  const low = state.products.filter(p => p.quantity <= p.reorderLevel);
  $("watchlist").innerHTML = low.length
    ? low.slice(0, 4).map(p => `
      <div class="watch-row">
        <div class="product-thumb">${iconFor(p.category)}</div>
        <div class="watch-info">
          <b>${esc(p.name)}</b>
          <small>${esc(p.sku)} · Reorder at ${p.reorderLevel} units</small>
        </div>
        <span class="stock-pill low">${p.quantity} left</span>
        <button class="reorder-action-btn" onclick="quickReorder(${p.id}, ${p.supplierId || 'null'})" title="Order more">
          ＋ Reorder
        </button>
      </div>`).join("")
    : `<div class="empty">All catalogue items are currently above their reorder thresholds.</div>`;

  $("recent-orders").innerHTML = state.orders.length
    ? state.orders.slice(0, 4).map(orderRow).join("")
    : `<tr><td colspan="5" class="empty">No purchase orders registered yet.</td></tr>`;
}

function iconFor(cat) {
  return ({
    Vegetables: "🥬",
    Fruits: "🍎",
    Dairy: "🥛",
    Grains: "🌾",
    Meat: "🥩",
    Bakery: "🥖"
  })[cat] || "📦";
}

function statusClass(s) {
  return String(s || "pending").toLowerCase();
}

function orderRow(o) {
  return `<tr>
    <td class="order-code">${esc(o.orderCode)}</td>
    <td>${esc(o.supplier)}</td>
    <td>${dateText(o.orderDate)}</td>
    <td><span class="status-pill ${statusClass(o.status)}">${esc(String(o.status).replace("_", " "))}</span></td>
    <td><b>${money(o.totalAmount)}</b></td>
  </tr>`;
}

function renderProducts() {
  const q = ($("product-search")?.value || "").toLowerCase().trim();
  const categoryFilter = ($("product-category-filter")?.value || "").trim();

  const list = state.products.filter(p => {
    const matchesQuery = !q || [p.name, p.sku, p.category].some(x => String(x || "").toLowerCase().includes(q));
    const matchesCategory = !categoryFilter || String(p.category).toLowerCase() === categoryFilter.toLowerCase();
    return matchesQuery && matchesCategory;
  });

  if ($("product-count")) $("product-count").textContent = `${list.length} items`;

  $("products-table").innerHTML = list.length
    ? list.map(p => `
      <tr>
        <td>
          <div class="product-name">
            <div class="product-thumb">${iconFor(p.category)}</div>
            <div>
              <b>${esc(p.name)}</b>
              <small>${p.supplierId ? `Supplier #${p.supplierId}` : "No supplier assigned"}</small>
            </div>
          </div>
        </td>
        <td><code>${esc(p.sku)}</code></td>
        <td>${esc(p.category)}</td>
        <td><span class="stock-pill ${p.quantity <= p.reorderLevel ? "low" : "ok"}">${p.quantity} units</span></td>
        <td><b>${money(p.unitPrice)}</b></td>
        <td>${dateText(p.expiryDate)}</td>
        <td>
          <button class="table-action" onclick="editProduct(${p.id})">Edit</button>
          <button class="table-action danger" onclick="deleteProduct(${p.id})">Delete</button>
        </td>
      </tr>`).join("")
    : `<tr><td colspan="7" class="empty">No matching products found.</td></tr>`;
}

function renderSuppliers() {
  const q = ($("supplier-search")?.value || "").toLowerCase().trim();
  const list = state.suppliers.filter(s => {
    return !q || [s.companyName, s.contactName, s.email, s.phone, s.city].some(x => String(x || "").toLowerCase().includes(q));
  });

  if ($("supplier-count")) $("supplier-count").textContent = `${list.length} suppliers`;

  $("suppliers-table").innerHTML = list.length
    ? list.map(s => `
      <tr>
        <td>
          <div class="product-name">
            <div class="product-thumb">♧</div>
            <div>
              <b>${esc(s.companyName)}</b>
              <small>${esc(s.getRoleLabel || "Supply partner")}</small>
            </div>
          </div>
        </td>
        <td>${esc(s.contactName)}</td>
        <td>${esc(s.email)}</td>
        <td>${esc(s.phone)}</td>
        <td>${esc(s.city)}</td>
        <td><span class="status-pill ${s.status === "ACTIVE" ? "delivered" : "cancelled"}">${esc(s.status)}</span></td>
        <td>
          <button class="table-action" onclick="editSupplier(${s.id})">Edit</button>
          <button class="table-action danger" onclick="deleteSupplier(${s.id})">Delete</button>
        </td>
      </tr>`).join("")
    : `<tr><td colspan="7" class="empty">No matching suppliers found.</td></tr>`;
}

function renderOrders() {
  const q = ($("order-search")?.value || "").toLowerCase().trim();
  const statusFilter = ($("order-status-filter")?.value || "").trim().toUpperCase();

  const list = state.orders.filter(o => {
    const matchesQuery = !q || [o.orderCode, o.supplier, o.notes].some(x => String(x || "").toLowerCase().includes(q));
    const matchesStatus = !statusFilter || String(o.status).toUpperCase() === statusFilter;
    return matchesQuery && matchesStatus;
  });

  if ($("order-count")) $("order-count").textContent = `${list.length} orders`;

  $("orders-table").innerHTML = list.length
    ? list.map(o => `
      <tr>
        <td class="order-code">${esc(o.orderCode)}</td>
        <td>${esc(o.supplier)}</td>
        <td>${dateText(o.orderDate)}</td>
        <td><span class="status-pill ${statusClass(o.status)}">${esc(String(o.status).replace("_", " "))}</span></td>
        <td><b>${money(o.totalAmount)}</b></td>
        <td>
          <select aria-label="Update status" class="status-select" onchange="updateOrderStatus(${o.id}, this.value)">
            ${["PENDING", "APPROVED", "IN_TRANSIT", "DELIVERED", "CANCELLED"].map(s => `
              <option value="${s}" ${s === o.status ? "selected" : ""}>${s}</option>
            `).join("")}
          </select>
        </td>
      </tr>`).join("")
    : `<tr><td colspan="6" class="empty">No purchase orders found.</td></tr>`;
}

function field(name, label, type = "text", value = "", opts = null, required = true, full = false) {
  let input;
  if (opts) {
    input = `<select name="${name}" ${required ? "required" : ""}>
      ${opts.map(o => `<option value="${esc(o.value)}" ${String(o.value) === String(value) ? "selected" : ""}>${esc(o.label)}</option>`).join("")}
    </select>`;
  } else {
    input = `<input name="${name}" type="${type}" value="${esc(value ?? "")}" ${required ? "required" : ""} ${type === "number" ? 'min="0" step="any"' : ""}>`;
  }
  return `<div class="field ${full ? "full" : ""}"><label>${label}</label>${input}</div>`;
}

function openModal(title, fields, type, editing = null) {
  state.modalType = type;
  state.editing = editing;
  $("modal-title").textContent = title;
  $("modal-fields").innerHTML = fields;
  $("modal-backdrop").classList.remove("hidden");
}

function closeModal() {
  $("modal-backdrop").classList.add("hidden");
  $("record-form").onsubmit = null;
  state.editing = null;
}

function openProductModal(p = null) {
  const v = p || {};
  const supplierOpts = [{ value: "", label: "No supplier assigned" }, ...state.suppliers.map(s => ({ value: s.id, label: s.companyName }))];
  openModal(p ? "Edit product" : "Add inventory product", [
    field("sku", "SKU", "text", v.sku || ""),
    field("name", "Product name", "text", v.name || ""),
    field("category", "Category", "text", v.category || "Vegetables"),
    field("quantity", "Quantity in stock", "number", v.quantity ?? 0),
    field("reorderLevel", "Reorder level", "number", v.reorderLevel ?? 10),
    field("unitPrice", "Unit price (₹)", "number", v.unitPrice ?? 0),
    field("expiryDate", "Expiry date", "date", v.expiryDate || "", null, false),
    field("supplierId", "Supplier", "text", v.supplierId || "", supplierOpts, false, true)
  ].join(""), "product", p);
}

function openSupplierModal(s = null) {
  const v = s || {};
  openModal(s ? "Edit supplier" : "Add supplier", [
    field("companyName", "Company name", "text", v.companyName || ""),
    field("contactName", "Contact person", "text", v.contactName || ""),
    field("email", "Email", "email", v.email || ""),
    field("phone", "Phone", "text", v.phone || ""),
    field("city", "City", "text", v.city || ""),
    field("status", "Status", "text", v.status || "ACTIVE", [{ value: "ACTIVE", label: "Active" }, { value: "INACTIVE", label: "Inactive" }])
  ].join(""), "supplier", s);
}

function openOrderModal(initialProductId = null, initialSupplierId = null) {
  if (!state.suppliers.length || !state.products.length) {
    toast("Add at least one supplier and product first.");
    return;
  }
  const defaultSupplier = initialSupplierId || state.suppliers[0].id;
  const defaultProduct = initialProductId || state.products[0].id;

  openModal("Create purchase order", [
    field("supplierId", "Supplier", "text", defaultSupplier, state.suppliers.map(s => ({ value: s.id, label: s.companyName }))),
    field("productId", "Product", "text", defaultProduct, state.products.map(p => ({ value: p.id, label: `${p.name} · ${money(p.unitPrice)}` }))),
    field("quantity", "Order quantity", "number", 10),
    field("notes", "Order notes", "text", "Restock replenishment order", null, false, true)
  ].join(""), "order");
}

window.quickReorder = function (productId, supplierId) {
  openOrderModal(productId, supplierId);
};

// CSV Export Utilities
function downloadCsv(filename, headers, rows) {
  const escapeCsv = (val) => `"${String(val ?? "").replace(/"/g, '""')}"`;
  const csvContent = [
    headers.map(escapeCsv).join(","),
    ...rows.map(row => row.map(escapeCsv).join(","))
  ].join("\r\n");

  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.setAttribute("href", url);
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  toast(`Exported ${filename}`);
}

window.exportProductsCsv = function () {
  if (!state.products.length) return toast("No products to export.");
  const headers = ["ID", "SKU", "Product Name", "Category", "Quantity", "Reorder Level", "Unit Price", "Expiry Date", "Supplier ID"];
  const rows = state.products.map(p => [
    p.id, p.sku, p.name, p.category, p.quantity, p.reorderLevel, p.unitPrice, p.expiryDate || "", p.supplierId || ""
  ]);
  downloadCsv("foodflow_inventory_catalogue.csv", headers, rows);
};

window.exportSuppliersCsv = function () {
  if (!state.suppliers.length) return toast("No suppliers to export.");
  const headers = ["ID", "Company Name", "Contact Name", "Email", "Phone", "City", "Status"];
  const rows = state.suppliers.map(s => [
    s.id, s.companyName, s.contactName, s.email, s.phone, s.city, s.status
  ]);
  downloadCsv("foodflow_suppliers_directory.csv", headers, rows);
};

window.exportOrdersCsv = function () {
  if (!state.orders.length) return toast("No orders to export.");
  const headers = ["ID", "Order Code", "Supplier", "Date", "Status", "Total Amount", "Notes"];
  const rows = state.orders.map(o => [
    o.id, o.orderCode, o.supplier, o.orderDate, o.status, o.totalAmount, o.notes || ""
  ]);
  downloadCsv("foodflow_purchase_orders.csv", headers, rows);
};

// Expose globals for HTML triggers
window.openProductModal = openProductModal;
window.openSupplierModal = openSupplierModal;
window.openOrderModal = openOrderModal;
window.closeModal = closeModal;
window.showView = showView;
window.editProduct = (id) => openProductModal(state.products.find(p => p.id === id));
window.editSupplier = (id) => openSupplierModal(state.suppliers.find(s => s.id === id));

$("record-form").addEventListener("submit", async e => {
  e.preventDefault();
  const form = new FormData(e.currentTarget);
  const d = Object.fromEntries(form.entries());
  try {
    if (state.modalType === "product") {
      d.quantity = Number(d.quantity);
      d.reorderLevel = Number(d.reorderLevel);
      d.unitPrice = Number(d.unitPrice);
      d.supplierId = d.supplierId ? Number(d.supplierId) : null;
      d.expiryDate = d.expiryDate || null;
      await api(state.editing ? `/api/products/${state.editing.id}` : "/api/products", {
        method: state.editing ? "PUT" : "POST",
        body: JSON.stringify(d)
      });
    } else if (state.modalType === "supplier") {
      await api(state.editing ? `/api/suppliers/${state.editing.id}` : "/api/suppliers", {
        method: state.editing ? "PUT" : "POST",
        body: JSON.stringify(d)
      });
    } else {
      d.supplierId = Number(d.supplierId);
      d.productId = Number(d.productId);
      d.quantity = Number(d.quantity);
      await api("/api/orders", { method: "POST", body: JSON.stringify(d) });
    }
    closeModal();
    await refresh();
    toast("Record saved successfully.");
  } catch (err) {
    toast(err.message);
  }
});

window.deleteProduct = async id => {
  if (!confirm("Delete this product? Products used in orders cannot be deleted.")) return;
  try {
    await api(`/api/products/${id}`, { method: "DELETE" });
    await refresh();
    toast("Product deleted.");
  } catch (e) {
    toast(e.message);
  }
};

window.deleteSupplier = async id => {
  if (!confirm("Delete this supplier? Suppliers linked to products or orders cannot be deleted.")) return;
  try {
    await api(`/api/suppliers/${id}`, { method: "DELETE" });
    await refresh();
    toast("Supplier deleted.");
  } catch (e) {
    toast(e.message);
  }
};

window.updateOrderStatus = async (id, status) => {
  try {
    await api(`/api/orders/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    await refresh();
    toast("Order status updated.");
  } catch (e) {
    toast(e.message);
  }
};

// Initial startup: check session then refresh data
(async function init() {
  const authed = await checkAuth();
  if (authed) {
    await refresh();
  }
})();
