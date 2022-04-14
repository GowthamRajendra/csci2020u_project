package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);
        //textArea.setPrefHeight(1200.0);

        Label usernameLBL = new Label("Choose a username: ");
        TextField usernameTF = new TextField();

        TextField messageTF = new TextField();

        Button sendButton = new Button("");
        sendButton.setPrefSize(10,10);
        Button confirmButton = new Button("Confirm");

        Button uploadButton = new Button();
        uploadButton.setPrefSize(10,10);

        Image img = new Image("file:icons/uploadIcon.png");
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(10);
        imageView.setFitWidth(10);
        imageView.setPreserveRatio(true);

        uploadButton.setGraphic(imageView);

        Image sendImg = new Image("file:icons/sendIcon.jpg");
        ImageView sendImageView = new ImageView(sendImg);
        sendImageView.setFitHeight(15);
        sendImageView.setFitWidth(15);
        sendImageView.setPreserveRatio(true);

        sendButton.setGraphic(sendImageView);

        VBox vBox = new VBox();

        messageTF.setPrefWidth(400);

        HBox hBoxMessage = new HBox();
        hBoxMessage.setPadding(new Insets(10));
        hBoxMessage.getChildren().addAll(uploadButton,messageTF,sendButton);
        hBoxMessage.setSpacing(20);

        Menu menu = new Menu("Options");

        // creating icon for menu
        Image menuGearImg = new Image("file:icons/gearIcon.png");
        ImageView menuImageView = new ImageView(menuGearImg);
        menuImageView.setFitHeight(15);
        menuImageView.setFitWidth(15);
        menuImageView.setPreserveRatio(true);

        menu.setGraphic(menuImageView);

        Image renameImg = new Image("file:icons/renameIcon.png");
        ImageView renameImgView = new ImageView(renameImg);
        renameImgView.setFitHeight(15);
        renameImgView.setFitWidth(15);
        renameImgView.setPreserveRatio(true);

        Image leaveImg = new Image("file:icons/leaveIcon.png");
        ImageView leaveImgView = new ImageView(leaveImg);
        leaveImgView.setFitHeight(15);
        leaveImgView.setFitWidth(15);
        leaveImgView.setPreserveRatio(true);

        MenuItem rename = new MenuItem("Rename", renameImgView);
        MenuItem exit = new MenuItem("Exit",leaveImgView);

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

        usernameTF.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                confirmButton.fire();
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

        // setting icon of stage
        Image stageIcon = new Image("file:icons/chatIcon.png");
        primaryStage.getIcons().add(stageIcon);

        // Naming UI (Default for choose name first)
        HBox renameUI = new HBox(usernameLBL, usernameTF, confirmButton);
        renameUI.setAlignment(Pos.CENTER);
        renameUI.setSpacing(20);
        Scene renameScene = new Scene(renameUI);
        primaryStage.setScene(renameScene);
        primaryStage.setWidth( 400 );
        primaryStage.setHeight( 200 );
        primaryStage.show();

        // Main UI
        vBox.getChildren().addAll(menuBar,textArea,hBoxMessage);
        primaryStage.setTitle("Chatroom");
        Scene mainScene = new Scene(vBox);

        rename.setOnAction(e -> {
            primaryStage.setScene(renameScene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(200);
            primaryStage.show();
        });

        confirmButton.setOnAction(e -> {
            name = usernameTF.getText();
            primaryStage.setScene(mainScene);
            primaryStage.setWidth( 500 );
            primaryStage.setHeight( 500 );
            primaryStage.setResizable(false);
            primaryStage.show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
