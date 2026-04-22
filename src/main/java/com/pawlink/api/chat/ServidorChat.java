package com.pawlink.api.chat;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ServidorChat {
    private static final int PUERTO_CHAT = 9090;
    private static final int MAX_CLIENTES = 10;

    private final Map<Integer, ManejadorClienteChat> clientes = new ConcurrentHashMap<>();

    @PostConstruct
    public void iniciar() {
        new Thread(() -> {
            ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTES);
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_CHAT)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ManejadorClienteChat manejador = new ManejadorClienteChat(clientSocket, this);
                    pool.execute(manejador);
                }
            } catch (IOException e) {
                // Error al iniciar/aceptar en el servidor de notificaciones TCP.
            } finally {
                pool.shutdown();
            }
        }).start();
    }

    public void registrar(Integer idUsuario, ManejadorClienteChat manejador) {
        clientes.put(idUsuario, manejador);
    }

    public void eliminar(Integer idUsuario) {
        clientes.remove(idUsuario);
    }

    public boolean notificar(Integer idUsuario, String texto) {
        if (idUsuario == null) {
            return false;
        }
        ManejadorClienteChat manejador = clientes.get(idUsuario);
        if (manejador == null) {
            return false;
        }
        manejador.enviarNotificacion(texto);
        return true;
    }
}
