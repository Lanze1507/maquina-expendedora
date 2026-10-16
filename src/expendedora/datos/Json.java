package expendedora.datos;

import java.math.BigDecimal;
import java.util.*;

/** Lector JSON estricto sin dependencias: objetos, listas, cadenas y números exactos. */
final class Json {
    private final String texto;
    private int pos;
    private Json(String texto) { this.texto = texto; }
    static Object leer(String texto) {
        if (texto == null) throw new DatosException("JSON_INVALIDO", "JSON nulo.");
        Json j = new Json(texto);
        if (texto.startsWith("\uFEFF")) j.pos++;
        Object resultado = j.valor(0);
        j.espacios();
        if (j.pos != texto.length()) throw j.error("Contenido extra después del JSON");
        return resultado;
    }
    private DatosException error(String mensaje) {
        int linea = 1, columna = 1;
        for (int i = 0; i < pos; i++) {
            if (texto.charAt(i) == '\n') { linea++; columna = 1; } else columna++;
        }
        return new DatosException("JSON_INVALIDO", mensaje + " (línea " + linea + ", columna " + columna + ").");
    }
    private void espacios() {
        while (pos < texto.length() && " \t\r\n".indexOf(texto.charAt(pos)) >= 0) pos++;
    }
    private boolean tomar(char c) {
        if (pos < texto.length() && texto.charAt(pos) == c) { pos++; return true; }
        return false;
    }
    private void exigir(char c) { espacios(); if (!tomar(c)) throw error("Se esperaba '" + c + "'"); }
    private Object valor(int profundidad) {
        if (profundidad > 64) throw error("JSON demasiado anidado");
        espacios();
        if (pos == texto.length()) throw error("Falta un valor");
        char c = texto.charAt(pos);
        if (c == '"') return cadena();
        if (c == '{') {
            pos++;
            Map<String,Object> mapa = new LinkedHashMap<>();
            espacios(); if (tomar('}')) return mapa;
            do {
                espacios(); if (pos == texto.length() || texto.charAt(pos) != '"') throw error("Falta una clave entre comillas");
                String clave = cadena(); exigir(':');
                if (mapa.containsKey(clave)) throw error("Clave JSON duplicada: " + clave);
                mapa.put(clave, valor(profundidad + 1)); espacios();
                if (tomar('}')) return mapa;
                exigir(',');
            } while (true);
        }
        if (c == '[') {
            pos++; List<Object> lista = new ArrayList<>();
            espacios(); if (tomar(']')) return lista;
            do {
                lista.add(valor(profundidad + 1)); espacios();
                if (tomar(']')) return lista;
                exigir(',');
            } while (true);
        }
        if (texto.startsWith("null", pos)) { pos += 4; return null; }
        if (texto.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
        if (texto.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        if (c == '-' || digito(c)) return numero();
        throw error("Valor JSON inválido");
    }
    private static boolean digito(char c) { return c >= '0' && c <= '9'; }
    private void digitos() {
        int inicio = pos;
        while (pos < texto.length() && digito(texto.charAt(pos))) pos++;
        if (pos == inicio) throw error("Faltan dígitos");
    }
    private BigDecimal numero() {
        int inicio = pos; tomar('-');
        if (!tomar('0')) digitos();
        if (tomar('.')) digitos();
        if (tomar('e') || tomar('E')) { if (!tomar('+')) tomar('-'); digitos(); }
        if (pos - inicio > 1100) throw error("Número demasiado largo");
        try {
            BigDecimal n = new BigDecimal(texto.substring(inicio, pos));
            Producto.validarNumero(n);
            return n;
        } catch (NumberFormatException ex) { throw error("Número fuera de rango"); }
    }
    private String cadena() {
        exigir('"'); StringBuilder b = new StringBuilder();
        while (pos < texto.length()) {
            char c = texto.charAt(pos++);
            if (c == '"') {
                String resultado = b.toString();
                for (int i=0; i<resultado.length(); i++) {
                    char u=resultado.charAt(i);
                    if (Character.isHighSurrogate(u)) {
                        if (++i >= resultado.length() || !Character.isLowSurrogate(resultado.charAt(i))) throw error("Unicode inválido");
                    } else if (Character.isLowSurrogate(u)) throw error("Unicode inválido");
                }
                return resultado;
            }
            if (c < 32) throw error("Control sin escapar en cadena");
            if (c != '\\') { b.append(c); continue; }
            if (pos == texto.length()) throw error("Escape incompleto");
            c = texto.charAt(pos++);
            switch(c) {
                case '"': case '\\': case '/': b.append(c); break;
                case 'b': b.append('\b'); break;
                case 'f': b.append('\f'); break;
                case 'n': b.append('\n'); break;
                case 'r': b.append('\r'); break;
                case 't': b.append('\t'); break;
                case 'u':
                    if (pos + 4 > texto.length()) throw error("Escape Unicode incompleto");
                    int u = 0;
                    for (int i=0; i<4; i++) {
                        char h = texto.charAt(pos++);
                        int d = "0123456789abcdef".indexOf(Character.toLowerCase(h));
                        if (d < 0) throw error("Escape Unicode inválido");
                        u = u * 16 + d;
                    }
                    b.append((char)u); break;
                default: throw error("Escape inválido");
            }
        }
        throw error("Cadena sin cerrar");
    }
    static String citar(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i=0; i<s.length(); i++) {
            char c=s.charAt(i);
            switch(c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 32) b.append(String.format(Locale.ROOT, "\\u%04x", (int)c));
                    else b.append(c);
            }
        }
        return b.append('"').toString();
    }
}
