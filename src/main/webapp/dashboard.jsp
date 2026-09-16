<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.miportafolio.model.Usuario" %>
<%@ page import="com.miportafolio.model.Archivo" %>
<%@ page import="com.miportafolio.model.Curso" %>
<%@ page import="com.miportafolio.dao.ArchivoDAO" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    List<Archivo> misArchivos = java.util.Collections.emptyList();
    String errorCarga = null;
    try {
        misArchivos = new ArchivoDAO().listarPorUsuario(usuario.getId());
    } catch (Exception e) {
        errorCarga = e.getMessage();
    }

    String mensajeExito = request.getParameter("exito");
    String mensajeError = request.getParameter("error");
    String inicial = usuario.getNombre().trim().substring(0, 1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subir tareas · Mi Portafolio UPLA</title>
    <link rel="icon" href="img/upla-logo.png">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,600;12..96,700&family=Karla:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>

<%@ include file="/WEB-INF/barra.jspf" %>

<main class="contenedor">

    <div class="panel-cabecera">
        <div class="panel-perfil">
            <img src="img/perfil.jpg" alt="Foto de perfil" class="foto-perfil-panel">
            <div>
                <h1>Hola, <%= usuario.getNombre() %></h1>
                <div class="correo"><%= usuario.getEmail() %></div>
                <div class="presentacion-corta">Estudiante de Ingeniería de Sistemas y Computación — III Ciclo</div>
            </div>
        </div>
        <div class="dato">
            <span class="cifra"><%= misArchivos.size() %></span>
            <span class="etiqueta">Tareas subidas</span>
        </div>
    </div>

    <% if (mensajeExito != null) { %>
        <div class="aviso aviso-exito"><%= mensajeExito %></div>
    <% } %>
    <% if (mensajeError != null) { %>
        <div class="aviso aviso-error"><%= mensajeError %></div>
    <% } %>
    <% if (errorCarga != null) { %>
        <div class="aviso aviso-error">
            No se pudieron leer tus tareas: <%= errorCarga %>
            <div style="margin-top:8px"><a href="estado">Revisar el estado del sistema</a></div>
        </div>
    <% } %>

    <section class="bloque">
        <h2>Subir una tarea</h2>
        <p class="descripcion">Indica a qué curso, unidad y semana pertenece el archivo.</p>

        <form action="subir" method="post" enctype="multipart/form-data">
            <div class="rejilla-campos">
                <div class="campo">
                    <label for="curso">Curso</label>
                    <select id="curso" name="curso" required>
                        <% for (Curso curso : Curso.getCatalogo().values()) { %>
                            <option value="<%= curso.getSlug() %>"><%= curso.getNombre() %></option>
                        <% } %>
                    </select>
                </div>

                <div class="campo">
                    <label for="unidad">Unidad</label>
                    <select id="unidad" name="unidad" required>
                        <% for (int u = 1; u <= Curso.UNIDADES; u++) { %>
                            <option value="<%= u %>">Unidad <%= u %></option>
                        <% } %>
                    </select>
                </div>

                <div class="campo">
                    <label for="semana">Semana</label>
                    <select id="semana" name="semana" required>
                        <% for (int s = 1; s <= Curso.SEMANAS_POR_UNIDAD; s++) { %>
                            <option value="<%= s %>">Semana <%= s %></option>
                        <% } %>
                    </select>
                </div>

                <div class="campo">
                    <label for="titulo">Título de la tarea</label>
                    <input type="text" id="titulo" name="titulo"
                           placeholder="Ej. Ejercicios de arreglos">
                    <p class="ayuda">Si lo dejas vacío usamos el nombre de cada archivo.
                        Si subes varios, este título se usa como prefijo común.</p>
                </div>
            </div>

            <div class="campo" style="margin-top:16px">
                <label for="archivo">Archivo(s)</label>
                <input type="file" id="archivo" name="archivo" multiple required>
                <p class="ayuda">Hasta 25 MB por archivo. Puedes seleccionar varios a la vez
                    (imágenes, código .java, PDF, Word, comprimidos, etc.) — todos se
                    guardan en la misma unidad y semana que elijas arriba.</p>
            </div>

            <button type="submit" class="btn">Subir tarea</button>
        </form>
    </section>

    <section class="bloque">
        <h2>Tus tareas</h2>
        <p class="descripcion">Todo lo que has subido, ordenado por curso, unidad y semana.</p>

        <% if (misArchivos.isEmpty()) { %>
            <div class="vacio">
                <strong>Todavía no has subido ninguna tarea</strong>
                Usa el formulario de arriba para subir la primera.
            </div>
        <% } else { %>
            <div class="lista-tareas">
            <%
                String cursoAnterior = null;
                for (Archivo archivo : misArchivos) {
                    if (!archivo.getCurso().equals(cursoAnterior)) {
                        cursoAnterior = archivo.getCurso();
            %>
                <h3 style="margin:22px 0 4px;font-size:1.02rem;color:var(--upla-profundo)">
                    <%= Curso.nombreDe(archivo.getCurso()) %>
                </h3>
            <%  } %>
                <div class="tarea">
                    <span class="tarea-icono">U<%= archivo.getUnidad() %>S<%= archivo.getSemana() %></span>
                    <span class="tarea-info">
                        <span class="titulo"><%= archivo.getTitulo() != null ? archivo.getTitulo() : archivo.getNombreOriginal() %></span>
                        <span class="meta">
                            Unidad <%= archivo.getUnidad() %> &middot; Semana <%= archivo.getSemana() %>
                            &middot; <%= archivo.getTamanoLegible() %>
                        </span>
                    </span>
                    <span class="tarea-acciones">
                        <a class="btn btn-linea btn-mini" href="visualizar?id=<%= archivo.getId() %>"
                           target="_blank" rel="noopener">Ver</a>
                        <a class="btn btn-linea btn-mini" href="descargar?id=<%= archivo.getId() %>">Descargar</a>
                        <form action="eliminar" method="post" style="display:inline"
                              onsubmit="return confirmarEliminacion('<%= archivo.getNombreOriginal().replace("'", "\\'") %>')">
                            <input type="hidden" name="id" value="<%= archivo.getId() %>">
                            <button type="submit" class="btn btn-riesgo btn-mini">Eliminar</button>
                        </form>
                    </span>
                </div>
            <% } %>
            </div>
        <% } %>
    </section>

</main>

<footer class="pie">
    Mi Portafolio · Universidad Peruana Los Andes · <%= java.time.Year.now() %>
</footer>

<script src="js/main.js"></script>
</body>
</html>
