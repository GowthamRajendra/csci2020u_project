package com.example.lab10;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client extends Application {

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message

        GridPane gp = new GridPane();
        gp.setPadding( new Insets(20) );
        gp.setHgap( 10 );
        gp.setVgap( 10 );

        Label usernameLBL = new Label("Username: ");
        TextField usernameTF = new TextField();

        Label messageLBL = new Label("Message:");
        TextField messageTF = new TextField();

        Button sendButton = new Button("Send");
        Button exitButton = new Button("Exit");

        sendButton.setOnAction(e -> {
            pWriter.println(usernameTF.getText() + ": " + messageTF.getText());
        });

        exitButton.setOnAction(e -> {
            primaryStage.close();
            try {
                sock.close(); // close socket when exiting the ui so a new client is able to connect.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        gp.add(usernameLBL, 0, 0);
        gp.add(usernameTF, 1, 0);
        gp.add(messageLBL, 0, 1);
        gp.add(messageTF, 1, 1);
        gp.add(sendButton, 0, 2);
        gp.add(exitButton, 0, 3);

        primaryStage.setTitle("Lab 10 Client Ravichandra Pogaku 100784105");
        Scene scene = new Scene(gp);
        primaryStage.setScene(scene);
        primaryStage.setWidth( 300 );
        primaryStage.setHeight( 200 );
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
