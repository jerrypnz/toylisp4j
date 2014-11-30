package org.toylisp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Main <br/>
 *
 * @author jerry created 14/11/30
 */
public class Main {

    public static void runREPL() throws IOException {
        Env rootEnv = Runtime.createRootEnv();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            System.out.print("toylisp> ");
            System.out.flush();
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            List<Object> forms = Reader.read(line);
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

    public static void main(String[] args) throws IOException {
        runREPL();
    }
}
