package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Date;

import static gitlet.Utils.*;
import static gitlet.Main.*;

/**
 * This class represent a commit.
 * <p>
 * A commit consist of a log message, timestamp,
 * a mapping of file names to blob references,
 * a parent reference, and (for merges) a second parent reference.
 *
 * @author Ang Wang
 */

public class Commit implements Serializable {
    /**
     * Directory for storing commits.
     * Name : commit's SHA
     * Content : Serialization.
     */
    private static File commitFolder = join(getMainFolder(), "commits");

    /**
     * Get commitFolder.
     *
     * @return commitFolder
     * */
    public static File getCommitFolder() {
        return commitFolder;
    }

    /**
     * Directory for storing branches.
     * Name : branch's name
     * Content : the commit's SHA pointed by branch
     */
    private static File branchFolder = join(getMainFolder(), "branches");

    /**
     * Get branchFolder.
     *
     * @return branchFolder
     * */
    public static File getBranchFolder() {
        return branchFolder;
    }

    /**
     * File for storing HEAD.
     * Name : HEAD
     * Content : the commit's SHA pointed by branch
     */
    private static File headFile = join(getMainFolder(), "HEAD");

    /**
     * Get headFile.
     *
     * @return headFile
     * */
    public static File getHeadFile() {
        return headFile;
    }

    /**
     * File for storing CUR_BRANCH.
     * Name : CURBRANCH
     * Content : branch's name
     */
    private static File curBranchFile = join(getMainFolder(), "CURBRANCH");

    /**
     * Get curBranchFile.
     *
     * @return curBranchFile
     * */
    public static File getCurBranchFile() {
        return curBranchFile;
    }

    /**
     * Set current working directory to REMOTE.
     *
     * @param remote .../.gitlet
     * */
    public static void setUpCommits(File remote) {
        commitFolder = join(remote, "commits");
        branchFolder = join(remote, "branches");
        headFile = join(remote, "HEAD");
        curBranchFile = join(remote, "CURBRANCH");
    }

    /**
     * Format for displaying date in log.
     */
    static final SimpleDateFormat DATE = new SimpleDateFormat("EEE MMM dd "
            + "HH:mm:ss yyyy Z");

    /**
     * Constructor for initial commit.
     */
    Commit() {
        _firstParentHash = null;
        _secondParentHash = null;
        _allMap = null;
        _log = "initial commit";
        _allMap = new HashMap<>();
        _date = DATE.format(new Date(0));
        curBranch = "master";
        writeContents(curBranchFile, curBranch);
    }

    /**
     * General constructor for inheritance from one ANCESTOR.
     * Simply the copy of ANCESTOR.
     *
     * @param log      log message.
     * @param ancestor its ancestor.
     */
    Commit(Commit ancestor, String log) {
        _firstParentHash = ancestor.commitSHA();
        _firstParent = ancestor;
        _secondParentHash = null;
        _log = log;
        _date = DATE.format(new Date());
        _allMap = new HashMap<>(ancestor._allMap);
    }

    /**
     * Save this commit to .gitlet/commits folder
     * and give it SHA hash value.
     * Suppose that there wouldn't be any collision.
     */
    public void saveCommit() {
        _hashSHA = sha1(serialize(this));
        byte[] toWrite = serialize(this);
        File folder = join(commitFolder, _hashSHA.substring(0, 2));
        if (!folder.exists()) {
            folder.mkdir();
        }
        writeContents(join(folder, _hashSHA.substring(2)), toWrite);
    }

    /**
     * Save COMMIT to current commits folder
     * without setting SHA.
     *
     * @param c the Commit to save.
     * */
    public static void saveCommit(Commit c) {
        File folder = join(commitFolder, c._hashSHA.substring(0, 2));
        if (!folder.exists()) {
            folder.mkdir();
        }
        writeObject(join(folder, c._hashSHA.substring(2)), c);
    }


    /**
     * Read a commit from commitFolder given its hash.
     * Handle the situation that given hash.length < 40.
     * Return null if doesn't exist that commit.
     *
     * @param hash commit's hash.
     */
    public static Commit readCommit(String hash) {
        String folder = hash.substring(0, 2);
        List<String> allFolders = foldernamesIn(commitFolder);
        if (!allFolders.contains(folder)) {
            return null;
        } else {
            List<String> allCommits
                    = plainFilenamesIn(join(commitFolder, folder));
            for (String s : allCommits) {
                if (s.indexOf(hash.substring(2)) != -1) {
                    return readObject(join(commitFolder, folder, s),
                            Commit.class);
                }
            }
        }
        return null;
    }

