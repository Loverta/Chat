package Chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public final class MyServer {

    private static final Logger LOGGER = LogManager.getLogger(MyServer.class);

    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private AuthService authService;

    public MyServer() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();

            while (true) {

                LOGGER.info("Сервер ожидает подключения...");
                Socket socket = serverSocket.accept();
                LOGGER.info("Клиент подключился.");
                new ClientHandler(this, socket);

            }
        } catch (IOException ex) {
            LOGGER.fatal("Ошибка в работе сервера.");
        }finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nick) {

        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;

    }

    public synchronized void privateMsg(ClientHandler from, String nick, String msg) {
        for (ClientHandler o : clients) {
            if(o.getName().equals(nick)) {
                o.sendMsg("от " + from + ": " + msg);
                from.sendMsg("клиенту " + nick + ": " + msg);
                return;
            }
        }
        from.sendMsg("клиента с именем " + nick + " сейчас нет за чат-столом");
    }

    public synchronized void broadcastClientsList() {
        String str = "/clients ";
        for (ClientHandler o : clients) {
            str += (o.getName() + " ");
        }
        broadcastMsg(str.toString());
    }

    public synchronized void broadcastMsg(String msg) {

        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }

    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        authService = new BaseAuthService();
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientsList();
    }

}
