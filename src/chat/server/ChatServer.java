/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Igor Gayvan
 */
public class ChatServer implements Runnable {

    private static final int SERVER_TIMEOUT = 1000;

    private int port;
    private ServerSocket serverSocket;
    private boolean listen;
    
    private List<ChatMessageListener> chatMessageListeners;

    public ChatServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setSoTimeout(SERVER_TIMEOUT);
        
        this.chatMessageListeners = new ArrayList<>();
    }
    
    public boolean addChatMessageListener(ChatMessageListener chatMessageListener) {
        return chatMessageListeners.add(chatMessageListener);
    }

    @Override
    public void run() {
        listen = true;

        System.out.println("SERVER LISTEN ON " + port);
        while (listen) {
            try (Socket accept = serverSocket.accept()) {
                // CONFIG SOCKET
                accept.setSoLinger(true, SERVER_TIMEOUT / 2); // Waiting before close connection
                accept.setSoTimeout(SERVER_TIMEOUT); // Waiting before throws SocketTimeoutException when read data.

                // Print connected ip address.
                String clientAddress = accept.getInetAddress().getHostAddress();
                System.out.println("CONNECTED: " + clientAddress);

                // GET IO
                DataInputStream dis = new DataInputStream(accept.getInputStream());
                DataOutputStream dos = new DataOutputStream(accept.getOutputStream());

                commandHandler(dis, dos, accept.getPort());
            } catch (SocketTimeoutException ex) {
                // NOOP
            } catch (IOException ex) {
                Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Delegate command.
     *
     * @param dis input stream.
     * @param dos output stream.
     * @throws IOException
     */
    public void commandHandler(DataInputStream dis, DataOutputStream dos, int clientPort) throws IOException {
        // WORKING WITH SOCKET
        String command = dis.readUTF();
        System.out.println("COMMAND: " + command);

        switch (command) {
            case "PING":
                pingHandler(dos);
                break;
            case "MSG":
                messageHandler(dis, dos, clientPort);
                break;
            default:
                defaultHandler(dos);
                break;
        }
    }

    public void pingHandler(DataOutputStream dos) throws IOException {
        dos.writeUTF("PONG");
        dos.flush();
    }

    public void messageHandler(DataInputStream dis, DataOutputStream dos, int clientPort) throws IOException {
        String sender = dis.readUTF();
        String message = dis.readUTF();
        Date timestamp = new Date();
        
        for (ChatMessageListener chatMessageListener : chatMessageListeners) {
            chatMessageListener.onMessage(sender, message, timestamp);
        }
        
        dos.writeUTF("OK");
        dos.flush();
    }

    public void defaultHandler(DataOutputStream dos) throws IOException {
        dos.writeUTF("ERROR:Unknown command!");
        dos.flush();
    }

    /**
     * Set {@link #listen} to false.
     */
    public void stop() {
        listen = false;
    }

}
