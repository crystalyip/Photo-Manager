package sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * GalleryView handles gallery of images
 */
class GalleryView {

    private TilePane tile;
    private ScrollPane scroll;

    private ObservableList<File> imageItems;

    private Controller controller;

    /**
     * Initialize GalleryView
     *
     * @param controller Controller
     */
    GalleryView(Controller controller) {
        this.controller = controller;
    }

    /**
     * Creates Image Gallery with list of image files
     *
     * @return ScrollPane that contains image gallery
     */
    private ScrollPane getImageGallery() {
        // Adapted from (Nov10/2017):
        // https://stackoverflow.com/questions/27182323/working-on-creating-image-gallery-in-javafx-not-able-to-display-image-properly
        // create a new tile pane for the image gallery
        tile = new TilePane();

        tile.setPadding(new Insets(15, 15, 15, 15));
        tile.setHgap(15);

        try {
            for (final File file : imageItems) {
                // create the image that will be loaded onto the ImageView
                Image image = new Image(new FileInputStream(file));
                // set up the ImageView
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(250);
                imageView.setPreserveRatio(true);

                // add the imageView to the tile
                tile.getChildren().addAll(imageView);

                // when an image is clicked, launch the stage so user can see detailed view of image
                imageView.setOnMouseClicked(instantiateSingleClick(file));
            }
        } catch (FileNotFoundException c) {
            c.printStackTrace();

        }

        // a new scroll pane so user can scroll through images in the gallery
        scroll = new ScrollPane();
        // scroll properties
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(500);
        if (tile.getChildren().size() == 0) {
            Text noImages = new Text("No images in selected folder");
            noImages.setFill(Color.BLUE);
            tile.getChildren().add(noImages);
        }
        scroll.setContent(tile);
        return scroll;
    }


    /**
     * Refresh image gallery with updated image file
     *
     * @param originalFile original image
     * @param updatedFile  updated image
     * @throws FileNotFoundException When file does not exist
     */
    void refreshGallery(File originalFile, File updatedFile) throws FileNotFoundException {
        int index = imageItems.indexOf(originalFile);
        imageItems.set(index, updatedFile);

        ImageView imgView = (ImageView) tile.getChildren().get(index);
        imgView.setOnMouseClicked(instantiateSingleClick(updatedFile));
    }


    /**
     * Sets up the ScrollPane for the gallery of images and returns it
     *
     * @param directoryPics list of image files to be added to image gallery
     * @return scroll ScrollPane
     */
    ScrollPane launchGalleryView(ArrayList<File> directoryPics) {
        imageItems = FXCollections.observableArrayList(directoryPics);
        scroll = getImageGallery();
        return scroll;
    }

    /**
     * Creates an instance of SingleClick
     *
     * @param image to be made clickable
     * @return instance of SingleClick
     */
    private SingleClick instantiateSingleClick(File image) {
        return new SingleClick(image);
    }

    /**
     * An EventHandler for a each image tile when clicked
     */
    public class SingleClick implements EventHandler<MouseEvent> {

        private File image;

        SingleClick(File image) {
            this.image = image;
        }

        /**
         * Event when image is clicked
         *
         * @param event MouseEvent
         */
        @Override
        public void handle(MouseEvent event) {
            try {
                controller.launchImageView(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            event.consume();
        }
    }
}