    /**
     * Generate this commit's SHA.
     * NOTE: ONLY this commit is saved then it has
     * a hash SHA.
     *
     * @return its hash.
     */
    public String commitSHA() {
        return _hashSHA;
    }

    /**
     * Print this commit's log.
     * e.g:
     * ===
     * commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
     * Date: Wed Dec 31 16:00:00 1969 -0800
     * initial commit
     */
    public void printLog() {
        System.out.println("===");
        System.out.println(String.format("commit %s", commitSHA()));
        if (_secondParentHash != null) {
            System.out.println(String.format("Merge: %s %s",
                    _firstParentHash.substring(0, 7),
                    _secondParentHash.substring(0, 7)));
        }
        System.out.println(String.format("Date: %s", _date));
        System.out.println(_log);
        System.out.println();
    }

    /**
     * Return its first parent and set
     * _firstParent.
     */
    public Commit getFirstParent() {
        if (_firstParentHash != null) {
            File file = join(commitFolder,
                    _firstParentHash.substring(0, 2),
                    _firstParentHash.substring(2));
            _firstParent = readObject(file, Commit.class);
            return _firstParent;
        }
        return null;
    }

    /**
     * HEAD.
     */
    private static Commit head;

    /**
     * Return head.
     */
    public static Commit getHead() {
        return head;
    }


    /**
     * The branch HEAD is attached to.
     * Could be NULL when HEAD is detached.
     */
    private static String curBranch;

    /**
     * Return current branch's name.
     */
    public static String getCurBranch() {
        return curBranch;
    }


    /**
     * Map from all branches to commits' SHA.
     */
    private static HashMap<String, String> branches = new HashMap<>();

    /**
     * Return all branches.
     */
    public static HashMap<String, String> getBranches() {
        return branches;
    }

    /**
     * Read current head from HEAD and set HEAD to that commit.
     * Read current branch head attached.
     * Suppose that HEAD exists.
     *
     * @param branch 1. true -> read all branches and set up current branch
     *               and brach set.
     *               2. false -> only read HEAD.
     */
    public static void readHEAD(boolean branch) {
        String hash = readContentsAsString(headFile);
        File commitFile = join(commitFolder, hash.substring(0, 2),
                hash.substring(2));
        head = readObject(commitFile, Commit.class);
        curBranch = readContentsAsString(curBranchFile);
        if (branch) {
            List<String> listOfBranches = plainFilenamesIn(branchFolder);
            for (String s : listOfBranches) {
                String localS = readContentsAsString(join(branchFolder, s));
                branches.put(s, localS);
            }
            List<String> remotes = foldernamesIn(branchFolder);
            if (remotes == null) {
                return;
            }
            for (String s : remotes) {
                File[] files = join(branchFolder, s).listFiles();
                for (File f : files) {
                    String remoteS = readContentsAsString(f);
                    branches.put(s + "/" + f.getName(), remoteS);
                }
            }
        }
    }

    /**
     * Find split point between CUR_BRANCH and BRANCH(the commit).
     * Suppose:
     * HEAD has been read from files and set up BRANCHES.
     * BRANCH exists.
     * CUR and BRANCH are distinct branch.
     * <p>
     * Strategy:
     * First get all ancestors of branch and cur.
     * Second find common ancestors.
     * Last make use of counting map to determine SP.
     *
     * @return Split Point
     */
    public static Commit findSP(Commit cur, Commit branch) {
        HashSet<String> curParents = new HashSet<>();
        HashSet<String> branchParents = new HashSet<>();
        HashMap<String, Integer> curSHAToNum = new HashMap<>();
        HashMap<String, Integer> branchSHAToNum = new HashMap<>();
        cur.ancestors(curParents, curSHAToNum, 0);
        branch.ancestors(branchParents, branchSHAToNum, 0);
        curParents.retainAll(branchParents);
        int min = Integer.MAX_VALUE;
        String res = null;
        for (String s : curParents) {
            if (curSHAToNum.get(s) < min) {
                min = curSHAToNum.get(s);
                res = s;
            }
        }
        return readCommit(res);
    }

