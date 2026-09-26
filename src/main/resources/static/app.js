const api = '/api';
let departments = [];

const message = (text, error = false) => {
  const node = document.querySelector('#message');
  node.textContent = text;
  node.className = error ? 'message error' : 'message';
};

const escapeHtml = value => String(value ?? '').replace(/[&<>'"]/g, char => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;'
}[char]));

async function loadComplaints() {
  const status = document.querySelector('#statusFilter').value;
  try {
    const response = await fetch(`${api}/complaints${status ? `?status=${encodeURIComponent(status)}` : ''}`);
    if (response.status === 401) { return; }
    if (!response.ok) throw new Error('Could not load complaints.');

    const complaints = await response.json();
    complaintRows.innerHTML = complaints.length
      ? complaints.map(item => `<tr><td>#${escapeHtml(item.id)}</td><td><strong>${escapeHtml(item.title)}</strong><br><small>${escapeHtml(item.description)}</small></td><td>${escapeHtml(item.category)}</td><td><span class="badge ${escapeHtml(item.status)}">${escapeHtml(item.status)}</span></td><td>${item.departmentId ? `Department #${escapeHtml(item.departmentId)}` : 'Not assigned'}</td></tr>`).join('')
      : '<tr><td colspan="5">No complaints found.</td></tr>';

    const all = status ? await (await fetch(`${api}/complaints`)).json() : complaints;
    totalCount.textContent = all.length;
    pendingCount.textContent = all.filter(c => c.status === 'PENDING').length;
    progressCount.textContent = all.filter(c => c.status === 'IN_PROGRESS').length;
    resolvedCount.textContent = all.filter(c => ['RESOLVED', 'CLOSED'].includes(c.status)).length;
  } catch (error) { message(error.message, true); }
}

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

loadComplaints();
loadDepartments();
