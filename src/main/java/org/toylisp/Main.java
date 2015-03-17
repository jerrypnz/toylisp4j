package org.toylisp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Main <br/>
 *
 * @author jerry created 14/11/30
 */
public class Main {

    public static void runREPL() throws IOException {
        Env rootEnv = Runtime.getRootEnv();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            System.out.print("toylisp> ");
            System.out.flush();
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            List<Object> forms;
            try {
                forms = Reader.read(line);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            for (Object form : forms) {
                Object ret = null;
                try {
                    ret = Runtime.eval(form, rootEnv);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println(ret);
            }
        }
    }

    public static void runFile(String fileName, String encoding) throws IOException {
        String code;
        try (FileInputStream input = new FileInputStream(fileName)) {
            code = readCode(input, encoding);
        }
        runCode(code);
    }

    private static void runCode(String code) {
        List<Object> forms = Reader.read(code);
        for (Object form : forms) {
            Runtime.eval(form, Runtime.getRootEnv());
        }
    }

    private static String readCode(InputStream input, String encoding) throws IOException {ByteArrayOutputStream codeBuf = new ByteArrayOutputStream(4096);
        byte[] buf = new byte[2048];
        int len;
        while ((len = input.read(buf)) > 0) {
            codeBuf.write(buf, 0, len);
        }

        return codeBuf.toString(encoding);
    }

    private static void loadLib(String classpath) throws IOException {
        InputStream ins = Main.class.getClassLoader().getResourceAsStream(classpath);
        if (ins == null) {
            throw new IllegalStateException("Unable to load library from classpath: " + classpath);
        }

        String code;
        try {
            code = readCode(ins, "UTF-8");
        } finally {
            ins.close();
        }
        runCode(code);
    }

    public static void main(String[] args) throws IOException {
        loadLib("core.lisp");

        if (args.length == 0) {
            runREPL();
        } else {
            String fileName = args[0];
            if (fileName == null || fileName.isEmpty()) {
                runREPL();
            } else {
                runFile(fileName, Charset.defaultCharset().name());
            }
        }
    }

}
