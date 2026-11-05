document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.login-form');
  if (!form) return;
  const showError = (input, invalid) => {
    const shell = input.closest('.login-input-shell');
    const message = document.getElementById(input.getAttribute('aria-describedby'));
    shell?.classList.toggle('has-error', invalid);
    input.setAttribute('aria-invalid', String(invalid));
    if (message) message.hidden = !invalid;
  };
  form.querySelectorAll('input[required]').forEach(input => {
    input.addEventListener('input', () => showError(input, false));
  });
  const toggle = document.querySelector('.olp-password-toggle');
  toggle?.addEventListener('click', event => {
    const input = document.querySelector('#contrasena');
    const show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    event.currentTarget.setAttribute('aria-label', show ? 'Ocultar contraseña' : 'Mostrar contraseña');
  });
  form.addEventListener('submit', event => {
    let valid = true;
    const fields = [...form.querySelectorAll('input[required]')];
    fields.forEach(input => {
      const invalid = !input.value.trim();
      showError(input, invalid);
      if (invalid) valid = false;
    });
    if (!valid) { event.preventDefault(); fields.find(input => !input.value.trim())?.focus(); }
  });
  const params = new URLSearchParams(location.search);
  const alert = document.getElementById('login-server-error');
  const messages = {
    credenciales: 'Usuario o contraseña incorrectos, o cuenta inactiva.',
    servidor: 'No se pudo validar el acceso. Inténtalo nuevamente.',
    sesion: 'Inicia sesión para acceder al sistema.'
  };
  if (alert && messages[params.get('error')]) { alert.textContent = messages[params.get('error')]; alert.hidden = false; }
});
