/**
 * SeriesTracker.java Created 12:15:14 pm 2015
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.output.TeeOutputStream;

import me.mikujo.series.utils.FormatDef;
import me.mikujo.series.utils.Utils;
import me.mikujo.series.wiki.WikiParser;
import me.mikujo.series.writer.IFormatter;
import me.mikujo.series.writer.TextFormatter;

/**
 * Series tracker class that ties up all the code together
 * @author mithun.gonsalvez
 */
public class SeriesTracker {

    /** Output file path */
    private final Path output;

    /** Output format */
    private final String outputFormat;

    /** Formats for all the series */
    private final Map<String, FormatDef> formats;

    /** All the series that have to be processed */
    private final List<Map<String, Object>> allSeries;

    /**
     * Series tracker constructor
     * @param input Input JSON file that specifies the series as well as the format that each series follow
     * @param output Output file path
     * @param outputFormat Output format that defines the output format
     * @throws IOException If something goes wrong while reading the data
     */
    public SeriesTracker(Path input, Path output, String outputFormat) throws IOException {
        this.output = output;
        this.outputFormat = outputFormat;

        Map<String, Object> rawData = Utils.readData(input);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> rawFormats = (Map<String, Map<String, Object>>) rawData.get("formats");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allSeries = (List<Map<String, Object>>) rawData.get("series");

        this.allSeries = allSeries;
        this.formats = processFormats(rawFormats);

    }

    /**
     * Start processing
     * @throws IOException If there is a problem while writing the data
     */
    public void process() throws IOException {
        try (Writer writer = getWriter(output)) {
            IFormatter formatter = buildFormatter(outputFormat, writer);
            int i = 0;
            for (Map<String, Object> series : this.allSeries) {
                Object formatId = series.get("format");
                FormatDef formatDef;
                if (formatId instanceof String) {
                    formatDef = checkGetFormatDef((String) formatId);
                } else if (formatId instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> formatIdMap = (Map<String, Object>) formatId;
                    formatDef = buildFormatDef(this.formats, (i++) + ":" + System.currentTimeMillis(), formatIdMap);
                    Object extId = formatDef.getOptional("extends");
                    if (extId != null) {
                        FormatDef parentDef = checkGetFormatDef((String) extId);
                        formatDef.setParent(parentDef);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown type for def [" + formatId + "]");
                }

                Object type = formatDef.get("type");
                if ("wiki".equals(type)) { // When we add more types here, put a lookup mechanism
                    WikiParser.parse(series, formatDef, formatter);
                } else {
                    throw new IOException("Unknown Type specified for series: " + series);
                }
            }
        }
    }

    /**
     * Check and get the format definition
     * @param formatId Format id
     * @return Format definition
     */
    private FormatDef checkGetFormatDef(String formatId) {
        FormatDef formatDef = this.formats.get(formatId);
        if (formatDef == null) {
            throw new IllegalArgumentException("No format defined with id [" + formatId + "]");
        }
        return formatDef;
    }

    /**
     * Process the formats and resolve all hierarchies
     * @param formats formats to process
     * @return Map containing the format definitions keyed by its identifier
     */
    private static Map<String, FormatDef> processFormats(Map<String, Map<String, Object>> formats) {
        Map<String, FormatDef> rFormats = new HashMap<>();

        for (Entry<String, Map<String, Object>> entry : formats.entrySet()) {
            String id = entry.getKey();
            Map<String, Object> format = entry.getValue();

            FormatDef fDef = buildFormatDef(rFormats, id, format);
            Object extId = format.get("extends");
            if (extId != null) {
                FormatDef parentFormatDef = buildFormatDef(rFormats, (String) extId, formats.get(extId));
                fDef.setParent(parentFormatDef);
            }
        }

        return rFormats;
    }

    /**
     * Fetches the FormatDef from the map, if available, else builds one
     * @param rFormats Formats map
     * @param id Id of the format to fetch
     * @param format Raw format
     * @return Format definition, built or retrieved
     */
    private static FormatDef buildFormatDef(Map<String, FormatDef> rFormats, String id, Map<String, Object> format) {
        FormatDef formatDef = rFormats.get(id);
        if (formatDef == null) {
            formatDef = new FormatDef(id, format);
            rFormats.put(id, formatDef);
        }

        return formatDef;
    }

    /**
     * Build the writer instance
     * @param format Format type
     * @param writer Writer instance where the data should be written
     * @return Formatter instance
     * @throws IOException If the wrong type of format is provided
     */
    private IFormatter buildFormatter(String format, Writer writer) throws IOException {
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
    private Writer getWriter(Path output) throws IOException {
        String debug = System.getProperty("debug.writing", null);
        OutputStream os = new BufferedOutputStream(Files.newOutputStream(output));
        if (Boolean.parseBoolean(debug)) {
            os = new TeeOutputStream(System.out, os);
        }
        return new OutputStreamWriter(os, StandardCharsets.UTF_8);
    }

}
