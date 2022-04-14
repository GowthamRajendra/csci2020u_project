package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Client extends Application {

    String name = "";
    int messageCount = 0;
    Date date = new Date();

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        String loginDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);
        //textArea.setPrefHeight(1200.0);

        Label usernameLBL = new Label("Choose a username: ");
        TextField usernameTF = new TextField();

        TextField messageTF = new TextField();
        messageTF.setPrefWidth(400);

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
        MenuItem savetxt = new MenuItem("Save Text");
        MenuItem stats = new MenuItem("User Stats");

        menu.getItems().addAll(rename, savetxt, stats, exit);

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
                    if (input != null && ((line = input.readLine()) == null)) break;
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

        // JavaFX Task that sends messages
        Task<Integer> sendTexts = new Task<>() {
            @Override
            protected Integer call() throws Exception {

                while (sock.isConnected())
                {
                    sendButton.setOnAction(e -> pWriter.println(name + ": " + messageTF.getText()));
                    messageTF.clear();
                    updateValue(messageCount++);

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

        // sendTexts has seperate thread so the ui doesn't freeze up
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

        // Naming UI (Default for choosing username first)
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

        //Stats UI
        Stage statStage = new Stage();
        VBox statsUI = new VBox();
        Scene statScene = new Scene(statsUI);
        Text statMessages = new Text();
        Text statLogin = new Text("Login time: " + loginDate);
        statsUI.getChildren().addAll(statLogin, statMessages);

        rename.setOnAction(e -> {
            primaryStage.setScene(renameScene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(200);
            primaryStage.show();
        });

        stats.setOnAction(e -> {
            statStage.setScene(statScene);
            statMessages.setText("Total number of messages in chat: " + String.valueOf(sendTexts.valueProperty().getValue()));
            statStage.setWidth(400);
            statStage.setHeight(200);
            statStage.show();
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