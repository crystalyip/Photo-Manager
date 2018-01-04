package Tagger;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.*;

public class DataManager implements Observer, Serializable {

    private ArrayList<LogObject> loggables = new ArrayList<>(); // contains the history of renaming
    private ArrayList<ImageFile> allImageFiles = new ArrayList<>(); // contains the ImageFiles being tracked
    private ArrayList<String> universalTags = new ArrayList<>(); // contains all the tags that any ImageFile has had

    /**
     * Update method that is called when observers are notified.
     * DataManager updates by adding a new log entry, LogObject into its ArrayList of loggable entries
     * Updates Universal tags list
     *
     * @param o   the observable object ImageFile
     * @param arg ImageFile
     */
    @Override
    public void update(Observable o, Object arg) {
        // if a file has been moved
        if (((ImageFile) arg).getPrevFile().getParent().equals(((ImageFile) arg).getFile().getParent())) {
            for (String tag : ((ImageFile) arg).getCurrTags()) {
                if (!universalTags.contains(tag)) {
                    universalTags.add(tag);
                }
            }
            // create a new log object and set the old name and new name
            LogObject log = new LogObject(((ImageFile) arg).getPrevFile().getName(), ((ImageFile) arg).getFile().getName());
            // add the log entry to the list of loggable entries
            loggables.add(log);
        }
    }

    /**
     * A getter for all files in a chosen directory
     *
     * @param directory a chosen directory
     * @return directoryPics
     */
    public ArrayList<File> getFiles(File directory) {
        File[] directoryFiles = directory.listFiles();


        ArrayList<File> directoryPics = new ArrayList<>();
        //retrieve list of folder's image files

        //Adapted from (Nov. 16/18):
        //https://stackoverflow.com/questions/9643228/test-if-file-is-an-image
        //https://stackoverflow.com/questions/4855627/java-mimetypesfiletypemap-always-returning-application-octet-stream-on-android-e
        if (directoryFiles != null) { // if the directory exists
            for (File file : directoryFiles) {
                //Only image files are added to directoryPics
                MimetypesFileTypeMap map = new MimetypesFileTypeMap();
                map.addMimeTypes("image png tif jpg jpeg bmp");
                String mimeType = map.getContentType(file);
                if (mimeType.substring(0, 5).equalsIgnoreCase("image")) {
                    directoryPics.add(file);
                }
            }
        }
        return directoryPics;
    }

    /**
     * A method used to add any ImageFile objects that user has viewed/modified
     *
     * @param imf the ImageFile being added
     */
    public void addImageFile(ImageFile imf) {
        allImageFiles.add(imf);
    }

    /**
     * A getter for the list of ImageFiles being tracked
     *
     * @return allImageFiles
     */
    public ArrayList<ImageFile> getAllImageFiles() {
        return allImageFiles;
    }

    /**
     * A getter for the loggable entries
     *
     * @return loggables
     */
    public ArrayList<LogObject> getLoggables() {
        return loggables;
    }

    /**
     * A setter method for the universal list of tags
     *
     * @param universalTags the input list of universal tags
     */
    public void setUniversalTags(ArrayList<String> universalTags) {
        this.universalTags = universalTags;
    }

    /**
     * A getter for the universal list of tags
     *
     * @return universalTags
     */
    public ArrayList<String> getUniversalTags() {
        return universalTags;
    }
}



