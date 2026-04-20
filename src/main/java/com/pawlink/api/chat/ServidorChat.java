package com.pawlink.api.chat;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ServidorChat {
    private static final int PUERTO_CHAT = 9090;
    private static final int MAX_CLIENTES = 10;
    private static Set<ManejadorClienteChat> clientes = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void iniciar() {
        new Thread(() -> {
            ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTES);
            System.out.println("=== SERVIDOR DE CHAT PAWLINK ===");
            System.out.println("Iniciado Sockets en puerto " + PUERTO_CHAT);
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_CHAT)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ManejadorClienteChat manejador = new ManejadorClienteChat(clientSocket);
                    clientes.add(manejador);
                    pool.execute(manejador);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                pool.shutdown();
            }
        }).start();
    }

    public static void broadcast(String mensaje, ManejadorClienteChat remitente) {
        for (ManejadorClienteChat cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }

    public static void removerCliente(ManejadorClienteChat cliente) {
        clientes.remove(cliente);
    }

    public static String obtenerListaUsuarios() {
        StringBuilder lista = new StringBuilder("Usuarios conectados: ");
        for (ManejadorClienteChat cliente : clientes) {
            lista.append(cliente.getNombreUsuario()).append(", ");
        }
        if (lista.length() > 22) {
            lista.setLength(lista.length() - 2);
        }
        return lista.toString();
    }
}
