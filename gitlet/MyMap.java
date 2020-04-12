package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * This class represent a map from file name to its blob's Hash.
 *
 * @author Ang Wang
 */

public class MyMap implements Serializable, Map.Entry {
    /**
     * Directory for storing staging .
     * Name : File Name' SHA.
     * Content : MyMap Object.
     */
    private static File stageFolder = join(Main.getMainFolder(), "stage");

    /**
     * Get stageFolder.
     * @return stageFolder
     */
    public static File getStageFolder() {
        return stageFolder;
    }

    /**
     * Directory for storing staging.
     * Name : File Name.
     * Content : remove.
     */
    private static File removeFolder = join(Main.getMainFolder(), "removal");

    /**
     * Get removeFolder.
     * @return removeFolder
     */
    public static File getRemoveFolder() {
        return removeFolder;
    }

    /**
     * Set current working directory to REMOTE.
     * @param remote .../.gitlet
     * */
    public static void setUpStages(File remote) {
        stageFolder = join(remote, "stage");
        removeFolder = join(remote, "removal");
    }

    /**
     * Construct with given file name and its blobHash.
     * By default, track this map.
     *
     * @param fileName The file name it maps from.
     * @param blobHash The sha of that file's content.
     */
    MyMap(String fileName, String blobHash) {
        _fileName = fileName;
        _blobHash = blobHash;
    }

    /**
     * Store this MyMap in stage folder
     * if exists the same name then overwrite.
     */
    public void saveMyMap() {
        writeObject(join(stageFolder, sha1(_fileName)), this);
    }

    /**
     * Remove the staged MyMap file
     * iff the MyMap file mapping from FILENAME to a blob exists.
     * If exist, also remove that blob.
     */
    public static void removeFile(String fileName) {
        File file = join(stageFolder, sha1(fileName));
        if (!file.exists()) {
            return;
        }
        MyMap myMap = readObject(file, MyMap.class);
        File blob = join(Blob.getBlobFolder(), myMap._blobHash.substring(0, 2));
        blob = join(blob, myMap._blobHash.substring(2));
        blob.delete();
        file.delete();
    }

    /**
     * Mark FILENAME to be removed in next commit.
     */
    public static void markRemove(String fileName) {
        writeContents(join(removeFolder, fileName), "remove");
    }

    /**
     * Clear stagin area (including removal).
     */
    public static void clearStage() {
        File[] files = stageFolder.listFiles();
        for (File f : files) {
            f.delete();
        }
        files = removeFolder.listFiles();
        for (File f : files) {
            f.delete();
        }
    }

    @Override
    public boolean equals(Object anObject) {
        return (((MyMap) anObject)._blobHash.equals(_blobHash))
                && (((MyMap) anObject)._fileName.equals(_fileName));
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(sha1(_blobHash, _fileName));
    }

    @Override
    public Object getKey() {
        return _fileName;
    }

    @Override
    public Object getValue() {
        return _blobHash;
    }

    @Override
    public Object setValue(Object value) {
        String toReturn = _blobHash;
        _blobHash = (String) value;
        return toReturn;
    }

    /**
     * The file name that mapped to its blob.
     */
    private String _fileName;

    /**
     * Its blobs file name.
     */
    private String _blobHash;

    /**
     * Create it when needed.
     * To avoid accessing secondary mem each time when need blobs.
     */
    private transient Blob _blob;
}
