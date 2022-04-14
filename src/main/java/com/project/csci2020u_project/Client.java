package com.project.csci2020u_project;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    public Client() {}

    @Override
    public void start(Stage primaryStage) throws IOException {
        Socket sock = new Socket("localhost", 6666);
        PrintWriter pWriter = new PrintWriter(sock.getOutputStream(), true); // output username and message
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

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

        Button uploadButton = new Button();
        uploadButton.setPrefSize(10,10);

        Path path = Paths.get("uploadIcon.png");

        Image img = new Image("file:uploadIcon.png");
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(10);
        imageView.setFitWidth(10);
        imageView.setPreserveRatio(true);

        uploadButton.setGraphic(imageView);

        VBox vBox = new VBox();

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);

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
                    sendButton.setOnAction(e -> pWriter.println(usernameTF.getText() + ": " + messageTF.getText()));
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


        vBox.getChildren().addAll(menuBar,textArea,hBoxMessage);
        primaryStage.setTitle("Client");
        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.setWidth( 550 );
        primaryStage.setHeight( 500 );
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
