package sample;

import Tagger.*;
import javafx.scene.input.KeyCode;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ImageFileView handles single image view
 */
public class ImageFileView {
    private ListView<String> currTags;
    private ListView<String> oldNames;

    private Controller controller;
    private ListView<String> universalTags = new ListView<>();

    private Stage oldNamesStage;

    /**
     * Initialize ImageFileView
     * @param currentTags current tags of image file
     * @param prevNames old names of image file
     * @param uniTags Universal tags
     * @param controller Controller
     */
    public ImageFileView(
            ArrayList<String> currentTags, ArrayList<String> prevNames, ArrayList<String> uniTags, Controller controller){
        currTags = new ListView<>();
        currTags.getItems().addAll(currentTags);
        oldNames = new ListView<>();
        oldNames.getItems().addAll(prevNames);
        universalTags.getItems().addAll(uniTags);
        this.controller = controller;
    }


    /**
     * refreshListViewInfo updates the information that is being displayed on the current tag, previous names, and
     * universal tag list views. This method is called every time tags are added and deleted from the selected image.
     * After an image is tagged, these list views will update correspondingly. Ex. if a tag was added then the current
     * tags list view will display the proper renamed file name
     * @param currentTags updated list of current tags the image is tagged with
     * @param prevNames updated list of all previous names the image has had
     * @param uniTags updated list of the global tag set
     */
    void refreshListViewInfo(ArrayList<String> currentTags, ArrayList<String> prevNames, ArrayList<String> uniTags){
        // clear the current tags and fill it with the updated list
        this.currTags.getItems().clear();
        this.currTags.getItems().addAll(currentTags);
        // clear the old names and fill it with the updated list
        this.oldNames.getItems().clear();
        this.oldNames.getItems().addAll(prevNames);
        // clear the universal tags and fill it with the updated list
        this.universalTags.getItems().clear();
        this.universalTags.getItems().addAll(uniTags);
    }


