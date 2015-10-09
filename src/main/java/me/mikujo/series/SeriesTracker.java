/**
 * SeriesTracker.java Created 12:15:14 pm 2015
 */
package me.mikujo.series;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.mikujo.series.utils.FormatDef;
import me.mikujo.series.utils.Utils;
import me.mikujo.series.wiki.Keyz;
import me.mikujo.series.wiki.WikiParser;
import me.mikujo.series.writer.IFormatter;
import me.mikujo.series.writer.TextFormatter;

/**
 * Series tracker class that ties up all the code together
 * @author mithun.gonsalvez
 */
public class SeriesTracker {

    /** Cache directory */
    private static final String CACHE_DIR = ".cache";

    /** Output file path */
    private final Path output;

    /** Output format */
    private final String outputFormat;

    /** Formats for all the series */
    private final Map<String, FormatDef> formats;

    /** All the series that have to be processed */
    private final List<Map<String, Object>> allSeries;

    /** Use cached data if available, if data is not available, then connect and fetch data */
    private final boolean offline;

    /**
     * Series tracker constructor
     * @param input Input JSON file that specifies the series as well as the format that each series follow
     * @param output Output file path
     * @param outputFormat Output format that defines the output format
     * @param offline Use cached data if available, if data is not available, then connect and fetch data
     * @throws IOException If something goes wrong while reading the data
     */
    public SeriesTracker(Path input, Path output, String outputFormat, boolean offline) throws IOException {
        this.output = output;
        this.outputFormat = outputFormat;

        Map<String, Object> rawData = Utils.readData(input);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> rawFormats = (Map<String, Map<String, Object>>) rawData.get(Keyz.FORMATS);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allSeries = (List<Map<String, Object>>) rawData.get(Keyz.SERIES);

        this.allSeries = allSeries;
        this.formats = processFormats(rawFormats);
        this.offline = offline;
    }

    /**
     * Start processing
     * @throws IOException If there is a problem while writing the data
     */
    public void process() throws IOException {
        // TODO : Make the Cache directory location configurable
        Path wikiDir = Paths.get(CACHE_DIR, Keyz.TYPE_WIKI);
        Files.createDirectories(wikiDir);

        int i = 0;
        Series[] allSeries = new Series[this.allSeries.size()];
        for (Map<String, Object> rawSeries : this.allSeries) {
            FormatDef formatDef = getFormatDef(rawSeries, i);
            Object type = formatDef.get(Keyz.TYPE);
            try {
                if (Keyz.TYPE_WIKI.equals(type)) { // When we add more types here, put a lookup mechanism
                    Series seriesData = WikiParser.parse(rawSeries, formatDef, wikiDir, offline);
                    allSeries[i++] = seriesData;
                } else {
                    throw new IOException("Unknown Type specified for series: " + rawSeries);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // TODO : Save this and do stuff with it
            }
        }

        Arrays.sort(allSeries);
        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            IFormatter formatter = buildFormatter(outputFormat, writer);
            for (Series series : allSeries) {
                formatter.write(series);
            }
        }
    }

    /**
     * Returns the format definition
     * @param rawSeries Raw Series data
     * @param count Count
     * @return FormatDef for the defined format
     */
    private FormatDef getFormatDef(Map<String, Object> rawSeries, int count) {
        Object formatId = rawSeries.get(Keyz.FORMAT);
        FormatDef formatDef;
        if (formatId instanceof String) {
            formatDef = checkGetFormatDef((String) formatId);
        } else if (formatId instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> formatIdMap = (Map<String, Object>) formatId;
            formatDef = buildFormatDef(this.formats, count + ":" + System.currentTimeMillis(), formatIdMap);
            Object extId = formatDef.getOptional(Keyz.EXTENDS);
            if (extId != null) {
                FormatDef parentDef = checkGetFormatDef((String) extId);
                formatDef.setParent(parentDef);
            }
        } else {
            throw new IllegalArgumentException("Unknown type for def [" + formatId + "]");
        }

        return formatDef;
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
            Object extId = format.get(Keyz.EXTENDS);
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
        if (format.equals(Keyz.FORMAT_TEXT)) {
            formatter = new TextFormatter(writer);
        } else {
            throw new IOException("Unknown format type [" + format + "]");
        }

        return formatter;
    }

}
