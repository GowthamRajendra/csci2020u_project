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
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

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

        VBox vBox = new VBox();

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.editableProperty().setValue(false);
        textArea.setPrefColumnCount(400);
        textArea.setPrefRowCount(400);

        messageTF.setPrefWidth(400);

        HBox hBoxMessage = new HBox();
        hBoxMessage.setPadding(new Insets(10));
        hBoxMessage.getChildren().addAll(messageTF,sendButton);
        hBoxMessage.setSpacing(20);

        Menu menu = new Menu("Options");

        MenuItem rename = new MenuItem("Rename");
        MenuItem exit = new MenuItem("Exit");
        MenuItem filechooser = new MenuItem("Choose File");

        menu.getItems().add(rename);
        menu.getItems().add(exit);
        menu.getItems().add(filechooser);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        filechooser.setOnAction(e ->{
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
                    if (!!((line = input.readLine()) != null)) break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            {
                txtMsg = line;
                textArea.appendText(txtMsg + " \n");
                //Process line
            }
        });

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
        primaryStage.setWidth( 500 );
        primaryStage.setHeight( 500 );
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}