    /**
     * Removes one or more tags chosen from the set of current tags of the image
     */
    private void buttonRemoveTagClicked() {
        // create a string of comma separated tags that will be deleted from the current image file
        StringBuilder sb = new StringBuilder();

        // get the tags that will be deleted from the list view selection model
        ObservableList<String> selectedTags = currTags.getSelectionModel().getSelectedItems();
        for (String t : selectedTags) {
            sb.append(t);
            sb.append(", ");
        }
        String tagsToBeDeleted = sb.toString();

        // if the tags to be deleted is not empty
        if (tagsToBeDeleted.length() != 0) {
            // delete tag
            try {
                controller.deleteTag(tagsToBeDeleted);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a new border pane filled with ImageFileView information and returns this border pane. The border pane
     * contains the list view of current tags, old names and global tag sets.
     * @param image the selected image file
     * @return returns a border pane that is set up with all the ImageFileView information
     */
     BorderPane launchSelectedImageView(ImageFile image) {

        TextField addedTag = new TextField();
        addedTag.setPrefWidth(200);
        // Button for adding tag
        Button buttonAddTag = new Button("New Tag");
        buttonAddTag.setPrefWidth(100);
        buttonAddTag.setLayoutX(10);
        buttonAddTag.setOnAction(e3 -> {
            String tagged = addedTag.getText();
            if (tagged.length() != 0) {
                // add tag
                try {
                    controller.addTag(tagged);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        BorderPane addTagBox = new BorderPane();
        addTagBox.setLeft(addedTag);
        addTagBox.setRight(buttonAddTag);

        // button for removing tag
        Button buttonRemoveTag = new Button("Remove Selected Tag");
        buttonRemoveTag.setPadding((Insets.EMPTY));
        buttonRemoveTag.setPrefWidth(300);

        buttonRemoveTag.setOnAction(e4 -> buttonRemoveTagClicked());

        Button buttonOldNames = new Button("Old Names");
        buttonOldNames.setOnAction(e4 -> launchOldNamesTable(controller.getCurrentImageObject()));

         ObservableList<String> selectedTags = universalTags.getSelectionModel().getSelectedItems();

         Button chooseUniTagButton = new Button("Add Tag to Image");
         chooseUniTagButton.setOnAction(e -> {
             try {
                 StringBuilder tagStr = new StringBuilder();
                 for (String tag : selectedTags) {
                     tagStr.append(tag).append(", ");
                 }
                 if (tagStr.toString().endsWith(", ")) {
                     tagStr = new StringBuilder(tagStr.substring(0, tagStr.length() - 2));
                 }
                 controller.addTag(tagStr.toString());
             } catch (IOException e1) {
                 e1.printStackTrace();
             }
         });



        BorderPane universalTagsPane = new BorderPane();
        universalTagsPane.setCenter(universalTags);
        Label uniLabel = new Label("Tag Set");
        universalTagsPane.setTop(uniLabel);
        universalTagsPane.setBottom(chooseUniTagButton);
        universalTagsPane.setPadding(new Insets(0, 0, 15, 0));

        BorderPane currTagsPane = new BorderPane();
        currTagsPane.setBottom(currTags);
        Label currLabel = new Label("Current Tags");
        currTagsPane.setTop(currLabel);
        currTagsPane.setCenter(addTagBox);

        BorderPane tagsPane = new BorderPane();
        tagsPane.setPrefSize(300, 800);
        tagsPane.setCenter(universalTagsPane);
        tagsPane.setBottom(currTagsPane);
        currTags.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        imageView = setImageViewIcon();
                        setGraphic(imageView);
                        setText(name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        universalTags.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        if (image.getCurrTags().contains(name)) {
                            imageView = setImageViewIcon();
                            setGraphic(imageView);
                            setText(name);
                        } else {
                            setGraphic(null);
                            setText(name);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        currTags.setPrefHeight(100);
        currTags.setPrefWidth(320);
        currTags.setPadding(new Insets(10,0,0,0));
        universalTags.setPrefHeight(150);
        universalTags.setPrefWidth(320);


        currTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        universalTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        currTags.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE){
                buttonRemoveTagClicked();
            }
        });

        universalTags.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER){
                try {
                    StringBuilder tagStr = new StringBuilder();
                    for (String tag : selectedTags) {
                        tagStr.append(tag).append(", ");
                    }
                    if (tagStr.toString().endsWith(", ")) {
                        tagStr = new StringBuilder(tagStr.substring(0, tagStr.length() - 2));
                    }
                    controller.addTag(tagStr.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });


        VBox tagBox = new VBox(10);
        tagBox.setPadding(new Insets(10, 15, 10, 15));
        tagBox.setAlignment(Pos.CENTER);
        tagBox.setPrefSize(300, 200);
        tagBox.getChildren().addAll(tagsPane,  buttonRemoveTag, buttonOldNames);

         ImageView imageView = null;
         //Creating an image
         try {
             // Adapted from (Nov8/2017): https://www.tutorialspoint.com/javafx/javafx_images.htm
             Image imageSelected = new Image(new FileInputStream(image.getFile()));
             imageView = new ImageView(imageSelected);
             imageView.setX(50);
             imageView.setY(70);
             imageView.setFitHeight(400);
             imageView.setFitWidth(500);
             imageView.setPreserveRatio(true);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }

        BorderPane imageBox = new BorderPane();
        imageBox.setPadding(new Insets(30, 48, 15, 0));
        imageBox.setCenter(imageView);
        BorderPane.setMargin(imageView, new Insets(10, 10, 10, 10));
        imageBox.setRight(tagBox);
        imageBox.setPrefHeight(500);

        return imageBox;
    }


    /**
     * launch table of old names
     */
     private void launchOldNamesTable(ImageFile image){
        if (oldNamesStage == null) {
            oldNamesStage = new Stage();
        }
        oldNamesStage.setTitle("Old Data");

        // set up a new list view
        ListView<String> oldView = new ListView<>();
        oldView.getItems().addAll(image.getOldNames());
        oldView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // set up new vbox for the listview
        VBox oldNamesBox = new VBox(20);
        oldNamesBox.setPadding(new Insets(15, 15, 15, 15));
        oldNamesBox.setAlignment(Pos.CENTER);

        // set up button for the name reversion
        Label nameLabel = new Label("Revert To...");
        nameLabel.setTextFill(Color.BLACK);
        nameLabel.setFont(Font.font("Calibri", FontWeight.NORMAL, 18));
        HBox nameLabelHb = new HBox();
        nameLabelHb.setAlignment(Pos.CENTER);
        nameLabelHb.getChildren().add(nameLabel);
        Button rename = new Button("Rename");

        rename.setOnAction(eB -> {
            String selected = oldView.getSelectionModel().getSelectedItem();
            try {
                controller.revertName(selected);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        oldNamesBox.getChildren().addAll(nameLabel, oldView, rename);
        Scene tagsScene = new Scene(oldNamesBox, 500, 250);
        oldNamesStage.setScene(tagsScene);
        oldNamesStage.show();
    }


    /**
     * Sets tag icon to denote tag is current tag of image
     * @return ImageView of tag icon
     * @throws IOException if file does not exist
     */
    private ImageView setImageViewIcon() throws IOException {
        ImageView imageView = new ImageView();
        File file = new File("src/tagicon.jpg");
        Image icon = new Image(new FileInputStream(file.getCanonicalPath()));
        imageView.setImage(icon);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        return imageView;

    }

}
