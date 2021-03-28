package serverside.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    
    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private ExecutorService executorService;
    private String name;
    
    
    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            executorService = Executors.newFixedThreadPool(1);
            executorService.execute(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException ignored) {
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException e) {
            closeConnection();
            throw new RuntimeException("Problem with ClientHandler");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String str = dis.readUTF();
            if (str.startsWith("/auth")) { //  /auth login password
                String [] arr = str.split("\\s");
                String nick = myServer
                        .getAuthService()
                        .getNickByLoginAndPassword(arr[1], arr[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMessage("/authok " + nick);
                        name = nick;
                        myServer.broadcastMessage("Hello " + name);
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMessage("Nick is busy");
                    }
                }
            } else {
                sendMessage("Wrong login and password");
            }
        }
    }

    public void readMessage() throws IOException {
        while (true) {
            String messageFromClient = dis.readUTF();
            System.out.println(name + " send message " + messageFromClient);
            if (messageFromClient.equals("/end")) {
                return;
            }
            if(messageFromClient.startsWith("/w")){
                String [] arr = messageFromClient.split("\\s");
                ClientHandler client = myServer.getClientByNick(arr[1]);
                if(client != null){
                   myServer.privateMessage(arr[2],client);
                   continue;
                }
            }
            myServer.broadcastMessage(name + ": " + messageFromClient);
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException ignored) {
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMessage(name + " Leave chat");
        try {
            dis.close();
            dos.close();
            socket.close();
            executorService.shutdown();
        } catch (IOException ignored) {
        }
    }

    public String getName() {
        return name;
    }
}
