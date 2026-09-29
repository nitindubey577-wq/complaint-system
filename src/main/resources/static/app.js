const api = '/api';
let departments = [];
let currentUser = null;
const locationInput = document.querySelector('#location');

const message = (text, error = false) => {
  const node = document.querySelector('#message');
  node.textContent = text;
  node.className = error ? 'message error' : 'message';
};

const escapeHtml = value => String(value ?? '').replace(/[&<>'"]/g, char => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
}[char]));

async function checkSession() {
  try {
    const response = await fetch('/api/auth/me');
    if (response.ok) {
      currentUser = await response.json();
      // Show "Welcome, Name" and Sign Out button
      const headerActions = document.querySelector('#headerActions');
      const welcomeTitle  = document.querySelector('#welcomeTitle');
      if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${escapeHtml(currentUser.name)} 👋`;
      if (headerActions) {
        headerActions.innerHTML = `
          <span style="font-size:13.5px;font-weight:600;color:var(--ink-soft)">${escapeHtml(currentUser.name)}</span>
          <button class="secondary" id="signOutBtn">Sign out</button>
        `;
        document.querySelector('#signOutBtn').addEventListener('click', async () => {
          await fetch('/api/auth/logout', { method: 'POST' });
          location.reload();
        });
      }
      await loadComplaints();
    } else {
      // Not logged in — show sign-in prompt in complaints table
      complaintRows.innerHTML = `<tr><td colspan="6" class="login-prompt"><a href="/login.html">Sign in</a> or <a href="/signup.html">Sign up</a> to view recent complaints.</td></tr>`;
    }
  } catch {
    complaintRows.innerHTML = `<tr><td colspan="6" class="login-prompt"><a href="/login.html">Sign in</a> or <a href="/signup.html">Sign up</a> to view recent complaints.</td></tr>`;
  }
}

async function loadComplaints() {
  const status = document.querySelector('#statusFilter').value;
  try {
    const response = await fetch(`${api}/complaints${status ? `?status=${encodeURIComponent(status)}` : ''}`);
    if (response.status === 401) { return; }
    if (!response.ok) throw new Error('Could not load complaints.');

    const complaints = await response.json();
    complaintRows.innerHTML = complaints.length
      ? complaints.map(item => `<tr><td>#${escapeHtml(item.id)}</td><td><strong>${escapeHtml(item.title)}</strong><br><small>${escapeHtml(item.description)}</small></td><td>${escapeHtml(item.location || 'Not provided')}</td><td><span class="badge ${escapeHtml(item.status)}">${escapeHtml(item.status)}</span></td><td>${item.departmentId ? `Department #${escapeHtml(item.departmentId)}` : 'Not assigned'}</td><td><button class="small-button" onclick="viewComplaint('${escapeHtml(item.id)}')">View complaint</button></td></tr>`).join('')
      : '<tr><td colspan="6">No complaints found.</td></tr>';

    const all = status ? await (await fetch(`${api}/complaints`)).json() : complaints;
    totalCount.textContent = all.length;
    pendingCount.textContent = all.filter(c => c.status === 'PENDING').length;
    progressCount.textContent = all.filter(c => c.status === 'IN_PROGRESS').length;
    resolvedCount.textContent = all.filter(c => ['RESOLVED', 'CLOSED'].includes(c.status)).length;
  } catch (error) { message(error.message, true); }
}

async function viewComplaint(id) {
  try {
    const response = await fetch(`${api}/complaints/${id}`);
    if (!response.ok) throw new Error('Complaint could not be loaded.');
    const complaint = await response.json();
    detailTitle.textContent = `Complaint #${complaint.id}: ${complaint.title}`;
    detailMeta.textContent = `Department ID: ${complaint.departmentId || 'Not assigned'} · Status: ${complaint.status}`;
    detailDescription.textContent = complaint.description;
    detailLocation.textContent = complaint.location || 'Not provided';
    renderStatusTimeline(complaint.status);
    complaintDetails.hidden = false;
    complaintDetails.scrollIntoView({ behavior: 'smooth', block: 'start' });
  } catch (error) { message(error.message, true); }
}

function renderStatusTimeline(status) {
  const steps = ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
  if (status === 'REJECTED') {
    statusTimeline.className = 'status-timeline rejected';
    statusTimeline.innerHTML = '<div class="status-step rejected">REJECTED</div>';
    return;
  }
  const currentIndex = steps.indexOf(status);
  statusTimeline.className = 'status-timeline';
  statusTimeline.innerHTML = steps.map((step, index) => `<div class="status-step ${index < currentIndex ? 'done' : ''} ${index === currentIndex ? 'current' : ''}">${step.replace('_', ' ')}</div>`).join('');
}

document.querySelector('#closeDetailsButton').addEventListener('click', () => { complaintDetails.hidden = true; });

async function loadDepartments() {
  try {
    const response = await fetch(`${api}/departments`);
    if (response.status === 401) { return; }
    if (!response.ok) throw new Error();

    departments = await response.json();
    departmentList.innerHTML = departments.map(department => `<article class="department"><strong>#${escapeHtml(department.id)} — ${escapeHtml(department.name)}</strong><p>${escapeHtml(department.description || 'No description')}</p></article>`).join('');
    departmentId.innerHTML = '<option value="">Choose a department</option>' + departments.map(department => `<option value="${escapeHtml(department.id)}">#${escapeHtml(department.id)} — ${escapeHtml(department.name)}</option>`).join('');
  } catch { departmentList.textContent = 'Departments could not be loaded.'; }
}


document.querySelector('#complaintForm').addEventListener('submit', async event => {
  event.preventDefault();
  const selected = departments.find(department => department.id === departmentId.value);
  const body = {
    title: title.value,
    location: locationInput.value,
    description: description.value,
    departmentId: departmentId.value,
    category: selected ? selected.name.toUpperCase().replaceAll(' ', '_') : 'GENERAL'
  };

  try {
    const response = await fetch(`${api}/complaints`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Complaint could not be submitted.');

    event.target.reset();
    message(`Complaint #${data.id} was submitted successfully.`);
    await loadComplaints();
  } catch (error) { message(error.message, true); }
});

document.querySelector('#currentLocationButton').addEventListener('click', () => {
  const hint = document.querySelector('#locationHint');
  const button = document.querySelector('#currentLocationButton');
  if (!navigator.geolocation) {
    hint.textContent = 'Your browser does not support location. Please enter it manually.';
    return;
  }

  button.disabled = true;
  button.textContent = 'Getting location…';
  hint.textContent = 'Allow location access in your browser when asked.';

  navigator.geolocation.getCurrentPosition(
    async position => {
      const { latitude, longitude } = position.coords;
      hint.textContent = 'Finding the nearest address…';
      try {
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1`, {
          headers: { 'Accept-Language': 'en' }
        });
        const data = await response.json();
        if (!response.ok || !data.display_name) throw new Error();
        locationInput.value = data.display_name;
        hint.textContent = 'Current address selected. You can edit it if needed.';
      } catch {
        locationInput.value = `GPS location: ${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
        hint.textContent = 'An address could not be found, so GPS coordinates were selected instead.';
      } finally {
        button.disabled = false;
        button.textContent = '⌖ Use current location';
      }
    },
    () => {
      hint.textContent = 'Location permission was unavailable. Please enter a landmark or address manually.';
      button.disabled = false;
      button.textContent = '⌖ Use current location';
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  );
});

checkSession();
loadDepartments();
