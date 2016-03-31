import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by prakhash on 22/03/16.
 */

class Branch implements Serializable {
    public String name;                              // Branch name
    public Commit head;                              // Head commit of the branch
    private transient Set<String> addedFiles;        // Files marked to be added
    private transient Set<String> removedFiles;      // Files marked to be removed

    /**
     * Creates a new branch given a head commit. The branch will have name
     * supplied by the user. 
     */
    public Branch(Commit h, String n) {
        name = n;
        head = h;
        if (h != null) {
            head.numChildren += 1;
        }
        addedFiles = new HashSet<String>();
        removedFiles = new HashSet<String>();
        Util.createDirectory(getBranchPath());
        save();
    }

    /**
     * Returns true if given file is marked to be added, false otherwise.
     */
    public boolean isFileMarkedAdded(String fileName) {
        load();
        return addedFiles.contains(fileName);
    }

    /**
     * Returns true if given file is marked to be removed, false otherwise.
     */
    public boolean isFileMarkedRemoved(String fileName) {
        load();
        return removedFiles.contains(fileName);
    }

    /**
     * Returns true if file is being kept track of, false otherwise.
     */
    public boolean isFileStaged(String fileName) {
        return isFileMarkedAdded(fileName) || isFileMarkedRemoved(fileName);
    }

    /**
     * Returns true if no file is currently being tracked, false otherwise.
     */
    public boolean noStagedFile() {
        load();
        return addedFiles.isEmpty() && removedFiles.isEmpty();
    }

    /**
     * Loads a collection of addedFiles and removedFiles from disk.
     */
    private void load() {
        if (addedFiles == null || removedFiles == null) {
            addedFiles = (Set<String>) Util.deserialize(getBranchPath() + "/addedFiles.ser");
            removedFiles = (Set<String>) Util.deserialize(getBranchPath() + "/removedFiles.ser");
        }
    }

    /**
     * Saves addedFiles and removedFiles to disk.
     */
    private void save() {
        Util.serialize(addedFiles, getBranchPath() + "/addedFiles.ser");
        Util.serialize(removedFiles, getBranchPath() + "/removedFiles.ser");
    }

    /**
     * Marks a file to be tracked.
     */
    public void markAddFile(String fileName) {
        load();
        addedFiles.add(fileName);
        save();
    }

    /**
     * Indicates that the file is no longer being tracked.
     */
    public void markRemoveFile(String fileName) {
        if (isFileMarkedAdded(fileName)) {
            addedFiles.remove(fileName);
        } else {
            removedFiles.add(fileName);
        }
        save();
    }

    /**
     * Prints a list of files being marked as tracked, one per line.
     */
    public void printAddedFiles() {
        load();
        for (String file : addedFiles) {
            System.out.println(file);
        }
    }

    /**
     * Prints a list of files being marked as no longer tracked, one per line.
     */
    public void printRemovedFiles() {
        load();
        for (String file : removedFiles) {
            System.out.println(file);
        }
    }

    /**
     * Removes all files from addFiles and removeFiles, also updates the disk.
     */
    public void emptyStagedFiles() {
        addedFiles = new HashSet<String>();
        removedFiles = new HashSet<String>();
        save();
    }

    /**
     * Returns a collection of fileNames being marked as tracked.
     */
    public Set<String> getAddFiles() {
        load();
        return addedFiles;
    }

    /**
     * Returns a collection of fileNames being marked as removed.
     */
    public Set<String> getRemovedFiles() {
        load();
        return removedFiles;
    }

    /**
     * Returns the path for current branch on disk, within version control
     * system folder.
     */
    public String getBranchPath() {
        return Gitlet.BRANCH_DIR + "/" + name;
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
}
