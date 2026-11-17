package com.barberiacortes.servicios.util;

/**
 * Convierte texto en HTML seguro antes de mostrarlo en una página JSP.
 * Evita que un dato guardado (por ejmplo, un nombre con "<script>")
 * se inrepreta como código en el navegador.
 * TextoHtml
 */

public final class TextoHtml {
    
    private TextoHtml() {
        // Clase de utilidad: no se instancia
    }

    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder(texto.length());
        for (char caracter : texto.toCharArray()) {
            switch (caracter) {
                case '&' -> resultado.append("&amp;");
                case '<' -> resultado.append("&lt;");
                case '>' -> resultado.append("&gt;");
                case '"' -> resultado.append("&quot;");
                case '\'' -> resultado.append("&#39;");
                default -> resultado.append(caracter);
            }
        }
        return resultado.toString();
    }
}
