package com.miportafolio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.Filter;

import java.io.IOException;

/**
 * Fuerza UTF-8 en cada peticion y respuesta.
 *
 * Sin esto, los acentos escritos en formularios (nombre, titulo de una
 * tarea, etc.) pueden guardarse mal en la base de datos, aunque las
 * paginas .jsp ya declaren UTF-8 individualmente.
 */
@WebFilter("/*")
public class CodificacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        chain.doFilter(request, response);
    }
}
