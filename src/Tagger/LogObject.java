package Tagger;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * LogObject stores record of a single name change
 */
public class LogObject implements Serializable {

    private String oldName;
    private String newName;
    private String timeStamp;

    /**
     * Initializes LogObject
     *
     * @param oldName Old filename
     * @param newName New filename
     */
    public LogObject(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;

        // Adapted from (Nov9/2017) https://beginnersbook.com/2013/05/current-date-time-in-java/
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateObject = new Date();
        timeStamp = dateFormat.format(dateObject);
    }

    /**
     * Get old filename
     *
     * @return oldName
     */
    public String getOldName() {
        return oldName;
    }

    /**
     * Get new filename
     *
     * @return newName
     */
    public String getNewName() {
        return newName;
    }

    /**
     * Get timestamp of name change
     *
     * @return timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }
}