package com.project.csci2020u_project;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
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
        messageTF.setPrefWidth(400);

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

        HBox hBoxMessage = new HBox();
        hBoxMessage.setPadding(new Insets(10));
        hBoxMessage.getChildren().addAll(uploadButton,messageTF,sendButton);
        hBoxMessage.setSpacing(20);

        Menu menu = new Menu("Options");

        MenuItem rename = new MenuItem("Rename");
        MenuItem exit = new MenuItem("Exit");
        MenuItem savetxt = new MenuItem("Save Text");

        menu.getItems().add(rename);
        menu.getItems().add(savetxt);
        menu.getItems().add(exit);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        // Saving the chat log into a .txt file
        savetxt.setOnAction(e ->{
            FileChooser fileOpen = new FileChooser();
            fileOpen.setTitle("Open");
            fileOpen.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt Files","*.txt"));
            File selectedSaveFile = fileOpen.showSaveDialog(primaryStage);

            File path = selectedSaveFile;
            FileWriter file = null;
            try {
                file = new FileWriter(path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            BufferedWriter output = new BufferedWriter(file);
            try {
                output.write(textArea.getText());
                output.flush();
                output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Uploading the contents of a .txt file into the chat room
        uploadButton.setOnAction(e ->{
            String line = null;
            String txtMsg = null;
            FileChooser fileOpen = new FileChooser();
            fileOpen.setTitle("Open");
            fileOpen.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt Files","*.txt"));
            File selectedSaveFile = fileOpen.showOpenDialog(primaryStage);

            File path = selectedSaveFile;
            BufferedReader input = null;
            try {
                input = new BufferedReader(new FileReader(path));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            while (true) {
                try {
                    if (((line = input.readLine()) == null)) break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                pWriter.println(name + ": " + line); // print to clients
            }
            try {
                input.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Task<String> sendTexts = new Task<>() {
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

        Thread t = new Thread(sendTexts);
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