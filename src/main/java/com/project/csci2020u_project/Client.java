package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client extends Application {

    String name = "";

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);
        //textArea.setPrefHeight(1200.0);

        Label usernameLBL = new Label("Username: ");
        TextField usernameTF = new TextField();

        TextField messageTF = new TextField();

        Button sendButton = new Button("Send");
        Button confirmButton = new Button("Confirm");

        Button uploadButton = new Button();
        uploadButton.setPrefSize(10,10);

        Image img = new Image("file:uploadIcon.png");
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(10);
        imageView.setFitWidth(10);
        imageView.setPreserveRatio(true);

        uploadButton.setGraphic(imageView);

        VBox vBox = new VBox();

        messageTF.setPrefWidth(400);

        HBox hBoxMessage = new HBox();
        hBoxMessage.setPadding(new Insets(10));
        hBoxMessage.getChildren().addAll(uploadButton,messageTF,sendButton);
        hBoxMessage.setSpacing(20);

        Menu menu = new Menu("Options");

        MenuItem rename = new MenuItem("Rename");
        MenuItem exit = new MenuItem("Exit");

        menu.getItems().add(rename);
        menu.getItems().add(exit);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

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
                        textArea.appendText(line + " \n");
                        System.out.println(line);
                    }
                }


                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

        primaryStage.setOnCloseRequest(we -> exit.fire());

        messageTF.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                sendButton.fire();
            }
        });

        exit.setOnAction(e -> {
            primaryStage.close();
            try {
                sock.close(); // close socket when exiting the ui so a new client is able to connect.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Naming UI (Default for choose name first)
        HBox renameUI = new HBox(usernameLBL, usernameTF, confirmButton);
        Scene renameScene = new Scene(renameUI);
        primaryStage.setScene(renameScene);
        primaryStage.setWidth( 400 );
        primaryStage.setHeight( 400 );
        primaryStage.show();

        // Main UI
        vBox.getChildren().addAll(menuBar,textArea,hBoxMessage);
        primaryStage.setTitle("Client");
        Scene mainScene = new Scene(vBox);

        rename.setOnAction(e -> {
            primaryStage.setScene(renameScene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(400);
            primaryStage.show();
        });

        confirmButton.setOnAction(e -> {
            name = usernameTF.getText();
            primaryStage.setScene(mainScene);
            primaryStage.setWidth( 550 );
            primaryStage.setHeight( 500 );
            primaryStage.setResizable(false);
            primaryStage.show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
