import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Class that represent a commit in version control system. It also provides
 * some methods for performing actions with commits.
 * 
 * @author Prakhash
 * 
 */
class Commit implements Serializable {
    public int id;                                   // Commit ID
    public String message;                           // Commit message
    public Commit parent;                            // Parent commit
    public int numChildren;                          // A reference counter
    public Calendar timestamp;                       // Time commit was made
    private transient Map<String, FileInfo> files;   // Files staged in this commit

    /**
     * Constructs a new commit given commit data.
     */
    public Commit(int commit_id, String m, Commit p, Map<String, FileInfo> f) {
        id = commit_id;
        message = m;
        parent = p;
        files = f;
        timestamp = Calendar.getInstance();
        numChildren = 0;

        Util.createDirectory(getCommitPath());
        Util.createDirectory(getCommitPath() + "/files");

        save();
    }

    /**
     * Constructs a normal new commit and performs the corresponding actions.
     */
    public Commit(int commit_id, String m, Branch b) {
        this(commit_id, m, b.head, b.head == null ? new HashMap<String, FileInfo>() : b.head.getFiles());

        for (String fileName : b.getAddFiles()) {
            FileInfo file = new FileInfo(fileName, id);
            file.snapshot();
            files.put(fileName, file);
        }

        for (String fileName : b.getRemovedFiles()) {
            if (files.containsKey(fileName)) {
                files.remove(fileName);
            }
        }

        b.emptyStagedFiles();
        save();
    }

    /**
     * Returns the first common ancestor of the two supplied commits. If one
     * does not exist, null will be returned.
     */
    public static Commit firstCommonAncestor(Commit commit1, Commit commit2) {
        List<Commit> splits1 = commit1.getSplits();
        List<Commit> splits2 = commit2.getSplits();
        for (int i = 0; i < splits1.size(); i++) {
            for (int j = 0; j < splits2.size(); j++) {
                if (splits1.get(i) == splits2.get(j)) {
                    return splits1.get(i); // found
                }
            }
        }
        return null; // no common ancestor
    }

    /**
     * Returns a copy of current commit. Everything including parent but except
     * ID and timestamp will be the same.
     */
    public static Commit clone(int newCommitID, Commit commit) {
        return new Commit(newCommitID, commit.message, commit.parent, commit.getFiles());
    }

    /**
     * Loads a collection of files from disk.
     */
    private void load() {
        if (files == null) {
            files = (Map<String, FileInfo>) Util.deserialize(getCommitPath() + "/files.ser");
        }
    }

    /**
     * Returns a list of ancestors of current commit that are splits, in order.
     */
    private List<Commit> getSplits() {
        List<Commit> splits = new ArrayList<Commit>();
        Commit p = this;
        while (p != null) {
            if (p.numChildren > 0) {
                splits.add(p);
            }
            p = p.parent;
        }
        return splits;
    }

    /**
     * Saves files to disk.
     */
    private void save() {
        Util.serialize(files, getCommitPath() + "/files.ser");
    }

    /**
     * Returns a stack of ancestors up to EXCLUDE. In case EXCLUDE is
     * not in the current path, stop condition will be null.
     */
    public Stack<Commit> getAncestorsStopAt(Commit exclude) {
        Stack<Commit> ancestors = new Stack<Commit>();
        Commit p = this;
        while (p != null && p != exclude) {
            ancestors.push(p);
            p = p.parent;
        }
        return ancestors;
    }

    /**
     * Returns a collection of files that were snapshotted in this commit.
     */
    public Map<String, FileInfo> getFiles() {
        load();
        return new HashMap<String, FileInfo>(files);
    }

    /**
     * Returns the path for current commit on disk, within version control
     * system folder.
     */
    public String getCommitPath() {
        return Gitlet.COMMIT_DIR + "/" + String.valueOf(id);
    }

    /**
     * Returns true if the given file exists in current commit, false otherwise.
     */
    public boolean hasFile(String fileName) {
        load();
        return files.containsKey(fileName);
    }

    /**
     * Returns true if the given file has changed since this commit, false otherwise.
     */
    public boolean fileChanged(String fileName) {
        if (!hasFile(fileName)) {
            return true;
        }
        return !files.get(fileName).md5.equals(Util.getHexFromBytes(Util.computeFileMD5(fileName)));
    }

    /**
     * Returns true if current commit is in the history of given branch, false otherwise.
     */
    public boolean isInBranch(Branch branch) {
        Commit p = branch.head;
        while (p != null) {
            if (p == this) {
                return true;
            }
            p = p.parent;
        }
        return false;
    }

    /**
     * Restores a given file from version control system folder to working folder.
     */
    public void restoreFile(String fileName) {
        load();
        files.get(fileName).restore(false);
    }

    /**
     * Restores all committed files to working folder.
     */
    public void restoreAllFiles() {
        load();
        for (String fileName : files.keySet()) {
            restoreFile(fileName);
        }
    }

    /**
     * Merges files from supplied commit.
     */
    public void mergeFrom(Commit from, boolean resolveConflict) {
        load();
        Map<String, FileInfo> fromFiles = from.getFiles();
        Map<String, FileInfo> splitFiles = Commit.firstCommonAncestor(from, this).getFiles();
        for (String fileName : fromFiles.keySet()) {
            FileInfo fromFile = fromFiles.get(fileName);
            if (files.containsKey(fileName)) {
                FileInfo currentFile = files.get(fileName);
                if (!fromFile.md5.equals(currentFile.md5)) { // 'from' != current
                    if (splitFiles.containsKey(fileName)) {
                        FileInfo splitFile = splitFiles.get(fileName);
                        if (!splitFile.md5.equals(fromFile.md5)) { // only if 'from' changed since split
                            if (splitFile.md5.equals(currentFile.md5)) {
                                files.put(fileName, fromFile); // current not changed, copy over
                            } else if (resolveConflict) { // conflict
                                fromFile.restore(true);
                            }
                        }
                    } else if (resolveConflict) { // 'from' and current created different file with same name independently
                        fromFile.restore(true);
                    }
                }
            } else { // Current commit does not have the file, copy over
                files.put(fileName, fromFile);
            }
        }
        if (!resolveConflict) { // must be called from rebase
            save();
        }
    }

    /**
     * Prints information of current commit in the form as a log.
     */
    public void print() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("====");
        System.out.println("Commit " + String.valueOf(id) + ".");
        System.out.println(dateFormat.format(timestamp.getTime()));
        System.out.println(message);
        if (parent != null) {
            System.out.println();
        }
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
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
