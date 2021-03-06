import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.DigestInputStream;

/**
 * Created by prakhash on 22/03/16.
 */

class util {

    /**
     * Given an array of raw bytes, returns string representation in HEX.
     */
    public static String getHexFromBytes(byte[] raw) {
        String result = "";
        for (byte byteValue : raw) {
            result += String.format("%02x", byteValue);
        }
        return result;
    }

    /**
     * Returns true if file exists in file system, false otherwise.
     */
    public static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    /**
     * Computes the MD5 hash(as byte array) of a file given the filename.
     */
    public static byte[] computeFileMD5(String fileName) {
        try {
            InputStream is = Files.newInputStream(Paths.get(fileName));
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(is, md);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dis.read(buffer)) > 0) {}
            is.close();
            return md.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new file with the given fileName and gives it the text
     * fileText.
     */
    public static void createDirectory(String directoryName) {
        File f = new File(directoryName);
        if (!f.exists()) {
            f.mkdir();
        } else if (directoryName == Gitlet.GITLET_DIR) {
            System.out.println(Messages.GITLET_EXIST);
            System.exit(0);
        }
    }

    /**
     * Serialize the object and store in fileName indicated.
     */
    public static void serialize(Object obj, String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        }catch(IOException e) {
          e.printStackTrace();
        }
    }

    /**
     * Deserialize the file given the filename and returns the object.
     */
    public static Object deserialize(String fileName) {
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object obj = in.readObject();
            in.close();
            fileIn.close();
            return obj;
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }catch(ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }        
    }
}
