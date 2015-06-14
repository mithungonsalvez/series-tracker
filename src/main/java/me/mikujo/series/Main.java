/**
 * Main.java Created 3:39:22 am 2015
 */

package me.mikujo.series;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class that starts everything
 * @author mithun.gonsalvez
 */
public class Main {

    /**
     * Main method
     * @param args Arguments
     * @throws Exception If something goes wrong
     */
    public static void main(String[] args) throws Exception {
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        String outputFormat = args[2];

        SeriesTracker tracker = new SeriesTracker(input, output, outputFormat);
        tracker.process();
    }

}
