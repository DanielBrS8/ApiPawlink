package com.pawlink.api.chat;

import java.io.*;
import java.net.*;

public class ManejadorClienteChat implements Runnable {
    private final Socket socket;
    private final ServidorChat servidorChat;
    private PrintWriter salida;
    private BufferedReader entrada;
    private Integer idUsuario;

    public ManejadorClienteChat(Socket socket, ServidorChat servidorChat) {
        this.socket = socket;
        this.servidorChat = servidorChat;
    }

    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            String primeraLinea = entrada.readLine();
            if (primeraLinea == null) {
                cerrarSocket();
                return;
            }

            try {
                idUsuario = Integer.parseInt(primeraLinea.trim());
            } catch (NumberFormatException e) {
                cerrarSocket();
                return;
            }

            servidorChat.registrar(idUsuario, this);

            while (entrada.readLine() != null) {
                // Mantener la conexión viva; el servidor sólo envía notificaciones.
            }
        } catch (IOException ignored) {
        } finally {
            if (idUsuario != null) {
                servidorChat.eliminar(idUsuario);
            }
            cerrarSocket();
        }
    }

    public void enviarNotificacion(String texto) {
        if (salida != null) {
            salida.println(texto);
            salida.flush();
        }
    }

    private void cerrarSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