    /**
     * Put all this commit's ancestors and itself in
     * parents.
     * And set NUMBERS mapping form its hash to its #
     * given by COUNT.
     * For the ancestor which could be accessed in two different
     * path, its count is unpredicted in this implementation.(12/4/2019)
     *
     * @param parents  The set contains all its parents.
     * @param hashToNum Map from commit SHA to #.
     * @param count    The count of recursive calls.
     */
    public void ancestors(Set<String> parents,
                          Map<String, Integer> hashToNum, int count) {
        parents.add(this.commitSHA());
        if (hashToNum != null) {
            hashToNum.put(commitSHA(), count);
        }
        if (_firstParentHash != null) {
            readCommit(_firstParentHash).ancestors(parents,
                    hashToNum, count + 1);
        }
        if (_secondParentHash != null) {
            readCommit(_secondParentHash).ancestors(parents,
                    hashToNum, count + 1);
        }
    }

    /**
     * Get all its ancestors as COMMIT.
     *
     * @param set the set contains all commits.
     * */
    public void ancestors(Set<Commit> set) {
        set.add(this);
        if (_firstParentHash != null) {
            readCommit(_firstParentHash).ancestors(set);
        }
        if (_secondParentHash != null) {
            readCommit(_secondParentHash).ancestors(set);
        }
    }


    /**
     * Set CUR_BRANCH and write it to file.
     *
     * @param curBranchNew New current branch name.
     */
    public static void setCurBranch(String curBranchNew) {
        curBranch = curBranchNew;
        writeContents(curBranchFile, curBranch);
    }

    /**
     * Set BRANCH pointing to COMMIT.
     *
     * @param branch The branch to set.
     * @param hash   The commit's hash which branch points to.
     */
    public static void setBranch(String branch, String hash) {
        writeContents(join(branchFolder, branch), hash);
    }

    /**
     * Set head.
     *
     * @param headV New head.
     */
    public static void setHEAD(Commit headV) {
        head = headV;
    }

    /**
     * Save head.
     * Name : HEAD
     * Content : the commit's SHA
     */
    public static void saveHEAD() {
        writeContents(headFile, head.commitSHA());
    }

    /**
     * Forward CUR_BRANCH to (new) HEAD'SHA and save it.
     * Suppose already read CUR_BRANCH in
     * or already set it.
     */
    public static void forwardBranch() {
        writeContents(join(branchFolder, curBranch), head.commitSHA());
    }

    /**
     * Return Commit hash stored in branch named BRANCH.
     */
    public static String readBranch(String branch) {
        return readContentsAsString(join(branchFolder, branch));
    }

    /**
     * Local date log.
     */
    private String _date;

    /**
     * SHA hash for its parent. NULL for initial commit.
     */
    private String _firstParentHash, _secondParentHash;

    /**
     * Check whether has that PARENT.
     *
     * @return true iff this commit has a parent
     *          whose hash is PARENT
     * */
    public boolean hasParent(String parent) {
        if (_firstParentHash == null) {
            return false;
        }
        if (_firstParentHash.equals(parent)) {
            return true;
        }
        if (_secondParentHash == null) {
            return false;
        }
        return _secondParentHash.equals(parent);
    }

    /**
     * Set secondParentHash.
     *
     * @param hash second parent's hash.
     */
    public void setSecondParentHash(String hash) {
        _secondParentHash = hash;
    }

    /**
     * Store all map this commit contains.
     * Maps from file name to its blob.
     */
    private HashMap<String, String> _allMap;

    /**
     * Return the mapping in this commit.
     */
    public HashMap<String, String> allMap() {
        return _allMap;
    }

    /**
     * Add all blobs into the SET.
     *
     * @param set the set contain blobs.
     * */
    public void addBlobs(Set<Blob> set) {
        for (String s : _allMap.values()) {
            set.add(Blob.readBlob(s));
        }
    }

    /**
     * Create it when needed.
     */
    private transient Commit _firstParent, _secondParent;

    /**
     * Log message.
     */
    private String _log;

    /**
     * Return this commit's log message.
     */
    public String log() {
        return _log;
    }

    /**
     * Hash.
     */
    private String _hashSHA;
}
