package Chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class Client3 extends JFrame{

    private DataInputStream in;
    private BufferedReader inFile;
    private DataOutputStream out;
    private DataOutputStream outFile;
    private Socket socket;
    private long startTime;
    private boolean isAuth;

    private JTextField msgInputField;
    private JTextArea chatArea;
    private JTextField login;
    private JTextField pass;


    public Client3() {
        try {
            openConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        prepareGUI();
        setAuthorized();
    }

    public void openConnection() throws IOException {

        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        outFile = new DataOutputStream(new FileOutputStream("ChatHistory.txt", true));
        inFile = new BufferedReader(new FileReader("ChatHistory.txt"));
        isAuth = false;

        checkFileSize();

        startTime = System.currentTimeMillis();



        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if(strFromServer.startsWith("/authok")) {
                            isAuth = true;
                            break;
                        }
                    }
                    while (true) {
                        String strFromServer = in.readUTF();

                        if (strFromServer.equalsIgnoreCase("/end")) {
                            closeConnection();
                            break;
                        }

                        byte[] outData = strFromServer.getBytes(StandardCharsets.UTF_8);
                        outFile.write(outData);
                        outFile.writeBytes("\n");

                        chatArea.append(strFromServer + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void setAuthorized() {

        new Thread(() -> {
            while (true) {
                if ((System.currentTimeMillis() - startTime) > 120_000) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }).start();

        JFrame authFrame = new JFrame();

        authFrame.setBounds(100, 100, 300, 200);
        authFrame.setTitle("Авторизация.");
        authFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        authFrame.setLayout(null);

        login = new JTextField();
        pass = new JTextField();
        JButton button = new JButton("Вход");

        login.setBounds(20, 20, 120, 32);
        pass.setBounds(20, 60, 120, 32);
        button.setBounds(20, 100, 120, 32);

        authFrame.add(login);
        authFrame.add(pass);
        authFrame.add(button);

        button.addActionListener(e -> {
            sendLoginPas();
        });

        authFrame.setVisible(true);

    }

    public void prepareGUI() {

        JFrame GUI = new JFrame();

        GUI.setBounds(600, 300, 500, 500);
        GUI.setTitle("Клиент3");
        GUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        GUI.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        String str;
        try {
            while ((str = inFile.readLine()) != null) {
                chatArea.append(str + "\n");
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }



        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        GUI.add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);

        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        GUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF("/end");
                    closeConnection();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        });

        GUI.setVisible(true);

    }

    public void sendMessage() {

        if (!msgInputField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(msgInputField.getText());
                msgInputField.setText("");
                msgInputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }


    }

    public void sendLoginPas() {

        try {

            if (socket == null || socket.isClosed()) {
                openConnection();
            }

            String str = "/auth ";
            str = str + (login.getText().trim() + " " + pass.getText().trim());
            login.setText("");
            pass.setText("");
            out.writeUTF(str);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            outFile.close();
            socket.close();
            inFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void checkFileSize() {
        try {

            BufferedReader reader = new BufferedReader(new FileReader("ChatHistory.txt"));
            ArrayList<String> strIn = new ArrayList();
            String str;
            int count = 0;

            while ((str = reader.readLine()) != null) {
                strIn.add(str.trim());
                count++;
            }

            if (count > 100) {

                DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("ChatHistory.txt"));
                count -= 100;

                for (; count < strIn.size(); count++) {

                    byte[] bytes = strIn.get(count).getBytes(StandardCharsets.UTF_8);
                    outputStream.write(bytes);
                    outputStream.writeBytes("\n");
                }
                outputStream.close();
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        new Client3();

    }
}
