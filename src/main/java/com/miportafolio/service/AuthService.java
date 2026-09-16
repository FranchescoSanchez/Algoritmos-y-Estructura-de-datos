package com.miportafolio.service;

import com.miportafolio.config.DatabaseInitializer;
import com.miportafolio.dao.UsuarioDAO;
import com.miportafolio.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario registrar(String nombre, String email, String password) throws SQLException {

        if (!DatabaseInitializer.isBaseDatosLista()) {
            throw new IllegalStateException(
                    "La aplicación no está conectada a la base de datos. Detalle: "
                            + DatabaseInitializer.getUltimoError());
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe tu nombre completo.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Escribe un correo electrónico válido.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        nombre = nombre.trim();
        email = email.trim().toLowerCase();

        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        return usuarioDAO.crear(nombre, email, hash);
    }

    public Usuario login(String email, String password) throws SQLException {

        if (!DatabaseInitializer.isBaseDatosLista()) {
            throw new IllegalStateException(
                    "La aplicación no está conectada a la base de datos. Detalle: "
                            + DatabaseInitializer.getUltimoError());
        }

        if (email == null || password == null) {
            return null;
        }

        Optional<Usuario> usuario = usuarioDAO.buscarPorEmail(email.trim().toLowerCase());
        if (usuario.isEmpty()) {
            return null;
        }
        return BCrypt.checkpw(password, usuario.get().getPasswordHash()) ? usuario.get() : null;
    }
}
