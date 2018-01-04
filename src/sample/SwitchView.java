package sample;

import Tagger.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

 class SwitchView {

  private Controller controller;
  private ObservableList<LogObject> logEntries;


  ArrayList<File> foldersLookedAt = new ArrayList<>();

  private ListView<File> picsListView;
  private ListView<File> folderListView;

  private Scene scene;

  private Text actionStatus;

  private Stage universalStage;

  private ListView<String> universalTags = new ListView<>();

  private VBox listBox;


  private File rootDirectory;



  // nodes that will be updated upon user interaction
  private BorderPane all; // A pane that contains all nodes in the primaryStage
  private ArrayList<File> allImagesUnderRoot = new ArrayList<>();
  private ArrayList<File> allSubFolders = new ArrayList<>();
  private Stage logStage;

     SwitchView(ArrayList<LogObject> logs, Controller controller) {
    logEntries = FXCollections.observableArrayList(logs);
    this.controller = controller;
  }


     /**
      * Refreshes the list of log entries with an updated array list of log entries. This method is called every time
      * name changes to file images are made - the logEntries list is update with new cells that include the old name,
      * new name, and the time the changes were made.
      * @param logs an array list of LogObjects
      */
   void updateLogListView(ArrayList<LogObject> logs) {
    logEntries = FXCollections.observableArrayList(logs);
  }


  /**
   * Log entries display a LogObject's old name, new name, and the date these changes were made.
   */
   void launchLogView() {
    TableView<LogObject> table = new TableView<>();
    //initialize 3 columns, using Log class to extract info
    TableColumn<LogObject, String> newNameColumn = new TableColumn<>("New Name");
    TableColumn<LogObject, String> oldNameColumn = new TableColumn<>("Old Name");
    TableColumn<LogObject, String> timeStampColumn = new TableColumn<>("Timestamp");

    // Set up a new log stage
       if (logStage == null) {
           logStage = new Stage();
       }
    logStage.setTitle("Log Table");
    logStage.setWidth(915);
    logStage.setHeight(500);

    // Add all 3 columns to table
    ObservableList<TableColumn<LogObject, ?>> tableList = table.getColumns();
    tableList.add(newNameColumn);
    tableList.add(oldNameColumn);
    tableList.add(timeStampColumn);

    // Set the title of the stage
    final Label label1 = new Label("Renaming History");
    label1.setFont(new Font("Arial", 20));

    // do not allow editing of the tables
    table.setEditable(false);

    //Assign each column to a instance variable of Log Object
    oldNameColumn.setCellValueFactory(new PropertyValueFactory<>("oldName"));
    newNameColumn.setCellValueFactory(new PropertyValueFactory<>("newName"));
    timeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));

    //Initialize table to list of Log objects
    table.setItems(logEntries);

    // Initialize the vbox for the table
    final VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 10, 10, 10));
    vbox.getChildren().addAll(label1, table);

    // set the scene for the log table
    Scene scene = new Scene(vbox, 915, 500);
    // add css stylesheet
    scene.getStylesheets().add("sample/style.css");
    logStage.setScene(scene);
    logStage.show();
  }

     /**
      * updates the Switch View from the GalleryView to the single ImageFileView. This method is called when the user
      * switches its
      * @param view the border pane that the view is switching to
      * @param image the image that the single ImageFileView is diplaying
      */
  void switchViewToImage(BorderPane view, File image) {
    universalStage.setTitle("File selected: " + image.getAbsolutePath());
    all.setCenter(view);
  }

     /**
      * updates the Switch View from the ImageFileView to the single ImageFileView. This
      */
  void switchViewToGallery(ScrollPane tiles) {
    universalStage.setTitle("Root Directory");
    all.setCenter(tiles);
    actionStatus.setText("");
  }

  void launchMainDisplay(ScrollPane view, ArrayList<File> directoryPics, File chosen) {
    // if foldersLookedAt does not contain folder, add folder
    // else, if going back to a previous folder with Go Back button, don't add file

    if (chosen != null) {
      if (!foldersLookedAt.contains(chosen)) {
        foldersLookedAt.add(chosen);
      }
      rootDirectory = foldersLookedAt.get(0);
    }

    ArrayList<String> uniTags = controller.getUniversalTags();
    recSetImages(rootDirectory);
    for (File image: allImagesUnderRoot) {
      String imageName = image.getName();
      if (imageName.contains("@")) {
        String tagStr = imageName.substring(imageName.indexOf("@"), imageName.lastIndexOf("."));
        String[] tagList = tagStr.split("@");
        for (String tag : tagList) {
          tag = tag.trim();
          if (!uniTags.contains(tag)) {
            uniTags.add(tag);
          }
        }
      }
    }
    controller.changeUniTags(uniTags);

    setActionStatus();
    all = new BorderPane();

    all.setCenter(view);
    setListViewLayout(chosen, directoryPics);
    all.setBottom(listBox);
    if (chosen != null) {
      setStage(all, chosen.getName());
    }
    setMenuBar();
  }

  void updateListView(File original, File changed){
    picsListView.getItems().set(picsListView.getItems().indexOf(original), changed);
  }


  /**
   * setting up listview layout
   * @param chosen the chosen directory
   * @param directoryPics the files in the directory
   */
  private void setListViewLayout(File chosen, ArrayList<File> directoryPics) {

    // Set galleryObject's picsListView with the image files
    setPicsListView(directoryPics);

    //https://stackoverflow.com/questions/9722418/how-to-handle-listview-item-clicked-action
    picsListView.setOnMouseClicked(e -> {
      try {
        controller.launchImageView(picsListView.getSelectionModel().getSelectedItem());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });


    // Set folderListView
    setFolderListView(chosen);
    Button buttonFolder = new Button("Go to folder");
    buttonFolder.setOnAction(e1 -> {
      File chosenFolder = folderListView.getSelectionModel().getSelectedItem();
      //setSubfolderDisplay(chosenFolder);
      controller.launchSubFolder(chosenFolder);
    });

    HBox folderHBox = new HBox(10);

    // if we are in a root's subfolder, then add Go Back button and Go Back to Root button
    if (foldersLookedAt.size() > 1) {

       Button buttonBack = new Button("<<");
        buttonBack.setOnAction(e3 -> {
          try {
            goBackButtonClicked();
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      folderHBox.getChildren().addAll(buttonBack);
    }

    folderHBox.getChildren().addAll(buttonFolder);
    folderHBox.setAlignment(Pos.TOP_RIGHT);

    Text picsText = new Text("Images:");
    Text folderText = new Text("Folders:");

    VBox picsVBox = new VBox(10);
    picsVBox.getChildren().addAll(picsText, picsListView);

    VBox folderVBox = new VBox(10);
    folderVBox.getChildren().addAll(folderText, folderListView, folderHBox);

    HBox listHBox = new HBox(10);
    listHBox.setPadding(new Insets(10 , 10, 10, 10));
    listHBox.getChildren().addAll(picsVBox, folderVBox);
    picsListView.setPrefWidth(700);
    picsListView.setPrefHeight(150);
    folderListView.setPrefWidth(300);
    folderListView.setPrefHeight(150);
    listHBox.setAlignment(Pos.CENTER);
    listBox = new VBox();
    listBox.getChildren().addAll(listHBox);

  }

   private void setMenuBar(){
    final Menu fileMenu = new Menu("File");
    // view tagset, all images under root directory
    final Menu viewMenu = new Menu("View");
    final Menu moveMenu = new Menu("Move");

    CheckMenuItem tagSetMenu = new CheckMenuItem("View Tag Set");
    tagSetMenu.setSelected(false);
    tagSetMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue){
        controller.launchUniversalTagView();
        tagSetMenu.setSelected(false);
      }
    });


    CheckMenuItem allImagesMenu = new CheckMenuItem("View All Images Under Root Directory");
    allImagesMenu.setSelected(false);
    allImagesMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue){
        // All images under root will be tracked
        allImagesUnderRoot= new ArrayList<>();
        // All subFolders under root will be tracked
        allSubFolders = new ArrayList<>();
        // Sets allImagesUnderRoot
        recSetImages(rootDirectory);
        controller.launchAllImagesUnderRootGallery(allImagesUnderRoot);
        //allImagesMenu.setSelected(false);
      }
    });

    viewMenu.getItems().addAll(allImagesMenu, tagSetMenu);

    CheckMenuItem homeDirMenu = new CheckMenuItem("Go To Root Directory");
    homeDirMenu.setSelected(false);
    homeDirMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue){
        try {
          controller.launchOSGalleryView(controller.getCurrentDirectory());
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        homeDirMenu.setSelected(false);
      }
    });

     CheckMenuItem mainMenu = new CheckMenuItem("Main Menu");
     mainMenu.setSelected(false);
     mainMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
       if (newValue){
         controller.goToMainMenu();
         mainMenu.setSelected(false);
       }
     });

     CheckMenuItem goBackMenu = new CheckMenuItem("Back to Gallery");
     goBackMenu.setSelected(false);
     goBackMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
       if (newValue){
         try {
           controller.backToGalleryView();
           goBackMenu.setSelected(false);
         } catch (IOException e1) {
           e1.printStackTrace();
         }
       }
     });

    CheckMenuItem OSFileMenu = new CheckMenuItem("Go To OS Directory");
    OSFileMenu.setSelected(false);
    OSFileMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue){
        Text actionStatusOS = getActionStatusOS();
        buttonOSDirClicked(picsListView.getSelectionModel().getSelectedItem(), actionStatusOS);
        OSFileMenu.setSelected(false);
      }
    });
    fileMenu.getItems().addAll(homeDirMenu, OSFileMenu, goBackMenu, mainMenu);

    CheckMenuItem moveToMenu = new CheckMenuItem("Move To...");
    moveToMenu.setSelected(false);
    moveToMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        setMoveFileButton();
        moveToMenu.setSelected(false);
      }
    });

    moveMenu.getItems().add(moveToMenu);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(fileMenu, viewMenu, moveMenu);

    all.setTop(menuBar);


  }

  private void setMoveFileButton(){
    try {
      // chose the folder that file will be moved to
      File selected = picsListView.getSelectionModel().getSelectedItem();
      if (controller.moveFile(selected)){
        actionStatus.setText("File " + selected.getName() + " moved");
      }
      else{
        actionStatus.setText("File " + selected.getName() + " already exists in selected folder");
      }
    } catch (IOException i) {
      i.printStackTrace();
    }
  }



  /**
   * Recursion to find all images under a given directory, including images from subFolders
   * @param directory To get images and subFolders from
   */
  private void recSetImages(File directory) {
    // Add images under directory
    allImagesUnderRoot.addAll(getImages(directory));
    // All subFolders of directory
    ArrayList<File> subFolders = getSubFolders(directory);
    allSubFolders.addAll(subFolders);
    // Recursion over subFolders
    for (File subFolder: subFolders) {
      recSetImages(subFolder);
    }
  }


  private ArrayList<File> getImages(File folder) {
    return controller.getImages(folder);
  }

  void closeView() {
    universalStage.close();
  }

  void deleteFileFromListView(File moved) {
    //remove file from list display
    picsListView.getItems().removeAll(moved);
    if (allSubFolders.contains(moved.getParentFile())) {
      picsListView.getItems().removeAll(moved);
    }
  }

  void updateSelectionOnListView(File selected){
      picsListView.getSelectionModel().select(selected);
  }


  /**
   * Set up GalleryObject's picsListView
   *
   * @param directoryPics chosen directory
   */
  private void setPicsListView(ArrayList<File> directoryPics) {
    // Adapted from (Nov10/2017):
    // https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/list-view.htm#CEGGEDBF
    // picsListView is a ListView of all image in files in chosen directory
    picsListView = new ListView<>();
    // make the items in picsListView Observable
    ObservableList<File> imageItems = FXCollections.observableArrayList(directoryPics);
    picsListView.setItems(imageItems);
    picsListView.setCellFactory(list -> new FileDisplay());


  }

  /**
   * Set up folderListView
   *
   * @param directory chosen directory
   */
  private void setFolderListView(File directory) {
    // listview that displays subdirectories of directory
    folderListView = new ListView<>();

    // Set observable list with subfolders
    ObservableList<File> folderItems = FXCollections.observableArrayList(getSubFolders(directory));


    folderListView.setItems(folderItems);
    folderListView.setCellFactory(list -> new FolderDisplay());
  }


  private ArrayList<File> getSubFolders(File directory) {
    ArrayList<File> directoryFolders = new ArrayList<>();
    // Get subfolders from directory
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          directoryFolders.add(file);
        }
      }
    }
    return directoryFolders;
  }


  /**
   * Set up actionStatus Text
   */
  private void setActionStatus() {
    // Status message text
    actionStatus = new Text();
    actionStatus.setFont(Font.font("Calibri", FontWeight.NORMAL, 13));
    actionStatus.setFill(Color.GREEN);
  }

  /**
   * Action when Go Back button is clicked
   */
  private void goBackButtonClicked() throws IOException {
    // remove last folder looked at
    foldersLookedAt.remove(foldersLookedAt.size() - 1);
    if (foldersLookedAt.size() > 1) {
      try {
        // Launch display of previous folder
        controller.launchOSGalleryView(foldersLookedAt.get(foldersLookedAt.size() - 1));
        //setSubfolderDisplay(foldersLookedAt.get(foldersLookedAt.size() - 1));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // launch display of root folder
      //controller.backToGalleryView();
      File rootFolder = foldersLookedAt.get(foldersLookedAt.size() - 1);
      controller.clearFoldersLookedAt();
      controller.launchOSGalleryView(rootFolder);
    }
  }

  /**
   * Set up main gallery window
   *
   * @param all main layout of window
   */
  private void setStage(BorderPane all, String chosenDirectory) {

    if (universalStage == null) {
      universalStage = new Stage();
    }

    universalStage.setTitle(chosenDirectory);

    scene = new Scene(all, 1150, 850);

    universalStage.setScene(scene);
    scene.getStylesheets().add("sample/style.css");
    universalStage.show();
  }

  void setSpecifiedImagesView(ScrollPane view, ArrayList<File> files, String title){
    setActionStatus();
    all = new BorderPane();
    //universalStage = new Stage();
    all.setCenter(view);
    setPicsListView(files);
    setMenuBar();
    picsListView.setOnMouseClicked(e -> {
      try {
        controller.launchImageView(picsListView.getSelectionModel().getSelectedItem());
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    });

    VBox picsVBox = new VBox(10);
    picsVBox.getChildren().addAll(picsListView);

    HBox listHBox = new HBox(10);
    listHBox.setPadding(new Insets(10 , 0, 10, 0));
    listHBox.setPadding(new Insets(10 , 0, 10, 0));
    listHBox.getChildren().addAll(picsVBox);
    picsListView.setPrefWidth(scene.getWidth());
    picsListView.setPrefHeight(200);
    listHBox.setAlignment(Pos.CENTER);
    VBox listBox = new VBox();
    listBox.getChildren().addAll(listHBox);
    all.setBottom(listBox);
    //all.setBottom(bottomHBox);
    setStage(all, title);
  }




  /**
   * Displays each Folder item in folderListView's ObservableList folderItems
   */
  static class FolderDisplay extends ListCell<File> {
    ImageView imageView = new ImageView();
    @Override
    public void updateItem(File item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setGraphic(null);
        setText(null);
      } else {
        try {
          File file = new File("src/Folder-icon.jpg");
          Image image = new Image(new FileInputStream(file.getCanonicalPath()));
          imageView.setImage(image);
          imageView.setFitWidth(25);
          imageView.setPreserveRatio(true);
          setGraphic(imageView);
        } catch (IOException e) {
          e.printStackTrace();
        }
        setText(item.getName());
      }

    }
  }

  /**
   * Displays each File item in picsListView's ObservableList imageItems Updates the ListView
   * display when changes are made to the list of File items
   */
  static class FileDisplay extends ListCell<File> {

    ImageView imageView = new ImageView();

    /**
     * File is displayed in a ListView cell that updates when changes are made to the filename
     *
     * @param item File to be displayed in cell
     * @param empty Is true if item's value is null
     */
    @Override
    public void updateItem(File item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setGraphic(null);
        setText(null);
      } else {
        try {
          Image image = new Image(new FileInputStream(item));
          imageView.setImage(image);
          imageView.setFitWidth(30);
          imageView.setPreserveRatio(true);
          setGraphic(imageView);
        } catch (FileNotFoundException c) {
          c.printStackTrace();
        }
        setText(item.getAbsolutePath());
      }
    }
  }

  /**
   * launches the table of universal tags
   */
   void launchUniversalTagsView(ArrayList<String> tags) {
    Stage allTagsStage = new Stage();
    universalTags.getItems().clear();
    universalTags.getItems().addAll(tags);
    universalTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    //universalTags.setPrefHeight(100);

    // set up new vbox for the listview
    VBox allTagsBox = new VBox(20);
    allTagsBox.setPadding(new Insets(15, 15, 15, 15));
    allTagsBox.setAlignment(Pos.CENTER);

    // set up button for the name reversion
    Label nameLabel = new Label("Universal Tags");
    nameLabel.setTextFill(Color.BLACK);
    nameLabel.setFont(Font.font("Calibri", FontWeight.NORMAL, 18));
    HBox nameLabelHb = new HBox();
    nameLabelHb.setAlignment(Pos.CENTER);
    nameLabelHb.getChildren().add(nameLabel);

    ObservableList<String> selectedTags = universalTags.getSelectionModel().getSelectedItems();

    Button deleteTag = new Button("Delete Tags from List");
    deleteTag.setOnAction(e -> {
      // Observ list does not iterate properly, so convert to arraylist
      ArrayList<String> arrayTags = new ArrayList<>(selectedTags);
      System.out.println("arrayTags: " + arrayTags);
      for (String tag: arrayTags) {
        universalTags.getItems().remove(tag);
        System.out.println(tag + " removed");
      }
      //get updated uni tags
      ArrayList<String> updatedTags = new ArrayList<>(universalTags.getItems());
      System.out.println("new tagslist " + updatedTags);
      // Update datamanager uni tags
      controller.changeUniTags(updatedTags);
    });

    TextField createUniTag = new TextField();

    Button buttonCreateUniTag = new Button("Add Tags to List");
    buttonCreateUniTag.setOnAction(e -> {
      // get tags string from user input
      String addedTags = createUniTag.getText();

      if (addedTags.length() > 0) {
        // Add tags to a list to iterate over and add to listview
        String[] addedTagsList = addedTags.split(", ");
        for (String tag: addedTagsList) {
          if (!universalTags.getItems().contains(tag)) {
            universalTags.getItems().add(tag);
          }
        }
        // Change datamanager uni tags
        controller.changeUniTags(new ArrayList<>(universalTags.getItems()));
      }
    });


    Button buttonViewTagGallery = new Button("View Photos With Tag...");
    HBox buttonHb2 = new HBox(10);
    buttonHb2.setAlignment(Pos.CENTER);
    buttonHb2.getChildren().addAll(buttonViewTagGallery);

    buttonViewTagGallery.setOnAction(e3 ->{
      String tag = universalTags.getSelectionModel().getSelectedItem();
      allTagsStage.close();
      try {
        controller.launchTagsGallery(tag);
      } catch (IOException e) {
        e.printStackTrace();
      }


    });

    allTagsBox.getChildren().addAll(nameLabel, createUniTag, buttonCreateUniTag, universalTags, deleteTag, buttonHb2);
    Scene tagsScene = new Scene(allTagsBox, 500, 400);
    allTagsStage.setScene(tagsScene);
    tagsScene.getStylesheets().addAll("sample/style.css");
    allTagsStage.show();
  }

  private void buttonOSDirClicked(File image, Text actionStatusOS) {
    // Adapted from (Nov17/2017): https://docs.oracle.com/javase/tutorial/uiswing/misc/desktop.html
    // Adapted from (Nov22/2017): https://docs.oracle.com/javase/tutorial/uiswing/misc/desktop.html
    // Check if Desktop is supported on current platform
    if (Desktop.isDesktopSupported()) {
      // Use lambda to pass into Thread constructor an instance of anonymous class
      // that implements Runnable and override method run()
      new Thread(() -> {
        Desktop desktop = Desktop.getDesktop();
        // Check if action OPEN is supported on current platform
        if (desktop.isSupported(Desktop.Action.OPEN)) {
          try {
            desktop.open(image.getParentFile());
          } catch (IOException i) {
            actionStatusOS.setText("Cannot open directory");
            i.printStackTrace();
          }
        } else {
          actionStatusOS.setText("Action not supported by current platform");
        }
        // calling start() causes this thread to execute run()
      }).start();
    } else {
      actionStatusOS.setText("Action not supported by current platform");
    }
  }

  private Text getActionStatusOS() {
    Text actionStatusOS = new Text();
    actionStatusOS.setFont(Font.font("Calibri", FontWeight.NORMAL, 13));
    actionStatusOS.setFill(Color.RED);
    return actionStatusOS;
  }


}
