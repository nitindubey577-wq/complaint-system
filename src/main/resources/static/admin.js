const api = '/api';
let departments = [];
const msg = (text, error = false) => { message.textContent = text; message.className = error ? 'message error' : 'message'; };
const esc = value => String(value ?? '').replace(/[&<>'"]/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[char]));
const departmentOptions = (selected = '') => `<option value="">Select a department</option>${departments.map(department => `<option value="${department.id}" ${department.id === selected ? 'selected' : ''}>#${department.id} — ${esc(department.name)}</option>`).join('')}`;

async function loadDepartments() {
  const response = await fetch(`${api}/departments`);
  departments = await response.json();
  userDepartmentId.innerHTML = departmentOptions();
}

async function refresh() {
  await loadDepartments();
  const [users, complaints] = await Promise.all([
    fetch(`${api}/users`).then(response => response.json()),
    fetch(`${api}/complaints`).then(response => response.json())
  ]);
  userRows.innerHTML = users.map(user => `<tr><td>#${esc(user.id)}</td><td>${esc(user.name)}</td><td>${esc(user.email)}</td><td>${esc(user.role)}</td><td>${user.departmentId ? `#${esc(user.departmentId)}` : '—'}</td></tr>`).join('');
  adminComplaintRows.innerHTML = complaints.map(complaint => `<tr><td>#${complaint.id} ${esc(complaint.title)}<br><small>${esc(complaint.status)}</small></td><td><select id="d${complaint.id}">${departmentOptions(complaint.departmentId || '')}</select></td><td><input id="o${complaint.id}" value="${esc(complaint.assignedOfficerId || '')}" placeholder="Officer ID"></td><td><button onclick="assign('${complaint.id}')">Assign</button> <select id="s${complaint.id}"><option value="">Status</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>REJECTED</option><option>CLOSED</option></select><button onclick="status('${complaint.id}')">Update</button></td></tr>`).join('');
}

async function assign(id) {
  const departmentId = document.querySelector(`#d${id}`).value;
  const officerId = document.querySelector(`#o${id}`).value.trim();
  if (!departmentId) return msg('Select a department before assigning.', true);
  const response = await fetch(`${api}/complaints/${id}/assign?departmentId=${encodeURIComponent(departmentId)}&officerId=${encodeURIComponent(officerId)}&remark=Assigned`, { method: 'PUT' });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) return msg(result.message || 'Assignment failed.', true);
  msg('Complaint assigned.'); refresh();
}

async function status(id) {
  const newStatus = document.querySelector(`#s${id}`).value;
  if (!newStatus) return msg('Choose a status.', true);
  const response = await fetch(`${api}/complaints/${id}/status?newStatus=${newStatus}&remark=Updated`, { method: 'PUT' });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) return msg(result.message || 'Invalid workflow transition.', true);
  msg('Status updated.'); refresh();
}

departmentForm.onsubmit = async event => {
  event.preventDefault();
  const response = await fetch(`${api}/departments`, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({name: departmentName.value, description: departmentDescription.value}) });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) return msg(result.message || 'Could not create department.', true);
  event.target.reset(); msg(`Department #${result.id} created and added to the dropdowns.`); refresh();
};

userForm.onsubmit = async event => {
  event.preventDefault();
  if (!userDepartmentId.value) return msg('Select a department for the officer.', true);
  const response = await fetch(`${api}/users`, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({name: userName.value, email: userEmail.value, password: userPassword.value, departmentId: userDepartmentId.value, role: 'OFFICER'}) });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) return msg(result.message || 'Could not create staff account.', true);
  event.target.reset(); msg(`Officer account created. Officer ID: #${result.id}`); refresh();
};
refresh();
