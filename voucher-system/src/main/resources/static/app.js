const POLL_INTERVAL_MS = 2000;

const slotsEl = document.getElementById('slots');
const activeListEl = document.getElementById('active-list');
const toastEl = document.getElementById('toast');

async function api(path, options) {
    const res = await fetch(path, options);
    if (!res.ok) {
        const body = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(body.message || 'Request failed');
    }
    if (res.status === 204) return null;
    return res.json();
}

function showToast(message, isError) {
    toastEl.textContent = message;
    toastEl.classList.remove('hidden');
    toastEl.style.borderColor = isError ? 'var(--expired)' : 'var(--border)';
    clearTimeout(showToast._t);
    showToast._t = setTimeout(() => toastEl.classList.add('hidden'), 3000);
}

function formatRemaining(seconds) {
    if (seconds == null) return '';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    const parts = [];
    if (h > 0) parts.push(String(h).padStart(2, '0'));
    parts.push(String(m).padStart(2, '0'));
    parts.push(String(s).padStart(2, '0'));
    return parts.join(':');
}

function statusLabel(voucher) {
    if (voucher.status === 'ACTIVE') {
        return `Active - ${formatRemaining(voucher.secondsRemaining)} remaining`;
    }
    return voucher.status.charAt(0) + voucher.status.slice(1).toLowerCase();
}

async function generateVoucher(durationKey, button) {
    button.disabled = true;
    try {
        await api(`/api/vouchers/generate?duration=${durationKey}`, { method: 'POST' });
        showToast(`Generated a new ${durationKey.replaceAll('_', ' ').toLowerCase()} code`);
        await refresh();
    } catch (err) {
        showToast(err.message, true);
    } finally {
        button.disabled = false;
    }
}

async function simulateConnect(code) {
    const clientIdentifier = window.prompt('Simulated client identifier (device name/MAC), optional:', '') || undefined;
    try {
        await api(`/api/vouchers/${encodeURIComponent(code)}/connect`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ clientIdentifier })
        });
        showToast(`Client connected with code ${code}`);
        await refresh();
    } catch (err) {
        showToast(err.message, true);
    }
}

async function forceDisconnect(code) {
    if (!window.confirm(`Force-disconnect client on code ${code}?`)) return;
    try {
        await api(`/api/vouchers/${encodeURIComponent(code)}/disconnect`, { method: 'POST' });
        showToast(`Disconnected ${code}`);
        await refresh();
    } catch (err) {
        showToast(err.message, true);
    }
}

function renderActive(vouchers) {
    if (!vouchers.length) {
        activeListEl.innerHTML = '<p class="empty">No clients currently connected.</p>';
        return;
    }
    activeListEl.innerHTML = vouchers.map(v => `
        <div class="active-card">
            <div class="code">${v.code}</div>
            <div class="meta">${v.duration}${v.clientIdentifier ? ' &middot; ' + escapeHtml(v.clientIdentifier) : ''}</div>
            <div class="remaining">${formatRemaining(v.secondsRemaining)} remaining</div>
            <button class="danger" data-code="${v.code}" data-action="disconnect">Force Disconnect</button>
        </div>
    `).join('');
}

function renderSlots(slots) {
    slotsEl.innerHTML = slots.map(slot => `
        <div class="slot" data-duration="${slot.duration}">
            <div class="slot-header">
                <h3>${slot.label}</h3>
                <span class="count">${slot.vouchers.length} code(s)</span>
            </div>
            <button data-action="generate" data-duration="${slot.duration}">+ Generate Code</button>
            <div class="code-list">
                ${slot.vouchers.length ? slot.vouchers.map(v => `
                    <div class="code-row">
                        <div>
                            <div class="code">${v.code}</div>
                            <div class="meta" style="color: var(--muted); font-size: 0.78rem;">${statusLabel(v)}</div>
                        </div>
                        <div class="right">
                            <span class="badge ${v.status}">${v.status}</span>
                            ${v.status === 'UNUSED' ? `<button data-action="connect" data-code="${v.code}">Simulate Connect</button>` : ''}
                            ${v.status === 'ACTIVE' ? `<button class="danger" data-action="disconnect" data-code="${v.code}">Disconnect</button>` : ''}
                        </div>
                    </div>
                `).join('') : '<p class="empty">No codes generated yet.</p>'}
            </div>
        </div>
    `).join('');
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

async function refresh() {
    const [dashboard, active] = await Promise.all([
        api('/api/vouchers/dashboard'),
        api('/api/vouchers/active')
    ]);
    renderSlots(dashboard);
    renderActive(active);
}

document.addEventListener('click', (e) => {
    const target = e.target.closest('button[data-action]');
    if (!target) return;
    const action = target.dataset.action;
    if (action === 'generate') generateVoucher(target.dataset.duration, target);
    if (action === 'connect') simulateConnect(target.dataset.code);
    if (action === 'disconnect') forceDisconnect(target.dataset.code);
});

refresh().catch(err => showToast(err.message, true));
setInterval(() => refresh().catch(() => {}), POLL_INTERVAL_MS);
