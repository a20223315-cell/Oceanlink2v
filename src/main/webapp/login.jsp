<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html><html lang="es"><head>
<meta charset="utf-8">
<meta content="width=device-width, initial-scale=1.0" name="viewport">
<title>OceanLink - Iniciar sesión</title>

<link href="css/fonts.css?v=20261028-edicion-error" rel="stylesheet">
<link href="css/base.css?v=20261028-edicion-error" rel="stylesheet">
<link href="css/theme.css?v=20261028-edicion-error" rel="stylesheet">
<link href="css/final.css?v=20261028-edicion-error" rel="stylesheet">
<link href="css/login-hero.css?v=20261105-mensaje-login" rel="stylesheet">
<link href="css/vendor/fontawesome.css?v=20261028-edicion-error" rel="stylesheet">
<style>#login-server-error[hidden]{display:none}</style></head>
<body class="login-page olp-page">
  <main class="olp-layout" aria-label="Acceso a OceanLink">
    <section class="olp-brand" aria-label="OceanLink">
      <div class="olp-brand-glow olp-brand-glow--a" aria-hidden="true"></div>
      <div class="olp-brand-glow olp-brand-glow--b" aria-hidden="true"></div>

      <header class="olp-brand-head">
        <img class="olp-logo" src="assets/oceanlink-logo-sidebar.svg" alt="OceanLink">
        <span class="olp-internal"><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> Uso interno</span>
      </header>

      <div class="olp-brand-copy">
        <span class="olp-eyebrow">Gestión operativa de red submarina</span>
        <h1>Cada conexión tiene una historia, <span>nosotros cuidamos su continuidad.</span></h1>
        <p>Gestionamos juntos la infraestructura que conecta nuestra red submarina.</p>
      </div>

      <div class="olp-submarine-art">
        <img src="assets/oceanlink-submarine.svg" width="900" height="500" alt="Red submarina ilustrada: tres estaciones costeras conectadas mediante fibra óptica, con repetidores y una bifurcación sobre el fondo marino.">
      </div>
    </section>

    <svg class="olp-seam" viewBox="0 0 180 1000" preserveAspectRatio="none" aria-hidden="true">
      <path class="olp-seam-fill" d="M108 0 H180 V1000 H108 C94 914 116 834 102 748 C88 663 113 579 101 492 C89 404 112 320 100 232 C91 160 108 83 108 0 Z"></path>
      <path class="olp-seam-line" d="M108 0 C108 83 91 160 100 232 C112 320 89 404 101 492 C113 579 88 663 102 748 C116 834 94 914 108 1000"></path>
    </svg>

    <section class="olp-access" aria-label="Inicio de sesión">
      <div class="olp-access-inner">
        <header class="olp-access-head">
          <img class="olp-mobile-logo" src="assets/oceanlink-logo.svg" alt="OceanLink">
          <span class="olp-access-kicker">Acceso al sistema</span>
          <h2>Bienvenido</h2>
          <p>Ingresa con tu correo o código de usuario y contraseña para acceder según los permisos de tu rol.</p>
        </header>

        <form class="login-form olp-form" method="post" action="LoginServlet" novalidate="">
          <div class="olp-field">
            <label for="usuario">Usuario</label>
            <div class="login-input-shell olp-input-shell" data-login-field="usuario">
              <div class="olp-control">
                <span class="olp-control-icon" aria-hidden="true"><i class="fa-regular fa-user"></i></span>
                <input id="usuario" name="usuario" placeholder="Ingrese su correo o código de usuario" required="" type="text" autocomplete="username" aria-describedby="usuario-error">
              <span class="login-inline-error" id="usuario-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i> Ingrese su usuario.</span>
              </div>
            </div>
          </div>

          <div class="olp-field">
            <label for="contrasena">Contraseña</label>
            <div class="login-input-shell olp-input-shell" data-login-field="contrasena">
              <div class="olp-control">
                <span class="olp-control-icon" aria-hidden="true"><i class="fa-solid fa-lock"></i></span>
                <input id="contrasena" name="contrasena" placeholder="Ingrese su contraseña" required="" type="password" autocomplete="current-password" aria-describedby="contrasena-error">
                <button class="olp-password-toggle" type="button" aria-label="Mostrar contraseña" title="Mostrar contraseña"><i class="fa-regular fa-eye" aria-hidden="true"></i></button>
              <span class="login-inline-error" id="contrasena-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i> Ingrese su contraseña.</span>
              </div>
            </div>
          </div>

          <div class="olp-action-label"><span>Acceso al sistema</span></div>
          <div class="olp-actions" aria-label="Ingreso al sistema">
            <button class="olp-role-button olp-role-button--admin olp-role-button--wide" type="submit">
              <span class="olp-role-icon"><i class="fa-solid fa-right-to-bracket" aria-hidden="true"></i></span>
              <span class="olp-role-copy"><strong>Iniciar sesión</strong><small>El sistema identificará tu rol automáticamente</small></span>
              <i class="fa-solid fa-arrow-right olp-role-arrow" aria-hidden="true"></i>
            </button>
          </div>
          <%
            String mensajeLogin = (String) session.getAttribute("mensajeLogin");
            if (mensajeLogin != null) {
                session.removeAttribute("mensajeLogin");
          %>
          <p id="login-server-error" class="login-inline-error" role="alert"><%= mensajeLogin %></p>
          <% } %>
        </form>

        <footer class="olp-access-foot">
          <span><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> Acceso protegido mediante cuenta y contraseña.</span>
          <span>¿Problemas de acceso? Comunícate con el administrador.</span>
        </footer>
      </div>
    </section>
  </main>
<script defer="" src="js/login-acceso.js?v=20261104-sesiones-roles"></script>

</body></html>
