package Tagger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

public class ImageFile extends Observable implements Serializable {

    private File file; // the corresponding File object
    private File prevFile; // the previous File object of this ImageFile
    private ArrayList<String> currTags; // all the currTags that have been chosen
    private ArrayList<String> oldNames; // all the names used

    /**
     * Constructor for the ImageFile class
     *
     * @param path the string path that the ImageFile corresponds to
     */
    public ImageFile(String path) {
        this.file = new File(path);
        this.oldNames = new ArrayList<>();
        this.currTags = new ArrayList<>();
    }

    /**
     * Move this ImageFile to another directory denoted by directoryPath
     *
     * @param directoryPath the destination directory
     * @return boolean if the ImageFile has been moved
     */

    public boolean moveFile(String directoryPath) {
        try {
            // only move file if there are no files with the same name in the target directory
            File newFile = new File(directoryPath + file.getName());
            if (!file.getCanonicalPath().equals(newFile.getCanonicalPath()) && !newFile.exists()) {
                // set the current file the user is viewing to the moved file
                //this.file = moved;

                updateFile(newFile);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a string of tags to the file name
     *
     * @param tags A string of tags that are being added to the file name
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void addTag(String tags) throws IOException {

        String originalPathName = file.getCanonicalPath();
        String[] tagList = tags.split("[,]+");
        // A string that will store the name of the file as tags are added
        String newPathName = file.getCanonicalPath();
        // track whether tagging action has actually been done
        boolean tagged = false;
        // find the image type (ex. .jpg, .png, .jpeg)
        String photoType = file.getCanonicalPath()
                .substring(file.getCanonicalPath().lastIndexOf("."));

        String empty = "";
        // loop through the tag list to add tags
        for (String tag : tagList) {
            if (!tag.trim().equals(empty) && !originalPathName
                    .contains("@" + tag.trim())) { // only add tag if tag does not already exist on the photo
                tagged = true;
                // replace the photo type with an empty string, add the tag, then concatenate the phototype
                newPathName = newPathName.replace(photoType, "") + " @" + tag.trim() + photoType;
            }
        }

        // if tagging was done, update the actual file in the OS directory
        if (tagged) {
            for (String a : tagList) {
                if (!a.trim().equals(empty) && !currTags.contains(a.trim())) {
                    currTags.add(a.trim());
                }
            }
            File newFile = new File(newPathName);
            updateFile(newFile);
        }

    }

    /**
     * Deletes a string of tags from the file name
     *
     * @param tags a string of tags that are being deleted from the file name
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void deleteTag(String tags) throws IOException {
        //https://www.mkyong.com/java/how-to-move-file-to-another-directory-in-java/
        String[] tagList = tags.split(", ");
        String newPathName = file.getCanonicalPath();
        // tracks whether tags have actually been removed from the file
        boolean deleted = false;
        // delete each tag in the tag list
        for (String tag : tagList) {
            // if the tag is in the middle of the file name
            if (newPathName.contains("@" + tag + " ")) {
                deleted = true;
                newPathName = newPathName.replace(" @" + tag + " ", " ");
            } else { // if the tag is at the end of the file name
                deleted = true;
                newPathName = newPathName.replace(" @" + tag, "");
            }
        }

        // if a tag has been deleted, update the file name in the OS directory
        if (deleted) {
            currTags.removeAll(Arrays.asList(tagList));
            File newFile = new File(newPathName);
            updateFile(newFile);
        }
    }

    /**
     * Update the file name in the OS directory. This method also updates all the observers in the list of observers
     *
     * @param newFile the new renamed file
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    private void updateFile(File newFile) throws IOException {
        // rename currentFile path to moved file
        //currentFile.renameTo(newFile);
        prevFile = this.file;
        try {
            Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        file = newFile;

        if (!oldNames.contains(prevFile.getName())) {
            oldNames.add(prevFile.getName());
        }


        setChanged();
        notifyObservers(this);
    }

    /**
     * Change the name of the File this ImageFile object represents back to selectedName
     *
     * @param selectedName the name to be reverted to
     * @throws IOException Signals that an I/O exception of some sort has occurred
     */
    public void revertName(String selectedName) throws IOException {
        int nameEndIndex = selectedName.indexOf("@");
        int photoTypeIndex = selectedName.lastIndexOf(".");

        // if a tag exists in the name user is reverting to
        if (nameEndIndex >= 0) {
            String stringTags = selectedName.substring(nameEndIndex + 1, photoTypeIndex);
            String[] atSplit = stringTags.split(" @");

            currTags.clear();
            currTags.addAll(Arrays.asList(atSplit));

        } else {
            currTags.clear();
        }
        File revertedFile = new File(this.file.getParent() + "/" + selectedName);
        updateFile(revertedFile);
    }

    /**
     * Check if this ImageFile object is equal to other
     *
     * @param other the object to be compared to
     * @return boolean
     */
    public boolean equals(Object other) {
        if (!(other instanceof ImageFile)) {
            return false;
        }
        ImageFile otherImf = (ImageFile) other;
        if (prevFile != null) {
            return file.equals(otherImf.file) && prevFile.equals(otherImf.prevFile) &&
                    currTags.equals(otherImf.currTags) && oldNames.equals(otherImf.oldNames);
        } else {
            return file.equals(otherImf.file) && otherImf.prevFile == null &&
                    currTags.equals(otherImf.currTags) && oldNames.equals(otherImf.oldNames);
        }
    }

    /**
     * A getter for the ImageFile object's corresponding file
     *
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * A getter for the ArrayList of all tags the ImageFile objects has been tagged with
     *
     * @return currTags
     */
    public ArrayList<String> getCurrTags() {
        return currTags;
    }

    /**
     * A getter for the ArrayList of all names the ImageFile objects has been named with
     *
     * @return oldNames
     */
    public ArrayList<String> getOldNames() {
        return oldNames;
    }

    /**
     * A getter for the previous file that this ImageFile object represents
     *
     * @return prevFile
     */
    File getPrevFile() {
        return prevFile;
    }

}
