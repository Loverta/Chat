package Chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ClientHandler {

    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private MyServer myServer;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private DataBase dataBase;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {

        try {


            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            executorService.execute(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    closeConnection();
                }
            });

        } catch (IOException ex) {
            LOGGER.error("Проблемы при создании обработчика клиента.");
            throw new RuntimeException();
        }

    }

    public void authentication() throws IOException{

        while (true) {

            String str = in.readUTF();

            if (str.startsWith("/auth")) {

                LOGGER.info("Клиент прислал команду: " + str);
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);

                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {

                        sendMsg("/authok" + nick);
                        name = nick;
                        myServer.broadcastMsg(name + " Зашел в чат");
                        myServer.subscribe(this);
                        return;

                    } else LOGGER.error("Учетная запись уже используется.");
                } else LOGGER.error("Неверное имя пользователя/пароль.");
            }
        }
    }

    public void sendMsg(String msg) {

        try {
            out.writeUTF(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void readMessages() throws IOException{
        while (true) {

            String strFromClient = in.readUTF();

            if (strFromClient.startsWith("/")) {
                LOGGER.info("Клиент прислал команду: " + strFromClient);

                if (strFromClient.equals("/end")) {
                    break;
                }
                if (strFromClient.startsWith("/w")) {

                    String[] parts = strFromClient.split("\\s");
                    String nick = parts[1];
                    String msg = strFromClient.substring(4 + nick.length());

                    myServer.privateMsg(this, nick, msg);
                }
                if (strFromClient.startsWith("/nick")) {

                    dataBase = new DataBase();
                    String[] parts = strFromClient.split("\\s");
                    String nick = parts[1];

                    try {
                        out.writeUTF("Ваш никнейм обновлен, для того, чтобы изменения вошли в силу, нужно перезайти.");
                        dataBase.connect();
                        dataBase.updateEx(this.name, nick);
                    } catch (SQLException | IOException ex ) {
                        ex.printStackTrace();
                    }
                }
                continue;
            }
            myServer.broadcastMsg(name + ": " + strFromClient);
        }
    }

    public void closeConnection() {

        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataBase.disconnect();

    }
}
