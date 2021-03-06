package validation;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by bruntha on 3/31/16.
 */
public class Validation implements Serializable {
    //Method invokes System.exit(...)
    public boolean checkDM1(String line) {
        boolean result = false;

        String pattern = "System.exit\\((\\d)\\)";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            result = true;
        }
        return result;
    }

    public boolean checkDM1(File file) {
        boolean result = false;
        int lineNo = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result = checkDM1(line);
                if (result)
                    System.out.println("Method invokes System.exit(...) at line " + lineNo);
                lineNo++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Format string should use %n rather than \n
    public boolean checkFS(String line) {
        boolean result = false;
        if (line.contains("\\n")) {
            result = true;
        }
        return result;
    }

    public boolean checkFS(File file) {
        boolean result = false;
        int lineNo = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result = checkFS(line);
                if (result)
                    System.out.println("Format string should use %n rather than \\n at line " + lineNo);
                lineNo++;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Check to see if ((...) & 0) == 0
    public boolean checkBIT3(String line) {
        boolean result = false;

        String pattern = "if \\(\\((.+) & 0\\) == 0";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            result = true;
        }
        return result;
    }

    public boolean checkBIT3(File file) {
        boolean result = false;
        int lineNo = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                result = checkBIT3(line);
                if (result)
                    System.out.println("if ((...) & 0) == 0 like statement at line " + lineNo);
                lineNo++;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Switch statement found where default case is missing
    public boolean checkSF2(String line) {
        boolean result = true;

        String pattern = "switch(.+)\\{.+\\}";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            String def = line.substring(m.start(), m.end());
            if (def.contains("default:") || def.contains("default :"))
                result = false;
        }
        if (result)
            System.out.println("Switch statement found where default case is missing");
        return result;
    }

    public boolean checkSF2(File file) {
        return checkSF2(fileToString(file));
    }

    //Switch statement found where one case falls through to the next case
    public boolean checkSF1(String line) {
        boolean result = true;

        String pattern = "switch(.+)\\{.+\\}";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            String def = line.substring(m.start(), m.end());
            if (noOfSubString(def, "case") == noOfSubString(def, "break"))
                result = false;
        }
        if (result)
            System.out.println("Switch statement found where one case falls through to the next case");
        return result;
    }

    public boolean checkSF1(File file) {
        return checkSF1(fileToString(file));
    }

    //Class names should start with an upper case letter
    public boolean checkNM1(File file) {
        boolean result = false;
        try {
            if (getClassName(fileToString(file)).charAt(0) >= 65 && getClassName(fileToString(file)).charAt(0) <= 90)
                result = true;

            if (!result)
                System.out.println("Class names should start with an upper case letter :" + getClassName(fileToString(file)));

        } catch (NullPointerException e) {
            System.out.println("No Class available");
        } finally {

        }

        return result;
    }

    //IIL: NodeList.getLength() called in a loop
    public boolean checkIIL1(File file) {
        return checkIIL_Base(fileToString(file), "NodeList.getLength()");
    }

    //IIL: Method calls Pattern.compile in a loop
    public boolean checkIIL2(File file) {
        return checkIIL_Base(fileToString(file), "Pattern.compile");
    }

    //IIL: Method calls prepareStatement in a loop
    public boolean checkIIL4(File file) {
        return checkIIL_Base(fileToString(file), "prepareStatement");
    }

    public boolean checkIIL_Base(String line, String loopType, String pattern) {
        boolean result = true;

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            System.out.println(pattern + " called in " + loopType);
            result = false;
        }
        return result;
    }

    public boolean checkIIL_Base(String line, String pattern) {
        boolean result = true;

        if (haveForLoop(line) != null) {
            result = false;
            checkIIL_Base(haveForLoop(line), "For Loop", pattern);
        }
        if (haveWhileLoop(line) != null) {
            result = false;
            checkIIL_Base(haveWhileLoop(line), "While Loop", pattern);
        }
        if (haveDoWhileLoop(line) != null) {
            result = false;
            checkIIL_Base(haveDoWhileLoop(line), "Do While Loop", pattern);
        }

        return result;
    }


    public void validate(File file) {
        File tmp = removeAllComments(file);
        checkDM1(tmp);
        checkFS(tmp);
        checkBIT3(tmp);
        checkSF2(tmp);
        checkSF1(tmp);
        checkNM1(tmp);
        checkIIL1(tmp);
        checkIIL2(tmp);
        checkIIL4(tmp);
    }


