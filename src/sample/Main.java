package sample;

import Tagger.DataManager;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Main extends Application {

    private static final String TITLE_TXT = "Home"; // the title of the first window
    private DataManager dm; // a DataManager for the home window

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Start the application with its first window primaryStage
     *
     * @param primaryStage the home window
     */
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle(TITLE_TXT);
        dm = new DataManager();
        deserialize();
        Controller controller = new Controller(dm);

        // Window label
        Label label = new Label("Photo Manager");
        label.setTextFill(Color.BLACK);
        label.setFont(Font.font("Calibri", FontWeight.BOLD, 36));
        HBox labelHb = new HBox();
        labelHb.setAlignment(Pos.CENTER);
        labelHb.getChildren().add(label);

        // Button for choosing directory
        Button buttonDirectory = new Button("Choose a Directory...");
        buttonDirectory.setOnAction(
                e -> {
                    Stage dirStage = new Stage();
                    // launch directory choose for user to choose directory
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    File chosen = directoryChooser.showDialog(dirStage);


                    try {
                        controller.clearFoldersLookedAt();
                        controller.launchOSGalleryView(chosen);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
        HBox buttonHb1 = new HBox(10);
        buttonHb1.setAlignment(Pos.CENTER);
        buttonHb1.getChildren().addAll(buttonDirectory);

        // button for getting log
        Button buttonLog = new Button("Get Log");
        HBox buttonHb3 = new HBox(10);
        buttonHb3.setAlignment(Pos.CENTER);
        buttonHb3.getChildren().addAll(buttonLog);

        buttonLog.setOnAction(e -> controller.launchLogTable());


        // VBox
        VBox vbox = new VBox(30);
        vbox.setPadding(new Insets(25, 25, 25, 25));

        vbox.getChildren().addAll(labelHb, buttonHb1, buttonHb3);
        vbox.getStyleClass().add("vbox");

        // Scene
        Scene scene = new Scene(vbox, 500, 500); // w x h

        // Styling with css (this is optional, if we have time, style with css)
        scene.getStylesheets().add("/sample/style.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Stage savedStage = primaryStage;
    }

    /**
     * (Nov. 9/17) Serialization adapted from: https://beginnersbook.com/2013/12/how-to-serialize-arraylist-in-java/
     * A method to deserialize the DataManager DataManger contains all information that needs to be
     * retrieved when the application has been restarted
     */
    private void deserialize() {
        // create a new serialize file
        File serFile = new File("filemanager.ser");

        // check if a filemanager.ser file exists
        if (serFile.exists() && !serFile
                .isDirectory()) { // previous information has been serialized before
            try {
                // get the ser file
                FileInputStream fis = new FileInputStream("filemanager.ser");
                // get the objects serialized in the ser file
                ObjectInputStream ois = new ObjectInputStream(fis);

                // if a previous DataManager has been serialized, set the current DataManger to the previously serialized -
                // DataManager
                DataManager dmSer = (DataManager) ois.readObject();
                if (dmSer != null) {
                    // replace it with the deserialized DataManager
                    dm = dmSer;
                }
                // close the input files
                ois.close();
                fis.close();
            } catch (IOException ioe) {
                System.out.println("Controller has not made any changes yet.");
            } catch (ClassNotFoundException c) {
                System.out.println("Class not found");
                c.printStackTrace();
            }
        }

    }


}