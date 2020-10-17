package Homework78.Server;
/*
    В данном задание сервер получается от одного из клиентов сообщение (в каком-то ClientHandler из списка clients)
    его транслирует остальным через метод broadcastMsg

 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private List<ClientHandler> clients; // Список подключений
    private AuthService authService; //Объект для обработки аудентификации

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket = null;
        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запустился");

            while (true) { // Бесконечный цикл в котором создается ClientHandler для нового подключения
                socket = server.accept(); // !...! Точка ожидания нового подключения
                new ClientHandler(this, socket); // Выполняется при появление нового подключения
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Процедура отправки всем сообщения
    public void broadcastMsg(ClientHandler sender, String msg) {
        if (msg.startsWith("/w ")) { // Именная отправка
            String[] wordMsg = msg.split("\\s");
            if (wordMsg.length >= 3) {
                String nicSender = sender.getNickname();
                String nicReceiver = wordMsg[1];
                String message = String.format("[ %s->%s ]: ", nicSender, nicReceiver);
                for (int i = 2; i < wordMsg.length; i++) {
                    message += wordMsg[i] + " ";
                }
                for (ClientHandler c : clients) {
                    if (nicReceiver.equals(c.getNickname()) || nicSender.equals(c.getNickname())) {
                        c.sendMsg(message);
                    }
                }
            }
        } else { // Отправка сообщения всем
            String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
            for (ClientHandler c : clients) {
                c.sendMsg(message);
            }
        }
    }

    // Добавление нового подключения в список клиентов
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    // Исключения подключения в список клиентов
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    // Метод искользуется для получения доступа к authService внутри ClassHandler
    public AuthService getAuthService() {
        return authService;
    }
}
