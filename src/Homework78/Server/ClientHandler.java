package Homework78.Server;
/*
    Класс для создание отдельный экземпляров подключений для каждого клиента.
    В данном задание сервер получается от одного из клиентов сообщение (в каком-то ClientHandler из списка clients
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Подключился клиент, сокет: " + socket.getRemoteSocketAddress());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(5000); // Таймаут на отключение клиента, если клиент не зарегистрирован
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                nickname = newNick;
                                server.subscribe(this);
                                sendMsg("/authok " + newNick);
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }
                    socket.setSoTimeout(0); // Отключение таймаута, так как клиент залогинился
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }
                        server.broadcastMsg(this, str);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Отключение клиента по времени бездействия, сокет: " + socket.getRemoteSocketAddress());
                    sendMsg("Время соединения превышено, вы отключены");
                    sendMsg("/end");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // ЗАВЕРШЕНИЕ СВЯЗИ
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился, сокет: " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
