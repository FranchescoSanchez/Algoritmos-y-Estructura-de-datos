<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String emailPrevio = request.getAttribute("email") != null
            ? String.valueOf(request.getAttribute("email")) : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar sesión · Mi Portafolio UPLA</title>
    <link rel="icon" href="img/upla-logo.png">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;12..96,700&family=Karla:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>

<%@ include file="/WEB-INF/barra.jspf" %>

<div class="tarjeta-form">
    <div class="marca-form">
        <img src="img/upla-logo.png" alt="Universidad Peruana Los Andes">
        <h1>Iniciar sesión</h1>
        <p class="subtitulo">Entra para subir y gestionar tus tareas.</p>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="aviso aviso-error">
            <%= request.getAttribute("error") %>
            <div style="margin-top:8px"><a href="estado">Revisar el estado del sistema</a></div>
        </div>
    <% } %>
    <% if (request.getAttribute("exito") != null) { %>
        <div class="aviso aviso-exito"><%= request.getAttribute("exito") %></div>
    <% } %>

    <form action="login" method="post">
        <div class="campo">
            <label for="email">Correo electrónico</label>
            <input type="email" id="email" name="email" required autocomplete="email"
                   value="<%= emailPrevio %>" placeholder="tucorreo@upla.edu.pe">
        </div>
        <div class="campo">
            <label for="password">Contraseña</label>
            <input type="password" id="password" name="password" required
                   autocomplete="current-password" placeholder="Tu contraseña">
        </div>
        <button type="submit" class="btn btn-ancho">Entrar</button>
    </form>

    <p class="pie-form">
        ¿Aún no tienes cuenta? <a href="registro.jsp">Créala aquí</a>
    </p>
</div>

</body>
</html>
