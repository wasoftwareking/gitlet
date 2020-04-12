# gitlet
Gitlet Commands' Usage & Description:

1.init: java gitlet.Main init  
Creates a new Gitlet version-control system in the current directory.  

2.add: java gitlet.Main add [file name]  
Adds a copy of the file as it currently exists to the staging area.  

3.commit: java gitlet.Main commit [message]  
Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit.  

4.rm: java gitlet.Main rm [file name]  
Unstage the file if it is currently staged. If the file is tracked in the current commit, mark it to indicate that it is not to be included in the next commit, and remove the file from the working directory if the user has not already done so.  

5.log: java gitlet.Main log  
Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.  

6.global-log:  java gitlet.Main global-log  
Like log, except displays information about all commits ever made. The order of the commits does not matter.  

7.find: java gitlet.Main find [commit message]  
Prints out the ids of all commits that have the given commit message, one per line.  

8.status: java gitlet.Main status  
Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged or marked for untracking.  

9.checkout:a. java gitlet.Main checkout -- [file name]  
	   b. java gitlet.Main checkout [commit id] -- [file name]  
	   c. java gitlet.Main checkout [branch name]  
a. Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.  
b. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.  
c. Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.   

10.branch: java gitlet.Main branch [branch name]  
Creates a new branch with the given name, and points it at the current head node.  

11.rm-branch: java gitlet.Main rm-branch [branch name]  
Deletes the branch with the given name.  

12.reset: java gitlet.Main reset [commit id]  
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node.  

13.merge: java gitlet.Main merge [branch name]  
Merges files from the given branch into the current branch.  

14.add-remote: java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet  
Saves the given login information under the given remote name.  

15.rm-remote:  java gitlet.Main rm-remote [remote name]  
Remove information associated with the given remote name.  

16.push: java gitlet.Main push [remote name] [remote branch name]  
Attempts to append the current branch's commits to the end of the given branch at the given remote.  

17.fetch: java gitlet.Main fetch [remote name] [remote branch name]  
Brings down commits from the remote Gitlet repository into the local Gitlet repository.  

18.pull: java gitlet.Main pull [remote name] [remote branch name]  
Fetches branch [remote name]/[remote branch name] as for the fetch command, and then merges that fetch into the current branch.  
