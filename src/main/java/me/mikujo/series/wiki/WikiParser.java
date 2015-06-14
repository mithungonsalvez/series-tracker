/**
 * WikiParser.java Created 3:50:21 am 2015
 */
package me.mikujo.series.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.mikujo.series.utils.FormatDef;
import me.mikujo.series.utils.Utils;
import me.mikujo.series.writer.IFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * WikiParser
 * @author mithun.gonsalvez
 */
public class WikiParser {

    /** Prefix for english wikipedia pages */
    private static final String WIKI_PREFIX = "https://en.wikipedia.org/wiki/";

    /** Default date format example: (2015-04-02) */
    private static final String DEFAULT_DATE_FORMAT = "MMMM dddd, yyyy (yyyy-M-dd)";

    /** Parsers that are initialized per thread level */
    private static final ThreadLocal<WikiParser> PARSERS = new ThreadLocal<WikiParser>() {

        /** {@inheritDoc} */
        @Override
        protected WikiParser initialValue() {
            return new WikiParser();
        };

    };

    /**
     * Parse the provided series
     * @param series Series to parse
     * @param formatDef contains format information to extract the data
     * @param writer Writer interface
     * @throws IOException If something goes wrong while processing
     */
    public static void parse(Map<String, Object> series, FormatDef formatDef, IFormatter writer) throws IOException {
        PARSERS.get().process(series, formatDef, writer);
    }

    /**
     * Process the series and return the data
     * @param series Series data to process
     * @param formatDef contains format information to extract the data
     * @param writer Writer instance
     * @throws IOException If something goes wrong while reading the data
     */
    private void process(Map<String, Object> series, FormatDef formatDef, IFormatter writer) throws IOException {
        String imdbId = (String) series.get(Keyz.ID);
        String title = (String) Utils.readOmdbJSON(imdbId).get("Title");
        String page = (String) series.get(Keyz.PAGE);
        String watched = (String) series.get(Keyz.WATCHED);

        String tocId = (String) formatDef.get(Keyz.TOC_ID);
        String epClz = (String) formatDef.get(Keyz.EPISODES_LINK);
        String rowClass = (String) formatDef.get(Keyz.TABLE_ROW_CLZ);
        String dateFormat = (String) formatDef.getOptional(Keyz.DATE_FORMAT);
        if (dateFormat == null) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        int episodeTitle = (int) ((Long) formatDef.get(Keyz.TABLE_TITLE)).longValue();
        int episodeAirDate = (int) ((Long) formatDef.get(Keyz.TABLE_AIRDATE)).longValue();

        int[] cols = { episodeTitle, episodeAirDate };
        int[] seasonEpisode = Utils.parseWatched(watched);

        String strUrl = WIKI_PREFIX + page;
        Path file = Utils.fetchUrl(title, strUrl, "wiki");
        try (InputStream inputStream = Files.newInputStream(file)) {
            List<List<String[]>> list = parse(inputStream, strUrl, title, tocId, epClz, rowClass, cols);
            // TODO : Save this list into a separate cache per series

            writer.write(strUrl, title, list, seasonEpisode, sdf);
        }
    }

