package gitlet;


import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;
import static gitlet.Main.*;

/**
 * This class contain the String-form data in the file specified
 * by its constructor.
 *
 * @author Ang Wang
 */
public class Blob implements Serializable {

    /**
     * Directory for storing blobs.
     */
    private static File blobFolder = Utils.join(getMainFolder(), "blobs");

    /**
     * Get blobFolder.
     *
     * @return blobFolder
     */
    public static File getBlobFolder() {
        return blobFolder;
    }

    /**
     * Set current working directory to REMOTE.
     *
     * @param remote .../.gitlet
     * */
    public static void setUpBLobs(File remote) {
        blobFolder = Utils.join(remote, "blobs");
    }

    /**
     * Construct the blob from ./input file.
     *
     * @param input the file name
     */
    Blob(String input) {
        File f = join(getCwd(), input);
        if (!f.exists()) {
            _content = null;
        } else {
            _content = readContentsAsString(f);
        }
    }

    /**
     * Return whether the blob is an empty one.
     */
    public boolean empty() {
        return (_content == null);
    }

    /**
     * Return the file's content in String form.
     */
    public String content() {
        return _content;
    }

    /**
     * Save blob to blobFolder.
     * Name : SHA
     * Content : _content
     */
    public void saveBlob() {
        String s = blobSHA();
        File folder = join(blobFolder, s.substring(0, 2));
        if (!folder.exists()) {
            folder.mkdir();
        }
        writeObject(join(folder, s.substring(2)), this);
    }

    /**
     * Get the blob with hash in blobFolder.
     * If doesn't exist, return null.
     *
     * @param hash blob's hash
     */
    public static Blob readBlob(String hash) {
        File file = join(blobFolder, hash.substring(0, 2), hash.substring(2));
        if (file.exists()) {
            return ((Blob) readObject(file, Blob.class));
        }
        return null;
    }


    /**
     * SHA.
     *
     * @return This blob's hash.
     */
    public String blobSHA() {
        return Utils.sha1(_content);
    }

    /**
     * String form of the file content.
     */
    private String _content;

}
