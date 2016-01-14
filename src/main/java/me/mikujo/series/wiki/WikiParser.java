/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.mikujo.series.wiki;

import me.mikujo.series.Episode;
import me.mikujo.series.Series;
import me.mikujo.series.utils.FormatDef;
import me.mikujo.series.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * WikiParser
 * @author mithun.gonsalvez
 */
public class WikiParser {

    /** Fetch what ever is present in the bracket */
    private static final Pattern IN_BRACKET = Pattern.compile("\\((.+?)\\)");

    /** Prefix for english wikipedia pages */
    private static final String WIKI_PREFIX = "https://en.wikipedia.org/wiki/";

    /** Only one single instance */
    private static final WikiParser PARSER = new WikiParser();

    /**
     * Parse the provided series
     * @param series Series to parse
     * @param formatDef contains format information to extract the data
     * @param rawDir Directory where the raw series data should be stored
     * @param offline Use cached data if available, if data is not available, then connect and fetch data
     * @return Series instance
     * @throws IOException If something goes wrong while processing
     */
    public static Series parse(Map<String, Object> series, FormatDef formatDef, Path rawDir, boolean offline) throws IOException {
        return PARSER.process(series, formatDef, rawDir, offline);
    }

    /**
     * Process the series and return the data
     * @param series Series data to process
     * @param formatDef contains format information to extract the data
     * @param rawDir Directory where the raw series data should be stored
     * @param offline Use cached data if available, if data is not available, then connect and fetch data
     * @return Series instance
     * @throws IOException If something goes wrong while reading the data
     */
    private Series process(Map<String, Object> series, FormatDef formatDef, Path rawDir, boolean offline) throws IOException {
        String title = (String) series.get(Keyz.TITLE);
        String page = (String) series.get(Keyz.PAGE);

        String tocId = (String) formatDef.get(Keyz.TOC_ID);
        String epClz = (String) formatDef.get(Keyz.EPISODES_LINK);
        String rowClass = (String) formatDef.get(Keyz.TABLE_ROW_CLZ);
        String strDateFormat = (String) formatDef.getOptional(Keyz.DATE_FORMAT);
        DateTimeFormatter dateFormat = null;
        if (strDateFormat != null) {
            dateFormat = DateTimeFormatter.ofPattern(strDateFormat);
        }

        int episodeTitle = ((Long) formatDef.get(Keyz.TABLE_TITLE)).intValue();
        int episodeAirDate = ((Long) formatDef.get(Keyz.TABLE_AIRDATE)).intValue();

        String strUrl = WIKI_PREFIX + page;
        Path file = Utils.fetchUrl(title, strUrl, rawDir, offline);

        try (InputStream inputStream = Files.newInputStream(file)) {
            List<List<Episode>> list = parse(inputStream, strUrl, title, tocId, epClz, rowClass, episodeTitle,
                    episodeAirDate, dateFormat);

            return new Series(strUrl, title, list);
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
     * @param colTitle Column containing the title
     * @param colDate Column containing the date
     * @param dateFormat Date format to apply
     * @return A list containing all the seasons for the provided series
     * @throws IOException If there is a problem while parsing the data
     */
    private List<List<Episode>> parse(InputStream in, String baseUrl, String title, String tocId, String epClz,
            String rowClass, int colTitle, int colDate, DateTimeFormatter dateFormat) throws IOException {

        List<List<Episode>> allSeasons = new ArrayList<>();
        Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), baseUrl);
        // get the table of contents which will help us to find the class names which have episodes tables list
        String query = '#' + tocId + " a[href=" + epClz + ']';
        Elements tocEpisodesLst = doc.select(query);
        if (tocEpisodesLst.isEmpty()) {
            // if not found, possibly no TOC is present, so lets see if we can fetch episodes list directly
            List<Episode> season = processSeason(epClz, doc, title, 1, rowClass, colTitle, colDate, dateFormat);
            if (season == null) {
                throw new IOException("Unable to find data for query [" + query + "]");
            }
            allSeasons.add(season);

        } else {
            Element tocEpisodes = tocEpisodesLst.get(0);
            Elements seasonIds = tocEpisodes.parent().select("ul > li > a");

            for (int i = 0; i < seasonIds.size(); i++) {
                Element elLink = seasonIds.get(i);
                String link = getLink(elLink);
                List<Episode> season = processSeason(link, doc, title, i + 1, rowClass, colTitle, colDate, dateFormat);
                if (season != null) {
                    allSeasons.add(season);
                }
            }

            if (seasonIds.isEmpty() || allSeasons.isEmpty()) {
                // two possibilities: 1. First season 2. This wiki page does not follow our standard :(
                // since we are optimistic, try fetching the 'Episodes' and seeing if we are right
                String link = getLink(tocEpisodes);
                List<Episode> season = processSeason(link, doc, title, 1, rowClass, colTitle, colDate, dateFormat);
                if (season != null) {
                    allSeasons.add(season);
                }
            }

        }

        return allSeasons;
    }

    /**
     * Process a single season
     * @param link Link pointing to the table containing all the info
     * @param doc Document
     * @param title Title of the series
     * @param season Current season being parsed (1 based index)
     * @param rowClass Row class to use to fetch the actual rows and avoid the descriptions
     * @param colTitle Column containing the title
     * @param colDate Column containing the date
     * @param dateFormat Date format to apply
     * @return Data for a single season
     */
    private List<Episode> processSeason(String link, Document doc, String title, int season, String rowClass,
            int colTitle, int colDate, DateTimeFormatter dateFormat) {

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

        int episodeNum = 1;
        List<Episode> oneSeason = new ArrayList<>(trTags.size());
        for (Element trTag : trTags) {
            Elements children = trTag.children();
            String episodeTitle = getText(children, colTitle);
            String episodeAiredDate = extractDate(getText(children, colDate));
            Temporal date = Utils.parseDate(episodeAiredDate, dateFormat);

            oneSeason.add(new Episode(season, episodeNum++, episodeTitle, date));
        }

        return oneSeason;
    }

    /**
     * Extracts the date from the provided raw input
     * This is achieved by extracting the contents of the brackets
     * @param rawInput Raw input from which we should extract the date
     * @return if there is data found in the brackets, then it is returned, else the raw data is returned
     */
    private String extractDate(String rawInput) {
        Matcher matcher = IN_BRACKET.matcher(rawInput);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.err.println("rawInput: [" + rawInput + "]");
        }
        return rawInput;
    }

    /**
     * Fetch the text from the elements provided which is in the position indicated by the value column
     * @param episodeRow Episode row that contains data for a single episode
     * @param column Column from which we should extract the data
     * @return String data from the column
     */
    private String getText(Elements episodeRow, int column) {
        return cleanData(episodeRow.get(column - 1).text());
    }

    /**
     * Extract the link (HREF) from the element
     * @param elLink Element from which we should extract the link
     * @return Link extracted
     */
    private String getLink(Element elLink) {
        return elLink.attr("href");
    }

    /**
     * Cleanup the data<br>
     * The &amp;&nbsp; is interpreted as '\u00a0' ascii code of 160 instead of 32 for the standard space, so we clean it
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

        for(int count = 0; count < limit ; count++) {
            Element element = parent.child(childIndex + count);
            String tagName = element.tagName().toLowerCase();
            if (tagName.equals("table") && !"presentation".equals(element.attr("role"))) {
                elTable = element;
                break;
            }
        }

        return elTable;
    }

}
