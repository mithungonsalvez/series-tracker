/**
 * Main.java Created 3:39:22 am 2015
 */

package me.mikujo.series;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import me.mikujo.series.utils.Utils;
import me.mikujo.series.wiki.WikiParser;
import me.mikujo.series.writer.IFormatter;
import me.mikujo.series.writer.TextFormatter;

import org.apache.commons.io.output.TeeOutputStream;

/**
 * Main class
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
        String format = args[2];
        List<Map<String, Object>> allSeries = Utils.readData(input);
        try (Writer writer = getWriter(output)) {
            process(allSeries, buildWriter(format, writer));
        }
    }

    /**
     * Build the writer instance
     * @param format Format type
     * @param writer Writer instance where the data should be written
     * @return Formatter instance
     * @throws IOException If the wrong type of format is provided
     */
    private static IFormatter buildWriter(String format, Writer writer) throws IOException {
        IFormatter formatter;
        if (format.equals("text")) {
            formatter = new TextFormatter(writer);
        } else {
            throw new IOException("Unknown format type [" + format + "]");
        }

        return formatter;
    }

    /**
     * Returns the writer
     * @param output Output file path
     * @return Writer instance
     * @throws IOException if opening the file fails
     */
    @SuppressWarnings("resource")
    private static Writer getWriter(Path output) throws IOException {
        String debug = System.getProperty("debug.writing", null);
        OutputStream os = new BufferedOutputStream(Files.newOutputStream(output));
        if (Boolean.parseBoolean(debug)) {
            os = new TeeOutputStream(System.out, os);
        }
        return new OutputStreamWriter(os, StandardCharsets.UTF_8);
    }

    /**
     * Process
     * @param allSeries Configuration data
     * @param writer Writer to dump out all the data into
     * @throws IOException If there is a problem while writing
     */
    public static void process(List<Map<String, Object>> allSeries, IFormatter writer) throws IOException {
        for (Map<String, Object> series : allSeries) {
            Object object = series.get("type");
            if ("wiki".equals(object)) { // When we add more types here, put a lookup mechanism
                WikiParser.parse(series, writer);
            } else {
                throw new IOException("Unknown Type specified for series: " + series);
            }
        }
    }

}
