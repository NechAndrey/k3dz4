package clientside.one;

import serverside.service.HistoryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoClient extends JFrame {

    private final Integer SERVER_PORT = 8081;
    private final String SERVER_ADDRESS = "localhost";
    private Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    boolean isAuthorized = false;
    private List<String> historyMessage = new ArrayList<>();
    private HistoryService historyService = new HistoryService();
    private List<String> censorList = historyService.Censor();
    private ExecutorService executorService;
    private JTextField msgInputField;
    private JTextArea chatArea;

    public EchoClient() {
        try {
            connection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        prepareGUI();
    }

    public void connection() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            try {
                while (true) {
                    String messageFromServer = dis.readUTF();
                    if (messageFromServer.startsWith("/authok")) {
                        isAuthorized = true;
                        chatArea.append(messageFromServer + "\n");
                        List<String> list = historyService.loadHistory();
                        for (String s : list) {
                            chatArea.append(s + "\n");
                        }
                        break;
                    }
                    chatArea.append(messageFromServer + "\n");
                    for (String s : censorList) {
                        messageFromServer = messageFromServer.replace(s.toLowerCase(), s);
                    }
                    historyMessage.add(messageFromServer);
                }

                while (isAuthorized) {
                    String messageFromServer = dis.readUTF();
                    for (String s : censorList) {
                        messageFromServer = messageFromServer.replace(s.toLowerCase(), s);
                    }
                    chatArea.append(messageFromServer + "\n");
                    historyMessage.add(messageFromServer);
                }
            } catch (IOException ignored) {

            }
        });
       /* new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = dis.readUTF();
                    if (messageFromServer.startsWith("/authok")) {
                        isAuthorized = true;
                        chatArea.append(messageFromServer + "\n");
                        List<String> list = historyService.loadHistory();
                        for(String s : list){
                            chatArea.append(s + "\n");
                        }
                        break;
                    }
                    chatArea.append(messageFromServer + "\n");
                    for(String s:censorList){
                        messageFromServer = messageFromServer.replace(s.toLowerCase(),s);
                    }
                    historyMessage.add(messageFromServer);
                }

                while (isAuthorized) {
                    String messageFromServer = dis.readUTF();
                    for(String s:censorList){
                        messageFromServer = messageFromServer.replace(s.toLowerCase(),s);
                    }
                    chatArea.append(messageFromServer + "\n");
                    historyMessage.add(messageFromServer);
                }
            } catch (IOException ignored) {

            }
        }).start();*/
    }

    public void send() {
        if (msgInputField.getText() != null && !msgInputField.getText().trim().isEmpty()) {
            try {
                dos.writeUTF(msgInputField.getText());
                if (msgInputField.getText().equals("/end")) {
                    isAuthorized = false;
                    closeConnection();
                }
                msgInputField.setText("");
            } catch (IOException ignored) {
            }
        }
    }

    private void closeConnection() {
        try {
            historyService.SaveHistory(historyMessage);
            dis.close();
            dos.close();
            socket.close();
            executorService.shutdown();
        } catch (IOException ignored) {
        }
    }

    /*public void onAuthClcik() {
        try {
            dos.writeUTF("/auth" + " " + loginField.getText() + " " + passwordField.getText());
            loginField.setText("");
            passwordField.setText("");
        } catch (IOException ignored) {
        }
    }*/


    public void prepareGUI() {

        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);

        btnSendMsg.addActionListener(e -> {
            send();
        });

        msgInputField.addActionListener(e -> {
            send();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EchoClient();
        });
    }
}