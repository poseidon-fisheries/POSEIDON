package uk.ac.ox.poseidon.r;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String getStackTrace(final Throwable e) {
        try (final StringWriter sw = new StringWriter()) {
            try (final PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                return sw.toString();
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
