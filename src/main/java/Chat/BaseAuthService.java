package Chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class BaseAuthService implements AuthService {

    private DataBase dataBase;
    private static final Logger LOGGER = LogManager.getLogger(BaseAuthService.class);

    private class Entry {

        private String login;
        private String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.nick = nick;
            this.pass = pass;
        }
    }

    private List<Entry> entries;

    public BaseAuthService() {

        ArrayList arrayList;

        dataBase = new DataBase();
        entries = new ArrayList<>();

        try {
            dataBase.connect();
            arrayList = dataBase.readEx();
            String[] str = new String[arrayList.size()];

            for (int i = 0; i < arrayList.size(); i++) {

                str[i] = (String) arrayList.get(i);
                String[] parts = str[i].split("\\s");
                entries.add(new Entry(parts[0], parts[1], parts[2]));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            dataBase.disconnect();
        }


    }

    @Override
    public void start() {
        LOGGER.info("Сервис аутентификации запущен...");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) {
                return o.nick;
            }
        }
        return null;
    }

    @Override
    public void stop() {
        LOGGER.info("Сервис аутентификации остановлен.");
    }


}
