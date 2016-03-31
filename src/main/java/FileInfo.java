import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.security.MessageDigest;
import java.security.DigestInputStream;

/**
 * Created by prakhash on 22/03/16.
 */

class FileInfo implements Serializable {
    public String path;                              // Path and file name
    public String md5;                               // MD5 hash of the file
    public int latestCommit;                         // Commit id where last changed was made

    /**
     * Constructs a new file given its filename and commitID.
     */
    public FileInfo(String fileName, int commitID) {
        path = fileName;
        latestCommit = commitID;
    }

    /**
     * Restores current file from version control system folder to working folder.
     */
    public void restore(boolean isConflict) {
        try {
            Path from = Paths.get(getCachedPath(path));
            Path to;
            if (!isConflict) {
                to = Paths.get(path);
            } else {
                to = Paths.get(path + ".conflicted"); 
            }
            CopyOption[] options = new CopyOption[] {
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
            }; 
            Files.copy(from, to, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a snapshot of the file and saves in version control folder.
     */
    public void snapshot() {
        try {
            File out = new File(getCachedPath(path));
            if (!out.getParentFile().exists()) {
                out.getParentFile().mkdirs();
            }
            InputStream is = Files.newInputStream(Paths.get(path));
            OutputStream os = Files.newOutputStream(Paths.get(getCachedPath(path)));
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            md5 = Util.getHexFromBytes(md.digest());
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Given the location of the file in current working directory, returns the
     * path of the relative file in the version control system folder
     */
    public String getCachedPath(String path) {
        return Gitlet.COMMIT_DIR + "/" + String.valueOf(latestCommit) + "/files/" + path;
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