    /**
     * Parse the provided data and return the data in the form of a list(Seasons.Episodes
     * @param in Input stream for the data
     * @param baseUrl String form of the URL
     * @param title Title of the series
     * @param tocId Table of contents Id
     * @param epClz Episodes Class name
     * @param rowClass class attribute value to be used to filter rows (can be null if it does not have a class)
     * @param columns Columns to extract
     * @return A list containing all the seasons for the provided series
     * @throws IOException If there is a problem while parsing the data
     */
    private List<List<String[]>> parse(InputStream in, String baseUrl, String title, String tocId, String epClz,
            String rowClass, int[] columns) throws IOException {
        List<List<String[]>> allSeasons = new ArrayList<>();
        Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), baseUrl);
        // get the table of contents which will help us to find the class names which have episodes tables list
        String query = new StringBuilder(32).append('#').append(tocId).append(" a[href=").append(epClz).append(']')
                .toString();
        Elements tocEpisodesLst = doc.select(query);
        if (tocEpisodesLst.size() == 0) {
            throw new IOException("Unable to find data for query [" + query + "]");
        }

        Element tocEpisodes = tocEpisodesLst.get(0);
        Elements seasonIds = tocEpisodes.parent().select("ul > li > a");

        if (seasonIds.size() == 0) {
            // two possibilities: 1. First season 2. This wiki page does not follow our standard :(
            // since we are optimistic, try fetching the 'Episodes' and seeing if we are right
            List<String[]> season = processSeason(tocEpisodes, doc, title, 1, rowClass, columns);
            if (season != null) {
                allSeasons.add(season);
            }
        }

        for (int i = 0; i < seasonIds.size(); i++) {
            Element elLink = seasonIds.get(i);
            List<String[]> season = processSeason(elLink, doc, title, i + 1, rowClass, columns);
            if (season != null) {
                allSeasons.add(season);
            }
        }
        return allSeasons;
    }

    /**
     * Process a single season
     * @param elLink Element that is pointing to the table containing all the info
     * @param doc Document
     * @param title Title of the series
     * @param season Current season being parsed (1 based index)
     * @param rowClass Row class to use to fetch the actual rows and avoid the descriptions
     * @param columns Columns from the table that we are interested in
     * @return Data for a single season
     */
    private List<String[]> processSeason(Element elLink, Document doc, String title, int season, String rowClass,
            int[] columns) {

        List<String[]> oneSeason = new ArrayList<>();
        String link = elLink.attr("href");
        if (link.charAt(0) == '#') {
            link = link.substring(1);
        }
        Element linkData = doc.getElementById(link);
        Element headingTag = linkData.parent();
        int idx = headingTag.elementSiblingIndex();
        Element elTable = findNextTable(idx + 1, headingTag.parent(), 3);
        if (elTable == null) {
            System.err.println("No Episodes found for season [" + season + "] for title [" + title + "]");
            return null;
        }
        Elements trTags;
        if (rowClass == null) {
            trTags = elTable.getElementsByTag("tr");
        } else {
            trTags = elTable.select("tr." + rowClass);
        }

        for (Element trTag : trTags) {
            String[] row = new String[columns.length];
            oneSeason.add(row);
            Elements children = trTag.children();
            for (int i = 0; i < columns.length; i++) {
                Element cell = children.get(columns[i] - 1);
                row[i] = cleanData(cell.text());
            }
        }

        return oneSeason;
    }

    /**
     * Cleanup the data<br/>
     * The &amp;nbsp; is interpreted as '\u00a0' ascii code of 160 instead of 32 for the standard space, so we clean it
     * by replacing it and other things like it into space characters
     * @param rawData Raw data to be cleaned up
     * @return Cleaned up data
     */
    private String cleanData(String rawData) {
        char[] chars = rawData.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isSpaceChar(chars[i])) {
                chars[i] = ' '; // normalize the space
            }
        }
        return String.valueOf(chars).trim();
    }

    /**
     * Find the next table element
     * @param childIndex index to start from
     * @param parent Parent element to search for
     * @param limit Limit the number of elements to search for to find the table
     * @return Table element
     */
    private Element findNextTable(int childIndex, Element parent, int limit) {
        Element elTable = null;
        int count = 0;
        while (count < limit) {
            Element element = parent.child(childIndex + count);
            String role = element.attr("role");
            String tagName = element.tagName().toLowerCase();
            if (tagName.equals("table") && !"presentation".equals(role)) {
                elTable = element;
                break;
            } else if (tagName.charAt(0) == 'h' && tagName.length() == 2) { // encountered a heading tag, so break
                break;
            }
            count++;
        }

        return elTable;
    }

}
