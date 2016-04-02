import validation.Validation;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by prakhash on 22/03/16.
 */

public class Gitlet implements Serializable {

    private List<Commit> commits;                    // A list of all commits
    private Map<String, Branch> branches;            // A collection of all branches
    private Map<String, Remote> remotes;             // A collection of all remotes
    private Map<String, List<Commit>> msgToCommit;   // A mapping from commit messages to commits
    private Branch currentBranch;                    // Current branch
private Validation validation=new Validation();
    /* Constants that are used in this class */
    public static final String GITLET_DIR = "./gitlet";
    public static final String BRANCH_DIR = GITLET_DIR + "/branches";
    public static final String COMMIT_DIR = GITLET_DIR + "/commits";

    /**
     * Constructs a default Gitlet version control system. Initializes some
     * data structures used to manipulate information in memory.
     */
    public Gitlet() {
        commits = new ArrayList<Commit>();
        branches = new HashMap<String, Branch>();
        remotes = new HashMap<String, Remote>();
        currentBranch = null;
        msgToCommit = new HashMap<String, List<Commit>>();
    }

    /**
     * Constructs a mapping from commit message to commit itself. There
     * may be more than one commit with the same message, so a bucket is
     * used to potentially collect and group all of them.
     */
    private void addCommitToMap(Commit commit) {
        if (!msgToCommit.containsKey(commit.message)){
            msgToCommit.put(commit.message, new ArrayList<Commit>());
        }
        msgToCommit.get(commit.message).add(commit);
    }

    /**
     * Initializes the version control system. Prepares data structures
     * as well as file system for future usage.
     */
    private static void initialize() {
        if (Util.fileExists(GITLET_DIR)) {
            System.out.println(Messages.GITLET_EXIST);
        } else {
            Util.createDirectory(GITLET_DIR);
            Util.createDirectory(BRANCH_DIR);
            Util.createDirectory(COMMIT_DIR);
            Gitlet init = new Gitlet();
            Branch defaultBranch = new Branch(null, "master");
            init.currentBranch = defaultBranch;
            init.branches.put(defaultBranch.name, defaultBranch);
            String message = "initial commit";
            Commit firstCommit = new Commit(init.commits.size(), message, defaultBranch);
            defaultBranch.head = firstCommit;
            init.commits.add(firstCommit);
            init.addCommitToMap(firstCommit);
            init.save();
        }
    }
    /**
     * Mark a file to be added, which will be snapshotted at the time
     * it is committed if it still remains on the list.
     */
    private void add(String fileName) {
        if (!Util.fileExists(fileName)) {
            System.out.println(Messages.NO_FILE);
        } else if (!currentBranch.head.fileChanged(fileName)) {
            System.out.println(Messages.FILE_UNCHANGED);
        } else {
            validation.validate(new File(fileName));
            currentBranch.markAddFile(fileName);
        }
    }


    /**
     * Performs a commit action. All files marked to be added will be
     * copied over to the version control system folders.
     */
    private void commit(String message) {
        if (currentBranch.noStagedFile()) {
            System.out.println(Messages.NOTHING_TO_COMMIT);
        } else if (message.equals("")) {
            System.out.println(Messages.NO_COMMIT_MSG);
        } else {
            Commit newCommit = new Commit(commits.size(), message, currentBranch);
            currentBranch.head = newCommit;
            addCommitToMap(newCommit);
            commits.add(newCommit);
            this.save();
        }
    }

    /**
     * Marks a file to be off the 'keeping track' list, either removes
     * from the addedFile list, or stop inheriting in future commits. 
     */
    private void remove(String fileName) {
        if (!currentBranch.isFileMarkedAdded(fileName) && !currentBranch.head.hasFile(fileName)) {
            System.out.println(Messages.CANNOT_REMOVE_FILE);
        } else {
            currentBranch.markRemoveFile(fileName);
        }
    }

    /**
     * Prints a history of commits starting from the initial commit to
     * head of current branch at present. 
     */
    private void log() {
        Commit head = currentBranch.head;
        while (head != null) {
            head.print();
            head = head.parent;
        }
    }

    /**
     * Prints a history of all commits starting from the initial commit to
     * present, regardless of whether the commits are on current branch. 
     */
    private void globalLog() {

        File file=new File("gitlet/myfile.csv");
        file.delete();
        for (int i = commits.size()-1; i >= 0; i--) {
            commits.get(i).print();
        }
    }

    /**
     * Searches for commits that has the given MESSAGE, prints out all
     * commits found. 
     */
    private void find(String message) {
        if (!msgToCommit.containsKey(message)) {
            System.out.println(Messages.CANNOT_FIND_COMMIT);
        } else {
            for (Commit commit : msgToCommit.get(message)) {
                commit.print();
            }
        }
    }

