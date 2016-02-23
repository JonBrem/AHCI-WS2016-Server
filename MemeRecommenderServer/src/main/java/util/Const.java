package util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by jonbr on 27.10.2015.
 */
public class Const {

    public static PrintStream out = System.out;

    public static final int LEVEL_ERROR = 0;
    public static final int LEVEL_WARN = 1;
    public static final int LEVEL_DEBUG = 2;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_VERBOSE = 4;

    private static final boolean[] LEVELS_ACTIVE = {
      true, true, true, true, true
    };

    private static final String[] LEVEL_CODES = {
            "error", "warn", "debug", "info", "verbose"
    };

    public static void log(int level, String content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, int content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, float content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, long content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, double content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, byte content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

    public static void log(int level, char content) {
        if(LEVELS_ACTIVE[level]) {
            out.println(LEVEL_CODES[level] + "\t" + System.currentTimeMillis() + "\t" + content);
        }
    }

}
