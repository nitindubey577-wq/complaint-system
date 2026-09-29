const form = document.querySelector('#signupForm');
const message = document.querySelector('#message');

form.addEventListener('submit', async event => {
  event.preventDefault();
  const name = document.querySelector('#name').value.trim();
  const email = document.querySelector('#email').value.trim();
  const phone = document.querySelector('#phone').value.trim();
  const password = document.querySelector('#password').value;

  const button = form.querySelector('button');
  button.disabled = true;
  button.textContent = 'Creating account…';
  message.textContent = '';
  message.className = 'message';

  try {
    const response = await fetch('/api/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, phone, password })
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.message || 'Could not create account.');
    }
    window.location.href = '/';
  } catch (error) {
    message.textContent = error.message;
    message.className = 'message error';
    button.disabled = false;
    button.textContent = 'Sign up';
  }
});
