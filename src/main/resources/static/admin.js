const api = '/api';
let departments = [];
const msg = (text, error = false) => { message.textContent = text; message.className = error ? 'message error' : 'message'; };
const esc = value => String(value ?? '').replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));
const departmentOptions = selected => `<option value="">Select a department</option>${departments.map(d => `<option value="${d.id}" ${d.id === selected ? 'selected' : ''}>#${d.id} — ${esc(d.name)}</option>`).join('')}`;
const isPrevious = complaint => ['RESOLVED', 'REJECTED', 'CLOSED'].includes(complaint.status);

async function loadDepartments() {
  const response = await fetch(`${api}/departments`);
  departments = await response.json();
  userDepartmentId.innerHTML = departmentOptions('');
}

function activeRow(c) {
  const assignment = c.status === 'PENDING'
    ? `<select id="d${c.id}">${departmentOptions(c.departmentId || '')}</select><input id="o${c.id}" value="${esc(c.assignedOfficerId || '')}" placeholder="Officer ID"><button class="small-button" onclick="assign('${c.id}')">Assign</button>`
    : '<span class="muted">Already assigned</span>';
  const statusChoices = c.status === 'ASSIGNED' ? '<option>IN_PROGRESS</option>' : c.status === 'IN_PROGRESS' ? '<option>RESOLVED</option>' : c.status === 'PENDING' ? '<option>REJECTED</option>' : '';
  const update = statusChoices ? `<select id="s${c.id}"><option value="">Choose status</option>${statusChoices}</select><button class="small-button" onclick="status('${c.id}')">Update status</button>` : '<span class="muted">No update available</span>';
  return `<tr><td>#${esc(c.id)} ${esc(c.title)}<br><small>${esc(c.status)}</small></td><td>${assignment}</td><td>${update}</td></tr>`;
}

async function refresh() {
  try {
    await loadDepartments();
    const [users, complaints] = await Promise.all([fetch(`${api}/users`).then(r => r.json()), fetch(`${api}/complaints`).then(r => r.json())]);
    userRows.innerHTML = users.map(u => `<tr><td>#${esc(u.id)}</td><td>${esc(u.name)}</td><td>${esc(u.email)}</td><td>${esc(u.role)}</td><td>${u.departmentId ? `#${esc(u.departmentId)}` : '—'}</td></tr>`).join('');
    const active = complaints.filter(c => !isPrevious(c));
    const previous = complaints.filter(isPrevious);
    adminComplaintRows.innerHTML = active.length ? active.map(activeRow).join('') : '<tr><td colspan="3">No active complaints.</td></tr>';
    previousComplaintRows.innerHTML = previous.length ? previous.map(c => `<tr><td>#${esc(c.id)} ${esc(c.title)}</td><td>${esc(c.location || 'Not provided')}</td><td>${c.departmentId ? `#${esc(c.departmentId)}` : '—'}</td><td><span class="badge ${esc(c.status)}">${esc(c.status)}</span></td></tr>`).join('') : '<tr><td colspan="4">No previous complaints.</td></tr>';
  } catch { msg('Could not load the administration data.', true); }
}

async function assign(id) {
  const departmentId = document.querySelector(`#d${id}`).value;
  const officerId = document.querySelector(`#o${id}`).value.trim();
  if (!departmentId) return msg('Select a department before assigning.', true);
  const r = await fetch(`${api}/complaints/${id}/assign?departmentId=${encodeURIComponent(departmentId)}&officerId=${encodeURIComponent(officerId)}&remark=Assigned`, {method:'PUT'});
  const data = await r.json().catch(() => ({}));
  if (!r.ok) return msg(data.message || 'Assignment failed.', true);
  msg('Complaint assigned.'); refresh();
}

async function status(id) {
  const newStatus = document.querySelector(`#s${id}`).value;
  if (!newStatus) return msg('Choose a status.', true);
  const r = await fetch(`${api}/complaints/${id}/status?newStatus=${newStatus}&remark=Updated`, {method:'PUT'});
  const data = await r.json().catch(() => ({}));
  if (!r.ok) return msg(data.message || 'Invalid workflow transition.', true);
  msg(newStatus === 'RESOLVED' || newStatus === 'REJECTED' ? 'Complaint moved to Previous complaints.' : 'Status updated.');
  refresh();
}

departmentForm.onsubmit = async e => {
  e.preventDefault();
  const r = await fetch(`${api}/departments`, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:departmentName.value,description:departmentDescription.value})});
  const data = await r.json().catch(() => ({}));
  if (!r.ok) return msg(data.message || 'Could not create department.', true);
  e.target.reset(); msg(`Department #${data.id} created.`); refresh();
};

userForm.onsubmit = async e => {
  e.preventDefault();
  if (!userDepartmentId.value) return msg('Select a department for the officer.', true);
  const r = await fetch(`${api}/users`, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:userName.value,email:userEmail.value,password:userPassword.value,departmentId:userDepartmentId.value,role:'OFFICER'})});
  const data = await r.json().catch(() => ({}));
  if (!r.ok) return msg(data.message || 'Could not create officer account.', true);
  e.target.reset(); msg(`Officer account created. Officer ID: #${data.id}`); refresh();
};
refresh();
