package validation;

import java.io.File;

/**
 * Created by bruntha on 3/31/16.
 */
public class Test {
    public static void main(String args[]) {
        Test test=new Test();
        test.test();
    }

    public void test() {
        Validation validation = new Validation();
//        System.out.println(validation.removeCStyleCommets("class util {\n" +
//                "\n" +
//                "    /**\n" +
//                "     * Given an array of raw bytes, returns string representation in HEX.\n" +
//                "     */\n" +
//                "    public static String getHexFromBytes(byte[] raw) {\n" +
//                "        String result = \"\";\n" +
//                "        for (byte byteValue : raw) {\n" +
//                "            result += String.format(\"%02x\", byteValue);\n" +
//                "        }\n" +
//                "        return result;\n" +
//                "    }"));
//        System.out.println("sbsns\nhfhfh");
//
//
//        System.out.println(validation.checkFS("        System.out.println(\"sbsns\\nhfhfh\");"));
//        System.out.println(validation.getClassName("public class Test {\n"));
//        System.out.println(validation.checkBIT3("if ((..ddd.) & 0) == 0"));


        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("switch.txt").getFile());

        validation.validate(file);
//        System.out.println(validation.checkSF1(file));
    }
}