//    public String getClassName(File file) {
//        String className = null;
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                className = getClassName(line);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return className;
//    }

    public String getClassName(String line) {
        String className = null;
        if (line.contains("class")) {
            int index = line.indexOf("class");
            String clsName = line.substring(index + 6);
            String stgs[] = clsName.split(" ");
            className = stgs[0];
        }
        return className;
    }


    public String fileToString(File file) {
        StringBuffer fileS = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileS.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileS.toString();
    }

    public int noOfSubString(String str, String findStr) {

        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }

        return count;
    }

    public File removeAllComments(File file) {
        StringBuffer fileString = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String tString = removeComments(line);
                fileString.append(tString);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fString = removeCStyleCommets(fileString.toString());

        try {
            File file1 = new File("tmp.txt");
            FileWriter fileWriter = new FileWriter(file1);
            fileWriter.write(fString);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File("tmp.txt");
    }

    public String removeComments(String line) {
        String activeLine = line;

        if (line.contains("//")) {
            activeLine = line.substring(0, line.indexOf("//"));
        }

        return activeLine;
    }

    public String removeCStyleCommets(String line) {
        String activeLine;

        boolean result = true;

        String pattern = "\\/\\*([\\S\\s]+?)\\*\\/";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(line);
        if (m.find()) {
            String def = line.substring(m.start(), m.end());
            activeLine = line.replace(def, "");
        } else {
            activeLine = line;
        }
        return activeLine;
    }

    public String separateBraces(String line) {
        String l = null;
        int count = 1;
        int firstBracket = line.indexOf('{') + 1;
        String tmp = line.substring(firstBracket);

        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) == '{') {
                count++;
            } else if (tmp.charAt(i) == '}') {
                count--;
            }
            if (count == 0) {
                l = line.substring(0, i + firstBracket + 1);
                break;
            }
        }
        return l;
    }

    public String haveForLoop(String file) {
        String result = null;

        String pattern = "for(.+)\\{.+\\}";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(file);
        if (m.find()) {
            String def = file.substring(m.start(), m.end());
            result = separateBraces(def);
        }

        return result;
    }

    public String haveForLoop(File file) {
        return haveForLoop(fileToString(file));
    }

    public String haveWhileLoop(String file) {
        String result = null;

        String pattern = "while(.+)\\{.+\\}";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(file);
        if (m.find()) {
            String def = file.substring(m.start(), m.end());
            result = separateBraces(def);
        }

        return result;
    }

    public String haveWhileLoop(File file) {
        return haveWhileLoop(fileToString(file));
    }

    public String haveDoWhileLoop(String file) {
        String result = null;
        if (haveWhileLoop(file) != null) {
            String wl = haveWhileLoop(file);
            file.replace(wl, "");

        }
        String pattern = "do\\{.+\\}while(.+);";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(file);
        if (m.find()) {
            String def = file.substring(m.start(), m.end());
            result = separateBraces(def);

        }

        return result;
    }

    public String haveDoWhileLoop(File file) {
        return haveDoWhileLoop(fileToString(file));
    }
//    public boolean isKeyword(String word) {
//        boolean isKeyWord = JavaUtils.isJavaKeyword(word); // checks if KeyWord returns a boolean of true if it is, false if not
//        if(isKeyWord == true) // if it is true then will print the obvious statement ect.
//        System.out.println("Sorry cannot use, it is a Keyword.");
//        else System.out.println("Is not a keyword, you can use it!");
//
//        return false;
//    }
}
