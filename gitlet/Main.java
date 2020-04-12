package gitlet;


import java.io.File;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


import static gitlet.Commit.*;
import static gitlet.Blob.*;
import static gitlet.Utils.*;
import static gitlet.MyMap.*;
import static gitlet.Remote.*;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Ang Wang
 */
public class Main {

    /**
     * Current Working Directory.
     */
    private static File cwd = new File(".");

    /**
     * Get cwd.
     *
     * @return cwd
     */
    public static File getCwd() {
        return cwd;
    }

    /**
     * Get cwd.
     *
     * @param remote remote working directory.
     */
    public static void setCwd(File remote) {
        cwd = remote;
    }

    /**
     * Main metadata folder.
     */
    private static File mainFolder = join(cwd, ".gitlet");

    /**
     * Get mainFolder.
     *
     * @return mainFolder
     */
    public static File getMainFolder() {
        return mainFolder;
    }

    /**
     * Set mainFolder.
     *
     * @param remote remote .gitlet folder.
     */
    public static void setMainFolder(File remote) {
        mainFolder = remote;
    }

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        preCheck(args);
        switch (args[0]) {
        case "add":
            add(args);
            break;
        case "commit":
            commit(args);
            break;
        case "rm":
            remove(args);
            break;
        case "log":
            log(args);
            break;
        case "global-log":
            globalLog(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            status(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "branch":
            branch(args);
            break;
        case "rm-branch":
            removeBranch(args);
            break;
        case "reset":
            reset(args);
            break;
        case "merge":
            merge(args);
            break;
        case "add-remote":
            addRemote(args);
            break;
        case "rm-remote":
            rmRemote(args);
            break;
        case "push":
            push(args);
            break;
        case "fetch":
            fetch(args);
            break;
        case "pull":
            pull(args);
            break;
        default:
            exitWithError("No command with that name exists.");
        }
        return;
    }

    /**
     * Pre check commands.
     *
     * @param args Gitlet commands.
     * */
    private static void preCheck(String... args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        if (args[0].equals("init")) {
            if (args.length != 1) {
                exitWithError("Incorrect operands.");
            }
            init();
            System.exit(0);
        }
        if (!mainFolder.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        return;
    }

    /**
     * Pull.
     *
     * @param args pull [remote name] [remote branch name]
     */
    private static void pull(String... args) {
        if (args.length != 3) {
            exitWithError("Incorrect operands.");
        }
        String remoteName = args[1];
        String remoteBranch = args[2];
        fetch("fetch", remoteName, remoteBranch);
        String localBranch = remoteName + "/" + remoteBranch;
        merge("merge", localBranch);
    }

    /**
     * Fetch.
     *
     * @param args fetch [remote name] [remote branch name]
     */
    private static void fetch(String... args) {
        if (args.length != 3) {
            exitWithError("Incorrect operands.");
        }
        String remoteName = args[1];
        String remoteBranch = args[2];
        boolean isReady = setRemoteFolder(remoteName);
        if (!isReady) {
            exitWithError("Remote directory not found.");
        }
        readHEAD(true);
        if (!getBranches().containsKey(remoteBranch)) {
            exitWithError("That remote does not have that branch.");
        }
        Commit remoteHead = readCommit(getBranches().get(remoteBranch));
        Set<Commit> appendCommits = new HashSet<>();
        Set<Blob> appendBlobs = new HashSet<>();
        remoteHead.ancestors(appendCommits);
        for (Commit c : appendCommits) {
            c.addBlobs(appendBlobs);
        }
        setFolderBack();
        readHEAD(true);
        String localBranch = remoteName + "/" + remoteBranch;
        for (Commit c : appendCommits) {
            saveCommit(c);
        }
        for (Blob b : appendBlobs) {
            b.saveBlob();
        }
        File remoteFolder = join(getBranchFolder(), remoteName);
        if (!remoteFolder.exists()) {
            remoteFolder.mkdir();
        }
        writeContents(join(remoteFolder, remoteBranch), remoteHead.commitSHA());
    }

    /**
     * Push.
     *
     * @param args push [remote name] [remote branch name]
     */
    private static void push(String... args) {
        if (args.length != 3) {
            exitWithError("Incorrect operands.");
        }
        String remoteName = args[1];
        String remoteBranchName = args[2];
        boolean isReady = setRemoteFolder(remoteName);
        if (!isReady) {
            exitWithError("Remote directory not found.");
        }
        readHEAD(true);
        if (!getBranches().containsKey(remoteBranchName)) {
            branch("branch", remoteBranchName);
        }
        Commit remoteHead = readCommit(getBranches().get(remoteBranchName));
        String remoteHash = remoteHead.commitSHA();
        setFolderBack();
        readHEAD(false);
        Commit localHead = getHead();
        HashSet<Commit> parents = new HashSet<>();
        getHead().ancestors(parents);
        HashSet<String> p = new HashSet<>();
        getHead().ancestors(p, null, 0);
        if (!p.contains(remoteHead.commitSHA())) {
            exitWithError("Please pull down remote changes before pushing.");
        }
        Set<Commit> appendCommits = new HashSet<>();
        Set<Blob> appendBlobs = new HashSet<>();
        boolean hasChild = true;
        while (hasChild) {
            hasChild = false;
            for (Commit c : parents) {
                if (c.hasParent(remoteHash)) {
                    hasChild = true;
                    appendCommits.add(c);
                    c.addBlobs(appendBlobs);
                    remoteHash = c.commitSHA();
                }
            }
        }
        setRemoteFolder(remoteName);
        for (Commit c : appendCommits) {
            saveCommit(c);
        }
        for (Blob b : appendBlobs) {
            b.saveBlob();
        }
        setCurBranch(remoteBranchName);
        reset("reset", localHead.commitSHA());
        setFolderBack();
    }

    /**
     * Remove Remote.
     *
     * @param args java gitlet.Main rm-remote [remote name]
     */
    private static void rmRemote(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        if (containRemote(args[1])) {
            deleteRemote(args[1]);
        } else {
            exitWithError("A remote with that name does not exist.");
        }
    }

    /**
     * Add remote.
     *
     * @param args add-remote [remote name] [name of remote directory]/.gitlet
     */
    private static void addRemote(String... args) {
        if (args.length != 3) {
            exitWithError("Incorrect operands.");
        }
        String remoteName = args[1];
        if (plainFilenamesIn(REMOTE_FOLDER).contains(remoteName)) {
            exitWithError("A remote with that name already exists.");
        }
        File in = new File(args[2]);
        writeRemote(remoteName, in);
    }


    /**
     * Merge.
     *
     * @param args merge [branch name]
     */
    public static void merge(String... args) {
        String branchName = mergeCheck(args);
        Commit branch = readCommit(readBranch(branchName));
        HashMap<String, String> branchMap = branch.allMap();
        Commit cur = getHead();
        HashMap<String, String> curMap = cur.allMap();
        Set<String> fileInBranch = branch.allMap().keySet();
        Set<String> fileInCurB = cur.allMap().keySet();
        List<String> working = plainFilenamesIn(cwd);
        checkOverwriteUntracked(fileInCurB, fileInBranch, working);
        Commit splitPoint = findSP(getHead(), branch);
        if (splitPoint.commitSHA().equals(branch.commitSHA())) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        }
        if (splitPoint.commitSHA().equals(cur.commitSHA())) {
            checkoutBranch("checkout", branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        boolean isConflict = false;
        Set<String> fileInSP = splitPoint.allMap().keySet();
        for (String s : fileInSP) {
            boolean modInBranch =
                    !splitPoint.allMap().get(s).equals(branchMap.get(s));
            if (splitPoint.allMap().get(s).equals(curMap.get(s))) {
                if (!branchMap.containsKey(s)) {
                    remove("rm", s);
                } else if (modInBranch) {
                    checkoutFileWithCommit(branch, s);
                    add("add", s);
                }
            } else if (curMap.containsKey(s)) {
                if (modInBranch
                        && !curMap.get(s).equals(branchMap.get(s))) {
                    writeConflict(s, curMap.get(s), branchMap.get(s));
                    isConflict = true;
                }
            } else if (branchMap.containsKey(s) && modInBranch) {
                writeConflict(s, curMap.get(s), branchMap.get(s));
                isConflict = true;
            }
        }
        fileInCurB.removeAll(fileInSP);
        fileInBranch.removeAll(fileInSP);
        for (String s : fileInBranch) {
            if (!fileInCurB.contains(s)) {
                checkoutFileWithCommit(branch, s);
                add("add", s);
            } else if (!branchMap.get(s).equals(curMap.get(s))) {
                writeConflict(s, curMap.get(s), branchMap.get(s));
                isConflict = true;
            }
        }
        commitHelper(String.format("Merged %s into %s.",
                branchName, getCurBranch()),
                branch.commitSHA());
        showConflict(isConflict);
    }

    /**
     * Merge command check.
     *
     * @param args merge [branch name]
     * @return [branch name]
     * */
    private static String mergeCheck(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        if (getStageFolder().listFiles().length != 0
                || getRemoveFolder().listFiles().length != 0) {
            exitWithError("You have uncommitted changes.");
        }
        String branchName = args[1];
        readHEAD(true);
        if (!getBranches().containsKey(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        if (branchName.equals(getCurBranch())) {
            exitWithError("Cannot merge a branch with itself.");
        }
        return branchName;
    }

    /**
     * Print conflict message.
     *
     * @param isConflict true iff encounters conflicts.
     * */
    private static void showConflict(boolean isConflict) {
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Write conflict in FILENAME and stage that change.
     * If file in head or branch is deleted, then the input String
     * would be null and write "".
     *
     * @param headBlob head's blob's hash
     * @param branchBlob branch's blob's hash
     * @param fileName the fileName met a conflict
     */
    private static void writeConflict(String fileName,
                                      String headBlob, String branchBlob) {
        String h = "";
        if (headBlob != null) {
            h = readBlob(headBlob).content();
        }
        String b = "";
        if (branchBlob != null) {
            b = readBlob(branchBlob).content();
        }
        writeContents(join(cwd, fileName),
                String.format(CONFLICT, h, b));
        add("add", fileName);
    }


    /**
     * Reset both HEAD and CUR_BRANCH
     * to specified commit.
     *
     * @param args reset [commit id]
     */
    public static void reset(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        String id = args[1];
        Commit commit = readCommit(id);
        if (commit == null) {
            exitWithError("No commit with that id exists.");
        }
        readHEAD(false);
        checkoutCommit(commit);
        setHEAD(commit);
        saveHEAD();
        setBranch(getCurBranch(), commit.commitSHA());
    }

    /**
     * Delete that branch if exist.
     *
     * @param args rm-branch [branch name]
     */
    public static void removeBranch(String[] args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        String branchName = args[1];
        List<String> branches = plainFilenamesIn(getBranchFolder());
        if (!branches.contains(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        String cur = readContentsAsString(getCurBranchFile());
        if (cur.equals(branchName)) {
            exitWithError("Cannot remove the current branch.");
        }
        join(getBranchFolder(), branchName).delete();
    }

    /**
     * Create a branch pointing to current commit.
     *
     * @param args branch [branch name]
     */
    public static void branch(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        String branchName = args[1];
        readHEAD(true);
        if (getBranches().containsKey(branchName)) {
            exitWithError("A branch with that name already exists.");
        }
        writeContents(join(getBranchFolder(), branchName),
                getHead().commitSHA());
        getBranches().put(branchName, getHead().commitSHA());
    }

    /**
     * Init a gitlet repo called .git
     * and generate a initial commit.
     */
    public static void init() {

        if (mainFolder.exists()) {
            exitWithError("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        mainFolder.mkdir();
        getCommitFolder().mkdir();
        getBranchFolder().mkdir();
        getBlobFolder().mkdir();
        getStageFolder().mkdir();
        getRemoveFolder().mkdir();
        REMOTE_FOLDER.mkdir();
        Commit init = new Commit();
        init.saveCommit();
        setHEAD(init);
        saveHEAD();
        forwardBranch();
    }

    /**
     * Add a MyMap to log folder.
     *
     * @param args add filename
     */
    public static void add(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        String fileName = args[1];
        File readIn = join(cwd, fileName);
        if (!readIn.exists()) {
            exitWithError("File does not exist.");
        }
        readHEAD(false);
        Commit head = getHead();
        if (head.allMap().containsKey(fileName)) {
            Blob blob = new Blob(fileName);
            if (!head.allMap().get(fileName).equals(blob.blobSHA())) {
                blob.saveBlob();
                MyMap myMap = new MyMap(fileName, blob.blobSHA());
                myMap.saveMyMap();
            } else {
                MyMap.removeFile(fileName);
            }
        } else {
            Blob blob = new Blob(fileName);
            blob.saveBlob();
            MyMap myMap = new MyMap(fileName, blob.blobSHA());
            myMap.saveMyMap();
        }
        List<String> markedRemove = plainFilenamesIn(getRemoveFolder());
        if (markedRemove.contains(fileName)) {
            join(getRemoveFolder(), fileName).delete();
        }
    }

    /**
     * Commit.
     *
     * @param args commit [message]
     */
    public static void commit(String... args) {
        if (args.length != 2) {
            if (args.length == 1) {
                exitWithError("Please enter a commit message.");
            }
            exitWithError("Incorrect operands.");
        }
        if (args[1].equals("")) {
            exitWithError("Please enter a commit message.");
        }
        commitHelper(args[1], null);
    }

    /**
     * Commit helper function.
     *
     * @param log the commit's log message
     * @param secondParent the commit's secondParent's hash
     */
    private static void commitHelper(String log, String secondParent) {
        File[] listOfChanges = getStageFolder().listFiles();
        List<String> markedRemove = plainFilenamesIn(getRemoveFolder());
        if (listOfChanges.length == 0
                && markedRemove.size() == 0) {
            exitWithError("No changes added to the commit.");
        }
        readHEAD(true);
        Commit commit = new Commit(getHead(), log);
        for (int i = 0; i < listOfChanges.length; i += 1) {
            MyMap myMap = readObject(listOfChanges[i], MyMap.class);
            commit.allMap().put((String) myMap.getKey(),
                    (String) myMap.getValue());
            listOfChanges[i].delete();
        }
        for (String s : markedRemove) {
            commit.allMap().remove(s);
            join(getRemoveFolder(), s).delete();
        }
        commit.setSecondParentHash(secondParent);
        commit.saveCommit();
        setHEAD(commit);
        saveHEAD();
        forwardBranch();
    }


    /**
     * Remove.
     *
     * @param args rm filename
     */
    public static void remove(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        String fileName = args[1];
        File staged = join(getStageFolder(), sha1(fileName));
        readHEAD(false);
        Boolean isStaged = false;
        if (staged.exists()) {
            staged.delete();
            isStaged = true;
        }
        if (getHead().allMap().containsKey(fileName)) {
            markRemove(fileName);
            File file = join(cwd, fileName);
            if (file.exists()) {
                file.delete();
            }
        } else if (!isStaged) {
            exitWithError("No reason to remove the file.");
        }
    }

    /**
     * Print log of commits backward to initial.
     *
     * @param args log
     */
    public static void log(String... args) {
        if (args.length != 1) {
            exitWithError("Incorrect operands.");
        }
        readHEAD(false);
        Commit tmp = getHead();
        while (tmp != null) {
            tmp.printLog();
            tmp = tmp.getFirstParent();
        }
    }

    /**
     * Print log of all commits in commitFolder.
     *
     * @param args global-log
     */
    public static void globalLog(String... args) {
        if (args.length != 1) {
            exitWithError("Incorrect operands.");
        }
        File[] folders = getCommitFolder().listFiles();
        for (int i = 0; i < folders.length; i += 1) {
            File[] commitFiles = folders[i].listFiles();
            for (int j = 0; j < commitFiles.length; j += 1) {
                readObject(commitFiles[j], Commit.class).printLog();
            }
        }
    }

    /**
     * Find log message in commitFolder.
     *
     * @param args find msg
     */
    public static void find(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        }
        Boolean found = false;
        File[] folders = getCommitFolder().listFiles();
        for (int i = 0; i < folders.length; i += 1) {
            File[] commitFiles = folders[i].listFiles();
            for (int j = 0; j < commitFiles.length; j += 1) {
                Commit commit = readObject(commitFiles[j], Commit.class);
                if (commit.log().equals(args[1])) {
                    found = true;
                    System.out.println(commit.commitSHA());
                }
            }
        }
        if (!found) {
            exitWithError("Found no commit with that message.");
        }
    }

    /**
     * Status.
     *
     * @param args status
     */
    public static void status(String[] args) {
        statusOfBranches(args);
        List<File> listOfFileStaged =
                Arrays.asList(getStageFolder().listFiles());
        HashMap<String, String> mapOfStaged = new HashMap<>();
        ArrayList<String> stagedFileName = new ArrayList<>();
        for (File file : listOfFileStaged) {
            MyMap myMap = readObject(file, MyMap.class);
            mapOfStaged.put((String) myMap.getKey(), (String) myMap.getValue());
            stagedFileName.add((String) myMap.getKey());
        }
        Collections.sort(stagedFileName);
        for (String s : stagedFileName) {
            System.out.println(s);
        }
        System.out.println();
        List<String> removedFileName = showRemoved();
        Commit head = getHead();
        HashSet<String> trackedFileName = new HashSet<>(head.allMap().keySet());
        List<String> listOfPlainFile = plainFilenamesIn(cwd);
        ArrayList<String> mod = new ArrayList<>();
        ArrayList<String> untracked = new ArrayList<>();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s : listOfPlainFile) {
            if (mapOfStaged.containsKey(s)) {
                Blob blob = new Blob(s);
                if (!mapOfStaged.get(s).equals(blob.blobSHA())) {
                    if (blob.empty()) {
                        mod.add(String.format("%s (deleted)", s));
                    } else {
                        mod.add(String.format("%s (modified)", s));
                    }
                }
            } else {
                if (trackedFileName.contains(s)) {
                    Blob blob = new Blob(s);
                    if (!blob.blobSHA().equals(head.allMap().get(s))) {
                        if (blob.empty()) {
                            if (!removedFileName.contains(s)) {
                                mod.add(String.format("%s (deleted)",
                                        s));
                            }
                        } else {
                            mod.add(String.format("%s (modified)", s));
                        }
                    }
                } else {
                    untracked.add(s);
                }
            }
        }
        trackedFileName.removeAll(listOfPlainFile);
        for (String s : trackedFileName) {
            if (!removedFileName.contains(s)) {
                mod.add(String.format("%s (deleted)", s));
            }
        }
        showModUntracked(mod, untracked);
    }

    /**
     * Show removed files.
     *
     * @return the list of removed files.
     * */
    private static List<String> showRemoved() {
        System.out.println("=== Removed Files ===");
        List<String> removedFileName = plainFilenamesIn(getRemoveFolder());
        for (String s : removedFileName) {
            System.out.println(s);
        }
        System.out.println();
        return removedFileName;
    }

    /**
     * Show modifications and untracked files
     * in status commands.
     *
     * @param mod the list of modificaitons.
     * @param untracked the list of untracked files.
     * */
    private static void showModUntracked(ArrayList<String> mod,
                                         ArrayList<String> untracked) {
        Collections.sort(mod);
        for (String s : mod) {
            System.out.println(s);
        }
        System.out.println();
        Collections.sort(untracked);
        System.out.println("=== Untracked Files ===");
        for (String s : untracked) {
            System.out.println(s);
        }
        System.out.println();
    }


    /**
     * Read head from HEAD and print status of branches.
     *
     * @param args args.
     */
    private static void statusOfBranches(String... args) {
        if (args.length != 1) {
            exitWithError("Incorrect operands.");
        }
        readHEAD(true);
        TreeSet<String> branches = new TreeSet<>(getBranches().keySet());
        System.out.println("=== Branches ===");
        for (String s : branches) {
            if (getCurBranch().equals(s)) {
                System.out.print("*");
            }
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
    }

    /**
     * Checkout Driver.
     *
     * @param args checkout -- [file name]
     *             checkout [commit id] -- [file name]
     *             checkout [branch name]
     */
    public static void checkout(String[] args) {
        switch (args.length) {
        case 2:
            checkoutBranch(args);
            break;
        case 3:
            checkoutFile(args);
            break;
        case 4:
            checkoutCommitFile(args);
            break;
        default:
            exitWithError("Incorrect operands.");
        }
    }

    /**
     * Checkout that branch.
     *
     * @param args checkout [branch name]
     */
    private static void checkoutBranch(String... args) {
        String branchName = args[1];
        readHEAD(true);
        if (!getBranches().containsKey(branchName)) {
            exitWithError("No such branch exists.");
        }
        if (getCurBranch().equals(branchName)) {
            exitWithError("No need to checkout the current branch.");
        }
        Commit branch = readCommit(getBranches().get(branchName));
        checkoutCommit(branch);
        setCurBranch(branchName);
        setHEAD(branch);
        saveHEAD();
    }

    /**
     * Checkout all files in COMMIT and clear stage.
     * CHECK whether untracked working files would be overwritten or not.
     * Suppose that HEAD has been read.
     * Also set head and curBranch to that commit.
     */
    private static void checkoutCommit(Commit commit) {

        List<String> allFiles = plainFilenamesIn(cwd);
        Set<String> checkoutFiles = commit.allMap().keySet();
        Set<String> trackedFiles = getHead().allMap().keySet();
        checkOverwriteUntracked(trackedFiles, checkoutFiles, allFiles);
        trackedFiles.removeAll(checkoutFiles);
        for (String s : trackedFiles) {
            join(cwd, s).delete();
        }
        for (String s : checkoutFiles) {
            checkoutFileWithCommit(commit, s);
        }
        clearStage();


    }


    /**
     * Check out that file given commit id.
     *
     * @param args checkout [commit id] -- [file name]
     */
    private static void checkoutCommitFile(String... args) {
        if (!args[2].equals("--")) {
            exitWithError("Incorrect operands.");
        }
        Commit commit = readCommit(args[1]);
        if (commit == null) {
            exitWithError("No commit with that id exists.");
        }
        checkoutFileWithCommit(commit, args[3]);
    }

    /**
     * Check out that file in current commit.
     *
     * @param args checkout -- [file name]
     */
    private static void checkoutFile(String... args) {
        if (!args[1].equals("--")) {
            exitWithError("Incorrect operands.");
        }
        String fileName = args[2];
        readHEAD(false);
        checkoutFileWithCommit(getHead(), fileName);
    }

    /**
     * Check out that file in that commit.
     *
     * @param commit   The commit for checking out.
     * @param fileName The file name.
     */
    private static void checkoutFileWithCommit(Commit commit, String fileName) {
        if (!commit.allMap().containsKey(fileName)) {
            exitWithError("File does not exist in that commit.");
        }
        String blobHash = commit.allMap().get(fileName);
        Blob blob = Blob.readBlob(blobHash);
        writeContents(join(cwd, fileName), blob.content());
    }

    /**
     * Prints out MESSAGE and exits with error code 0.
     *
     * @param message message to print
     * @param args    args
     */
    public static void exitWithError(String message, Object... args) {
        try {
            throw error(message, args);
        } catch (GitletException ex) {
            System.out.println(ex.getMessage());
        }
        System.exit(0);
    }

    /**
     * Check whether there are files contained in WORKING, CHECKOUT
     * but not in CURBRANCH. If so, exit with error.
     */
    private static void checkOverwriteUntracked(Set<String> curBranch,
                                                Set<String> checkout,
                                                List<String> working) {
        for (String s : checkout) {
            if (!curBranch.contains(s) && working.contains(s)) {
                exitWithError("There is an untracked file "
                        + "in the way; delete it or add it first.");
            }
        }
    }

}
