package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {

    String name = "";

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        GridPane gp1 = new GridPane();
        GridPane gp2 = new GridPane();
        gp1.setPadding( new Insets(20) );
        gp1.setHgap( 10 );
        gp1.setVgap( 10 );

        gp2.setPadding( new Insets(20) );
        gp2.setHgap( 10 );
        gp2.setVgap( 10 );

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);
        //textArea.setPrefHeight(1200.0);

        Label usernameLBL = new Label("Username: ");
        TextField usernameTF = new TextField();

        Label messageLBL = new Label("Message:");
        TextField messageTF = new TextField();

        Button sendButton = new Button("Send");
        Button confirmButton = new Button("Confirm");
        Button renameButton = new Button("Rename");
        Button exitButton = new Button("Exit");


        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {

                while (sock.isConnected())
                {
                    sendButton.setOnAction(e -> pWriter.println(name + ": " + messageTF.getText()));
                    messageTF.clear();

                    String line;
                    if((line = bufferedReader.readLine()) != null)
                    {
                        textArea.appendText(line + "\n");
                    }
                }


                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                exitButton.fire();
            }
        });

        exitButton.setOnAction(e -> {
            primaryStage.close();
            try {
                sock.close(); // close socket when exiting the ui so a new client is able to connect.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        gp1.add(textArea, 0, 0);
        gp2.add(usernameLBL, 0, 1);
        gp2.add(usernameTF, 1, 1);
        gp2.add(messageLBL, 0, 2);
        gp2.add(messageTF, 1, 2);
        gp2.add(sendButton, 2, 2);
        gp2.add(exitButton, 0, 4);
        gp2.add(renameButton, 1, 4);

        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);

        // Naming UI (Default for choose name first)
        HBox renameUI = new HBox(usernameLBL, usernameTF, confirmButton);
        Scene renameScene = new Scene(renameUI);
        primaryStage.setScene(renameScene);
        primaryStage.setWidth( 400 );
        primaryStage.setHeight( 400 );
        primaryStage.show();

        // Main UI
        VBox vBox = new VBox(gp1, gp2);
        primaryStage.setTitle("Client");
        Scene mainScene = new Scene(vBox);

        renameButton.setOnAction(e -> {
            primaryStage.setScene(renameScene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(400);
            primaryStage.show();
        });

        confirmButton.setOnAction(e -> {
            name = usernameTF.getText();
            primaryStage.setScene(mainScene);
            primaryStage.setWidth( 1000 );
            primaryStage.setHeight( 1000 );
            primaryStage.show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
