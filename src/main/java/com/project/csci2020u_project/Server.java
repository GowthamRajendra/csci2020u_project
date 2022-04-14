package com.project.csci2020u_project;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            // loop that waits for users to connect to the server
            while(true) {
                System.out.println("Waiting for users to connect...");
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress().getHostAddress() + " Connected");
                ClientHandler clientThread = new ClientHandler(socket, clients); // creates instance for new client
                clients.add(clientThread); // adds new client to list
                clientThread.start(); // starts thread for client where their messages will be handled
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}