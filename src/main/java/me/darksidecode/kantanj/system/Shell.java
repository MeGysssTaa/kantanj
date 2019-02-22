package me.darksidecode.kantanj.system;

import me.darksidecode.kantanj.logging.BasicLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Shell {

    private Shell() {}

    public static Process execute(String executor, File directory, String cmd, BasicLogger outLogger) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
            pb.directory(directory);

            Process p = pb.start();

            optout(executor + "/INFO", p.getInputStream(), outLogger);
            optout(executor + "/WARNING", p.getErrorStream(), outLogger);

            return p;
        } catch (final Exception ex) {
            throw new RuntimeException("bash command execution failure", ex);
        }
    }

    private static void optout(String name, InputStream stream, BasicLogger outLogger) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line;

        while ((line = in.readLine()) != null) {
            if (name.contains("ERROR"))
                outLogger.warning("(Bash) (%s) %s", name, line);
            else outLogger.info("(Bash) (%s) %s", name, line);
        }
    }

}
