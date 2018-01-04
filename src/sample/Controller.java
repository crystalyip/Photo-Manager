package sample;

import Tagger.DataManager;
import Tagger.ImageFile;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class Controller {

    private ImageFile currentImageObject; // current ImageFile object user is viewing
    private DataManager dm; // the DataManager that this Controller object is controlling
    private SwitchView uniView; // the universal view that this Controller is modifying
    private ImageFileView imgView; // the single image view that this Controller is modifying
    private GalleryView gallView; // the image gallery view that this Controller is modifying
    private File currentDirectory; // the directory this 
    private ScrollPane tiles;
    private ArrayList<File> specifiedImages = new ArrayList<>();
    private File currentSubDirectory;

    public Controller(DataManager dm) {
        this.dm = dm;
        uniView = new SwitchView(dm.getLoggables(), this);
        gallView = new GalleryView(this);
    }

    /**
     * A method for when a user views a file.
     * GalleryView class will use this method
     *
     * @param file the corresponding ImageFile object the user is viewing
     */
    void launchImageView(File file) throws IOException {
        // Check if selected image has been serialized
        boolean wasSerialized = false;
        ImageFile imageFile = new ImageFile(file.getCanonicalPath());
        for (ImageFile imf : dm.getAllImageFiles()) {
            if (imf.getFile().getCanonicalPath().equals(file.getCanonicalPath())) {
                imageFile = imf;
                wasSerialized = true;
            }
        }

        if (!wasSerialized) {
            ImageFile newImg = new ImageFile(file.getCanonicalPath());
            this.currentImageObject = newImg;
            dm.addImageFile(newImg);
            serialize();
        } else {
            this.currentImageObject = imageFile;
        }

        this.currentImageObject.addObserver(dm);

        imgView = new ImageFileView(
                this.currentImageObject.getCurrTags(),
                this.currentImageObject.getOldNames(), dm.getUniversalTags(), this);

        BorderPane updateBox = imgView.launchSelectedImageView(currentImageObject);
        uniView.switchViewToImage(updateBox, file);
        uniView.updateSelectionOnListView(file);
    }


    /**
     * launches the gallery view after user has selected a directory from the OS directory chooser
     */
    void launchOSGalleryView(File chosen) throws IOException {
        specifiedImages.clear();
        currentDirectory = chosen;
        launchSubFolder(chosen);
    }

    void launchSubFolder(File chosen) {
        currentSubDirectory = chosen;
        specifiedImages.clear();
        //retrieve list of directory's files
        ArrayList<File> directoryPics = dm.getFiles(chosen);
        tiles = gallView.launchGalleryView(directoryPics);
        uniView.launchMainDisplay(tiles, directoryPics, chosen);
    }


    void launchTagsGallery(String tag) throws IOException {
        specifiedImages.clear();
        findFile(tag, currentDirectory);
        //ArrayList<File> filesWithTag = new ArrayList<>(specifiedImages);
        ScrollPane specificTiles = gallView.launchGalleryView(specifiedImages);
        uniView.setSpecifiedImagesView(specificTiles, specifiedImages, tag);
    }

    void launchAllImagesUnderRootGallery(ArrayList<File> allImagesUnderRoot) {
        specifiedImages = allImagesUnderRoot;
        // launch Image tiles with list of imageFiles
        ScrollPane specificTiles = gallView.launchGalleryView(specifiedImages);
        // launch ListView with list of files and adds to full layout
        uniView.setSpecifiedImagesView(specificTiles, specifiedImages, "All Images Under Root");

    }

    /**
     * launches the original gallery user was viewing before image selection
     */
    void backToGalleryView() throws IOException {
        if (!specifiedImages.isEmpty()) {
            ScrollPane tiles = gallView.launchGalleryView(specifiedImages);
            uniView.switchViewToGallery(tiles);
        } else {
            uniView.switchViewToGallery(tiles);
            //uniView.setListViewLayout(currentDirectory, dm.getFiles(currentDirectory));
        }
    }


    void launchLogTable() {
        uniView.launchLogView();
    }

    ImageFile getCurrentImageObject() {
        return currentImageObject;
    }

    /**
     * Controller will add tags to model and then update the view with the model's updated information
     * @param tags to be added
     * @throws IOException thrown if action fails
     */
    void addTag(String tags) throws IOException {
        File originalImgFile = currentImageObject.getFile();
        currentImageObject.addTag(tags);
        imgView.refreshListViewInfo(currentImageObject.getCurrTags(), currentImageObject.getOldNames(), dm.getUniversalTags());
        uniView.updateLogListView(dm.getLoggables());
        gallView.refreshGallery(originalImgFile, currentImageObject.getFile());
        uniView.updateListView(originalImgFile, currentImageObject.getFile());
        serialize();
    }

    /**
     * Controller will delete tags to model and then update the view with the model's updated information
     * @param tags to be deleted
     */
    void deleteTag(String tags) throws IOException {
        File originalImgFile = currentImageObject.getFile();
        currentImageObject.deleteTag(tags);
        imgView.refreshListViewInfo(currentImageObject.getCurrTags(), currentImageObject.getOldNames(), dm.getUniversalTags());
        uniView.updateListView(originalImgFile, currentImageObject.getFile());
        //gallView.updateGallery(originalImgFile, currentImageObject);
        //uniView.updateLogView(dm.getLoggables());
        gallView.refreshGallery(originalImgFile, currentImageObject.getFile());
        serialize();
    }

    /**
     * Controller will revert the model's name back to an older name and then update the view with the model's
     * updated information
     * @param name to be reverted to
     * @throws IOException thrown if action fails
     */
    void revertName(String name) throws IOException {
        File originalImgFile = currentImageObject.getFile();
        currentImageObject.revertName(name);
        imgView.refreshListViewInfo(currentImageObject.getCurrTags(), currentImageObject.getOldNames(), dm.getUniversalTags());
        uniView.updateListView(originalImgFile, currentImageObject.getFile());
        gallView.refreshGallery(originalImgFile, currentImageObject.getFile());
        serialize();
    }

    /**
     * takes user back to the main menu by closing the current stage being viewed
     */
    void goToMainMenu() {
        uniView.closeView();
    }


    /**
     * (Nov. 9/17) Serialization adapted from: https://beginnersbook.com/2013/12/how-to-serialize-arraylist-in-java/
     * A method to serialize information Information being serialized: data in DataManager including
     * loggables and - allImageFiles
     */
    private void serialize() {
        try {
            // write the serialized objects to a ser file
            FileOutputStream fos = new FileOutputStream("filemanager.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dm);
            // close the input streams
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * (Nov. 10/17) Adapted idea of a single folder chooser from a "single file chooser" found in the
     * link below https://stackoverflow.com/questions/37769481/javafx-gui-that-opens-a-text-file-
     * how-to-read-whats-in-text-file-and-edit-save A file chooser that selects a directory that user
     * has chosen from the directory browser popup
     *
     * @throws IOException If moving File fails
     */
    boolean moveFile(File file) throws IOException {
        // launch a new directory chooser
        Stage chooser = new Stage();
        DirectoryChooser folderChooser = new DirectoryChooser();
        File selectedFolder = folderChooser.showDialog(chooser);

        File[] selectedFolderFiles = selectedFolder.listFiles();

        // if a folder was selected
        if (selectedFolderFiles != null && !Arrays.asList(selectedFolderFiles).contains(new File(selectedFolder.getCanonicalPath() + "/" + file.getName()))) {

            if (currentImageObject.moveFile(selectedFolder.getCanonicalPath() + "/")) {
                // file is moved
                ScrollPane galleryView;
                if (!specifiedImages.isEmpty()) {
                    if (!selectedFolder.getCanonicalPath().contains(currentDirectory.getName())) {
                        uniView.deleteFileFromListView(file);
                        specifiedImages.remove(file);
                    } else {
                        uniView.updateListView(file, currentImageObject.getFile());
                        specifiedImages.set(specifiedImages.indexOf(file), currentImageObject.getFile());
                    }
                    galleryView = gallView.launchGalleryView(specifiedImages);
                } else {
                    uniView.deleteFileFromListView(file);
                    //currentDirectory = new File(currentDirectory.getCanonicalPath());
                    galleryView = gallView.launchGalleryView(dm.getFiles(currentSubDirectory));
                }
                uniView.switchViewToGallery(galleryView);
                return true;
            }
        }
        return false;
    }

    /**
     * Change universal tag set with new set
     * @param newUniTags to be changed to
     */
    void changeUniTags(ArrayList<String> newUniTags) {
        String empty = "";
        if (newUniTags.contains(empty)) {
            newUniTags.remove(empty);
        }
        dm.setUniversalTags(newUniTags);
        serialize();
    }

    ArrayList<String> getUniversalTags() {
        return dm.getUniversalTags();
    }

    /**
     * Find all images in current directory that is tagged with selected tag
     * @param tag that is selected
     * @param file directory
     */
    private void findFile(String tag, File file) {
        File[] list = file.listFiles();
        if (list != null && list.length > 0)
            for (File fil : list)
            {
                if (fil.isDirectory()) {
                    findFile(tag, fil);
                } else if (fil.getName().contains("@" + tag)) {
                    specifiedImages.add(fil);
                }
            }
    }

    /**
     * Get current directory
     * @return current directory
     */
    File getCurrentDirectory() {
        return this.currentDirectory;
    }

    /**
     * clear folders looked at
     */
    void clearFoldersLookedAt() {
        uniView.foldersLookedAt = new ArrayList<>();
    }

    /**
     * Get images from folder
     * @param folder current folder
     * @return images from folder
     */
    ArrayList<File> getImages(File folder) {
        return dm.getFiles(folder);
    }

    /**
     * launch universal tags view
     */
    void launchUniversalTagView() {
        ArrayList<String> uniTags = dm.getUniversalTags();
        uniView.launchUniversalTagsView(uniTags);
    }


}
