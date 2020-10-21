package Homework78.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private HBox authPanel;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox msgPanel;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Stage stage;

    private boolean authenticated;
    private String nickname;

    // Метод перерисовки в зависимости от того прошла ли идентификация или нет
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
            setTitle("Балабол");
        } else {
            setTitle(String.format("[ %s ] - Балабол", nickname));
            textArea.clear();
        }

    }

    // Открытие окна
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow(); // Используется в методе изменения заголовка окна
        });
        setAuthenticated(false);
    }

    // Выполнение соединения после нажатия SignIn
    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF(); // !..! Ожидание пакета от сервера
                        if (str.startsWith("/")) {
                            if (str.startsWith("/authok ")) {
                                nickname = str.split("\\s")[1];
                                setAuthenticated(true);
                                break;
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF(); // !..! Ожидание пакета от сервера
                        if (str.equals("/end")) {
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                } catch (EOFException e) {
                    textArea.appendText("Соединение с сервером прервано");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
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

    // Нажатие клавиши "Send"
    public void sendMsg(ActionEvent actionEvent) {
        if (textField.getText().trim().length() == 0) {
            return;
        }
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Нажатие клавиши "Sign In"
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Изменение заголовка окна
    private void setTitle(String title) {
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }
}
