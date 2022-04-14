package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Client extends Application {

    String name = ""; // stores name of user

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 'localhost' would be replaced by the IP address of the user hosting the server
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

        // creating big text area that will serve as the chatroom
        TextArea chatArea = new TextArea();
        chatArea.setWrapText(true);
        chatArea.editableProperty().setValue(false);
        chatArea.setPrefColumnCount(400);
        chatArea.setPrefRowCount(400);

        Label usernameLBL = new Label("Choose a username: ");
        TextField usernameTF = new TextField();

        TextField messageTF = new TextField();
        messageTF.setPrefWidth(400);  // making text field longer

        Button confirmButton = new Button("Confirm");

        // creating button that lets user upload a text file
        Button uploadButton = new Button();
        uploadButton.setPrefSize(10,10);

        // setting image for the upload button
        Image uploadImg = new Image("file:icons/uploadIcon.png");
        ImageView uploadImageView = new ImageView(uploadImg);
        uploadImageView.setFitHeight(10);
        uploadImageView.setFitWidth(10);
        uploadImageView.setPreserveRatio(true);

        uploadButton.setGraphic(uploadImageView);

        // creating button that lets user send message
        Button sendButton = new Button("");
        sendButton.setPrefSize(10,10);

        // setting image for the send button
        Image sendImg = new Image("file:icons/sendIcon.jpg");
        ImageView sendImageView = new ImageView(sendImg);
        sendImageView.setFitHeight(15);
        sendImageView.setFitWidth(15);
        sendImageView.setPreserveRatio(true);

        sendButton.setGraphic(sendImageView);

        // HBox where the user can message and send as well as upload a file
        HBox hBoxMessage = new HBox();
        hBoxMessage.setPadding(new Insets(10));
        hBoxMessage.getChildren().addAll(uploadButton,messageTF,sendButton);
        hBoxMessage.setSpacing(20);

        // creating an options menu
        Menu menu = new Menu("Options");

        // setting image for options menu
        Image menuGearImg = new Image("file:icons/gearIcon.png");
        ImageView menuImageView = new ImageView(menuGearImg);
        menuImageView.setFitHeight(15);
        menuImageView.setFitWidth(15);
        menuImageView.setPreserveRatio(true);

        menu.setGraphic(menuImageView);

        // setting image for rename option
        Image renameImg = new Image("file:icons/renameIcon.png");
        ImageView renameImgView = new ImageView(renameImg);
        renameImgView.setFitHeight(15);
        renameImgView.setFitWidth(15);
        renameImgView.setPreserveRatio(true);

        // setting image for leave option
        Image leaveImg = new Image("file:icons/leaveIcon.png");
        ImageView leaveImgView = new ImageView(leaveImg);
        leaveImgView.setFitHeight(15);
        leaveImgView.setFitWidth(15);
        leaveImgView.setPreserveRatio(true);

        // setting image for save option (lets user save current chat into text file)
        Image saveImg = new Image("file:icons/saveIcon.png");
        ImageView saveImgView = new ImageView(saveImg);
        saveImgView.setFitHeight(15);
        saveImgView.setFitWidth(15);
        saveImgView.setPreserveRatio(true);

        // creating and adding options
        MenuItem rename = new MenuItem("Rename", renameImgView);
        MenuItem exit = new MenuItem("Leave",leaveImgView);
        MenuItem saveText = new MenuItem("Save Text", saveImgView);

        menu.getItems().add(rename);
        menu.getItems().add(saveText);
        menu.getItems().add(exit);

        // creating the menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        // Saving the chat log into a text file when the save option is pressed
        saveText.setOnAction(e ->{
            // opening file chooser to let user save the text file
            FileChooser fileOpen = new FileChooser();
            fileOpen.setTitle("Open");
            fileOpen.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt Files","*.txt"));
            File selectedSaveFile = fileOpen.showSaveDialog(primaryStage);

            // writing to text file
            FileWriter file = null;
            try {
                file = new FileWriter(selectedSaveFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            BufferedWriter output = new BufferedWriter(file);
            try {
                output.write(chatArea.getText());
                output.flush();
                output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Uploading the contents of text file into chat room
        uploadButton.setOnAction(e ->{
            String line = null;
            // opening file chooser to let user upload text file
            FileChooser fileOpen = new FileChooser();
            fileOpen.setTitle("Open");
            fileOpen.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt Files","*.txt"));
            File selectedOpenFile = fileOpen.showOpenDialog(primaryStage);

            // reading in text file
            BufferedReader input = null;
            try {
                input = new BufferedReader(new FileReader(selectedOpenFile));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            while (true) {
                try {
                    if (input != null && ((line = input.readLine()) == null)) break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // print to every user connected to the chatroom
                pWriter.println(name + ": " + line);
            }
            try {
                input.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // task that handles sending and receiving messages from the server
        Task<String> sendTexts = new Task<>() {
            @Override
            protected String call() throws Exception {

                // loops as long as user is connected
                while (sock.isConnected())
                {
                    // sends message to the server when send button is pressed
                    sendButton.setOnAction(e -> pWriter.println(name + ": " + messageTF.getText()));
                    messageTF.clear();

                    // receive messages from server to display on the chatroom
                    String line;
                    if((line = bufferedReader.readLine()) != null)
                    {
                        chatArea.appendText(line + " \n");
                    }
                }

                return null;
            }
        };

        // starts thread for socket
        Thread t = new Thread(sendTexts);
        t.setDaemon(true);
        t.start();

        // if user closes window instead of using exit button, redirects to exit button event handler so socket is closed
        primaryStage.setOnCloseRequest(we -> exit.fire());

        // lets user press the enter key to send messages by redirecting to the send button event handler
        messageTF.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                sendButton.fire();
            }
        });

        // lets user press the enter key to confirm their username by redirecting to the confirm button event handler
        usernameTF.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                confirmButton.fire();
            }
        });


        // closes socket and closes the window when exit button is pressed
        exit.setOnAction(e -> {
            primaryStage.close();
            try {
                sock.close(); // close socket when exiting the UI so a new client is able to connect
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // setting image of stage
        Image stageIcon = new Image("file:icons/chatIcon.png");
        primaryStage.getIcons().add(stageIcon);

        // Naming UI where users will be able to choose/change their username
        // this scene is shown when user first connects
        HBox renameUI = new HBox(usernameLBL, usernameTF, confirmButton);
        renameUI.setAlignment(Pos.CENTER);
        renameUI.setSpacing(20);
        Scene renameScene = new Scene(renameUI);
        primaryStage.setScene(renameScene);
        primaryStage.setWidth( 400 );
        primaryStage.setHeight( 200 );
        primaryStage.show();

        // Main UI where users will be able to chat
        VBox chatroomVbox = new VBox();
        chatroomVbox.getChildren().addAll(menuBar,chatArea,hBoxMessage);
        primaryStage.setTitle("Chatroom");
        Scene mainScene = new Scene(chatroomVbox);

        // switches scene to Naming UI where users will be able to change their username
        rename.setOnAction(e -> {
            primaryStage.setScene(renameScene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(200);
            primaryStage.show();
        });

        // button that lets users confirm their chosen username
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