    /**
     * Prints the current status of the version control system, including
     * all active branches, files marked to be added or files marked to be
     * removed in the upcoming commit. 
     */
    private void status() {
        System.out.println("=== Branches ===");
        for (Branch branch : branches.values()) {
            if (branch == currentBranch) {
                System.out.print('*');
            }
            System.out.println(branch.name);
        }
        System.out.println("\n=== Staged Files ===");
        currentBranch.printAddedFiles();
        System.out.println("\n=== Files Marked for Removal ===");
        currentBranch.printRemovedFiles();
    }

    /**
     * Restores given file from head commit in current branch to working direcotry.
     */
    private void checkout(String fileName) {
        if (branches.containsKey(fileName)) {
            checkout(branches.get(fileName));
        } else if (currentBranch == null || !currentBranch.head.hasFile(fileName)) {
            System.out.println(Messages.CANNOT_CHECKOUT);
        } else if (warnUser()) {
            currentBranch.head.restoreFile(fileName);   
        }
    }

    /**
     * Restores all files from head commit to working directory.
     * And switches to the branch specified. 
     */
    private void checkout(Branch branch) {
        if (branch == currentBranch) {
            System.out.println(Messages.ALREADY_ON_BRANCH);
        } else if (branch == null) {
            System.out.println(Messages.CANNOT_CHECKOUT);
        } else if (warnUser()) {
            branch.head.restoreAllFiles();
            currentBranch = branch;
            this.save();
        }
    }

    /**
     * Restores a certain file from given commit id, the file will
     * overwrite existing file on current working directory. 
     */
    private void checkout(int commitID, String fileName) {
        if (commitID < 0 || commitID >= commits.size()) {
            System.out.println(Messages.COMMIT_MISSING);
        } else {
            Commit commit = commits.get(commitID);
            if (!commit.hasFile(fileName)) {
                System.out.println(Messages.COMMIT_NO_FILE);
            } else if (warnUser()) {
                commit.restoreFile(fileName);
            }
        }
    }

