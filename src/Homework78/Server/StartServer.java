package Homework78.Server;

/*
 Задание 7 урока:  Реализовать личные сообщения так: если клиент пишет «/w nick3 Привет»,
 то только клиенту с ником nick3 и самому отправителю должно прийти сообщение «Привет».
 */
public class StartServer {
    public static void main(String[] args) {
        new Server(); // Сделан отдельный класс сервер, так как нельзя в статическом классе использовать this в ClientHandler
    }
}
