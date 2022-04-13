package com.example.lab10;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.concurrent.Service;

import java.net.*;
import java.io.*;

public class Server extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // task that connects new clients and updates the ui with messages sent by clients.
        Task connectAndUpdate = new Task() {
            @Override
            protected Object call() throws IOException {
                ServerSocket serverSock = new ServerSocket(6666);
                serverSock.setReuseAddress(true);
                System.out.println("Starting server...");
                System.out.println("Waiting for a client connection...");

                while (true) {
                    Socket clientSock = serverSock.accept();
                    //this will display the host address of client
                    System.out.println("Client is connected " + clientSock.getInetAddress().getHostAddress());

                    try (BufferedReader inStream = new BufferedReader(new InputStreamReader(clientSock.getInputStream()))) {
                        String line = "";
                        while ((line = inStream.readLine()) != null) {
                            line = line + "\n";
                            updateValue(line);
                        }
                    }

                    if (clientSock.isClosed()) {
                        System.out.println("Client is disconnected");
                    }
                }
            }
        };

        Thread connectThread = new Thread(connectAndUpdate);
        connectThread.setDaemon(true);
        connectThread.start();

        GridPane gp = new GridPane();
        gp.setPadding( new Insets(20) );
        gp.setHgap( 10 );
        gp.setVgap( 10 );

        TextArea messagesTA = new TextArea();

        Button exitButton = new Button("Exit");

        exitButton.setOnAction(e -> {
            primaryStage.close();
            connectThread.interrupt();
        });

        gp.add(messagesTA, 0, 0);
        gp.add(exitButton, 0, 1);

        primaryStage.setTitle("Lab 10 Server Ravichandra Pogaku 100784105");
        Scene scene = new Scene(gp);
        primaryStage.setScene(scene);
        primaryStage.setWidth( 400 );
        primaryStage.setHeight( 300 );
        primaryStage.show();

        // new messages sent update the value of the Task, listens for changes and updates text area accordingly
        connectAndUpdate.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                messagesTA.setText(messagesTA.getText() + newValue);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