    /**
     * Creates a new branch given the BRANCHNAME. By default the head of
     * new branch will point at current branch, and it does not automatically
     * switch to the new branch. 
     */
    private void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println(Messages.BRANCH_EXIST);
        } else {
            Branch newBranch = new Branch(currentBranch.head, branchName);
            branches.put(branchName, newBranch);
            this.save();
        }
    }

    /**
     * Removes a given branch in the version control system. Only branches
     * other than current branch can be removed.
     */
    private void removeBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println(Messages.CANNOT_FIND_BRANCH);
        } else if (branchName.equals(currentBranch.name)) {
            System.out.println(Messages.CANNOT_REMOVE_BRANCH);
        } else {
            branches.remove(branchName);
            this.save();
        }
    }

    /**
     * Resets head of current branch to a certain commit.
     */
    private void reset(int commitID) {
        if (commitID < 0 || commitID >= commits.size()) {
            System.out.println(Messages.COMMIT_MISSING);
        } else if (warnUser()) {
            Commit commit = commits.get(commitID);
            currentBranch.head = commit;
            commit.restoreAllFiles();
            this.save();
        }
    }

    /**
     * Merges files from given branch to current branch. No new commit
     * is automatially generated, nor do the heads change. This is slightly
     * different from real git. 
     */
    private void merge(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println(Messages.CANNOT_FIND_BRANCH);
        } else if (branchName.equals(currentBranch.name)) {
            System.out.println(Messages.CANNOT_MERGE_SELF);
        } else if (warnUser()) {
            Commit from = branches.get(branchName).head;
            Commit to = currentBranch.head;
            to.mergeFrom(from, true); // merge and resolve conflict
            to.restoreAllFiles();
        }
    }


    private void compare_branches(Gitlet gitlet){
        gitlet.globalLog();

        String line = "";
        HashSet<String> set=new HashSet();


        try {
            BufferedReader br=new BufferedReader((new FileReader("gitlet/myfile.csv")));

            ArrayList<CommitDetails> commitDetails=new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] data=line.split(",");
                String[] dateAndTime=data[0].split(" ");

                DateFormat df = new SimpleDateFormat("YYYY/MM/dd kk:mm:ss", Locale.ENGLISH);

                CommitDetails commitDetailsData=null;
                try {
                    df.parse(data[0]);
                    commitDetailsData=new CommitDetails(df.parse(data[0]),data[1],data[2]);
                    commitDetails.add(commitDetailsData);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                set.add(data[1]);
            }


            ProductivityMeasure[] productivityMeasure=new ProductivityMeasure[set.size()];
            int p=0;
            for(String s:set){
                productivityMeasure[p] = new ProductivityMeasure();
                productivityMeasure[p].setBranchname(s);

                for(int i=0;i<commitDetails.size();i++) {
                    if (s.equals(commitDetails.get(i).branch)) {
                        productivityMeasure[p].increaseCommits();


                        for (int j = i + 1; j < commitDetails.size(); j++) {
                            if (s.equals(commitDetails.get(j).branch)) {
                                Date date1 = commitDetails.get(i).getCommitedDate();
                                Date date2 = commitDetails.get(j).getCommitedDate();

                                String data1 = commitDetails.get(i).getMessage();
                                String data2 = commitDetails.get(i).getMessage();

                                int minDatalength = 0;
                                if (data1.length() < data2.length())
                                    minDatalength = data1.length();
                                else
                                    minDatalength = data2.length();


                                int distanceLenght = LevenshteinDistance.computeDistance(data1, data2);
                                double ratio = distanceLenght / minDatalength * 1.0;

                                if (ratio > 0.5 && date1.getTime() - date2.getTime() < 120000) {
                                    productivityMeasure[p].commitIncreament();
                                } else if (ratio <= 0.5 && date1.getTime() - date2.getTime() < 120000) {
                                    productivityMeasure[p].commitDeduction();
                                }

                                if (date1.getTime() - date2.getTime() > 840000 && ratio > 0.5) {
                                    productivityMeasure[p].commitDeduction();
                                } else if (date1.getTime() - date2.getTime() > 840000 && ratio < 0.5) {
                                    productivityMeasure[p].commitIncreament();
                                }
                            }

                        }
                    }
                }
                System.out.println(s+"  "+productivityMeasure[p].getScore());
               p++;

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void push(String remoteName, String remoteBranchName) {

    }

    private void pull(String remoteName, String remoteBranchName) {

    }

    private void clone(String remoteName) {

    }

    /**
     * Warns the user that current operation is dangerous, which could
     * Potentially over overwrite their files in working directory.
     * User needs to confirm by typing "yes" before proceeding.
     */
    private boolean warnUser() {
        Scanner sc = new Scanner(System.in);
        System.out.println(Messages.DANGEROURS);
        while (!sc.hasNextLine()) {}
        return sc.next().toUpperCase().equals("YES");
    }

    /**
     * Saves the current state of the version control system to disk by
     * serializing necessary information.
     */
    private void save() {
        Util.serialize(this, GITLET_DIR + "/gitlet.ser");
    }

    /**
     * Required by interface Serializable, used to serialize object.
     */ 
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    /**
     * Required by interface Serializable, used to deserialize object.
     */ 
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * Handles commands that have no argument.
     */
    private static void noArgument(Gitlet gitlet, String[] args) {
        switch (args[0]) {
            case "log":
                gitlet.log();
                break;
            case "global-log":
                gitlet.globalLog();
                break;
            case "status":
                gitlet.status();
                break;
            case "commit": // for error handling (in case user did not enter message)
                gitlet.commit("");
                break;
            case "compare":
                gitlet.compare_branches(gitlet);
                break;
            default:
                System.out.println(Messages.INVALID_COMMAND);
                break;
        }
    }

    /**
     * Handles commands that have one argument.
     */
    private static void oneArgument(Gitlet gitlet, String[] args) {
        switch (args[0]) {
            case "add":
                gitlet.add(args[1]);
                break;
            case "commit":
                gitlet.commit(args[1]);
                break;
            case "rm":
                gitlet.remove(args[1]);
                break;
            case "find":
                gitlet.find(args[1]);
                break;
            case "branch":
                gitlet.branch(args[1]);
                break;
            case "rm-branch":
                gitlet.removeBranch(args[1]);
                break;
            case "reset":
                gitlet.reset(Integer.parseInt(args[1]));
                break;
            case "merge":
                gitlet.merge(args[1]);
                break;
            case "clone":
                gitlet.clone(args[1]);
                break;
            case "checkout":
                gitlet.checkout(args[1]);
                break;
            default:
                System.out.println(Messages.INVALID_COMMAND);
                break;
        }
    }

    /**
     * Handles commands that have two arguments.
     */
    private static void twoAruments(Gitlet gitlet, String[] args) {
        switch (args[0]) {
            case "checkout":
                gitlet.checkout(Integer.parseInt(args[1]), args[2]);
                break;
            case "push":
                gitlet.push(args[1], args[2]);
                break;
            case "pull":
                gitlet.pull(args[1], args[2]);
                break;
            default:
                System.out.println(Messages.INVALID_COMMAND);
                break;
        }
    }


    /**
     * Main entrance to the version control system.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(Messages.ARGUMENT_MISSING);
        } else {
            if (args[0].equals("init")) {
                initialize();
            } else {
                Gitlet gitlet = (Gitlet) Util.deserialize(GITLET_DIR + "/gitlet.ser");
                if (args.length == 1) {
                    noArgument(gitlet, args);
                } else if (args.length == 2) {
                    oneArgument(gitlet, args);
                } else if (args.length == 3) {
                    twoAruments(gitlet, args);
                }  else {
                    System.out.println(Messages.WRONG_ARGUMENT_LENGTH);
                }
            }
        }
    }
}
