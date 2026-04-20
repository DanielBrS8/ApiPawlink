package com.pawlink.api.chat;

import java.io.*;
import java.net.*;

public class ManejadorClienteChat implements Runnable {
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private String nombreUsuario;

    public ManejadorClienteChat(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            salida.println("Bienvenido al chat! Por favor ingresa tu nombre:");
            nombreUsuario = entrada.readLine();
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                nombreUsuario = "Anónimo_" + socket.getPort();
            }

            salida.println("Bienvenido " + nombreUsuario + "!");
            salida.println("Comandos disponibles: /usuarios, /salir");
            ServidorChat.broadcast("*** " + nombreUsuario + " se ha unido al chat ***", this);

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.startsWith("/")) {
                    procesarComando(mensaje);
                } else if (!mensaje.trim().isEmpty()) {
                    String mensajeFormateado = nombreUsuario + ": " + mensaje;
                    System.out.println(mensajeFormateado);
                    ServidorChat.broadcast(mensajeFormateado, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Error con el cliente " + nombreUsuario + ": " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    private void procesarComando(String comando) {
        if (comando.equalsIgnoreCase("/salir")) {
            salida.println("Hasta luego!");
            desconectar();
        } else if (comando.equalsIgnoreCase("/usuarios")) {
            salida.println(ServidorChat.obtenerListaUsuarios());
        } else {
            salida.println("Comando desconocido. Comandos disponibles: /usuarios, /salir");
        }
    }

    public void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    private void desconectar() {
        try {
            ServidorChat.removerCliente(this);
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
