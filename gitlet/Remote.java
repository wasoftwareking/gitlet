package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static gitlet.Main.*;
import static gitlet.Blob.*;
import static gitlet.Commit.*;
import static gitlet.MyMap.*;

/**
 * Helper functions and variables for remote.
 * Set up working directory to remote or local.
 * @author Ang Wang
 */
public class Remote {

    /**
     * Local working directory.
     * */
    static final File LOCAL_CWD = new File(".");

    /**
     * Local main folder.
     * */
    static final File LOCAL_MAIN_FOLDER = join(LOCAL_CWD, ".gitlet");

    /**
     * Remote Folder.
     * Name: remote name;
     * Content: File object
     * [name of remote directory]/.gitlet
     */
    static final File REMOTE_FOLDER = join(LOCAL_MAIN_FOLDER, "remote");

    /**
     * Write file object F into file named
     * REMOTENAME in REMOTE_FOLDER.
     */
    static void writeRemote(String remoteName, File f) {
        writeObject(join(REMOTE_FOLDER, remoteName), f);
    }

    /**
     * Contain that remote or not.
     * @param remoteName Name of the remote repo.
     * @return true iff contain that remote's information.
     */
    static boolean containRemote(String remoteName) {
        if (plainFilenamesIn(REMOTE_FOLDER).contains(remoteName)) {
            return true;
        }
        return false;
    }

    /**
     * Delete REMOTE's information.
     * @param remoteName Name of the remote repo.
     */
    static void deleteRemote(String remoteName) {
        if (join(REMOTE_FOLDER, remoteName).exists()) {
            join(REMOTE_FOLDER, remoteName).delete();
        }
    }

    /**
     * Set up remote.
     * @param remoteName Name of the remote repo.
     * @return true iff set it up successfully.
     */
    static boolean setRemoteFolder(String remoteName) {
        if (!containRemote(remoteName)) {
            return false;
        }
        File f = readObject(join(REMOTE_FOLDER, remoteName), File.class);
        if (!f.exists()) {
            return false;
        }
        setCwd(f.getParentFile());
        setMainFolder(f);
        setUpCommits(f);
        setUpBLobs(f);
        setUpStages(f);
        return true;
    }

    /**
     * Set folder back to local one.
     */
    static void setFolderBack() {
        setCwd(LOCAL_CWD);
        setMainFolder(LOCAL_MAIN_FOLDER);
        setUpCommits(LOCAL_MAIN_FOLDER);
        setUpBLobs(LOCAL_MAIN_FOLDER);
        setUpStages(LOCAL_MAIN_FOLDER);
    }

}
