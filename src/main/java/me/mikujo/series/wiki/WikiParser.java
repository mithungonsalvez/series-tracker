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
import me.mikujo.series.RawInfo;
import me.mikujo.series.Series;
import me.mikujo.series.utils.Configs;
import me.mikujo.series.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WikiParser
 *
 * @author mithun.gonsalvez
 */
public class WikiParser {

  /**
   * Fetch what ever is present in the bracket
   */
  private static final Pattern IN_BRACKET = Pattern.compile("\\((.+?)\\)");

  /**
   * Prefix for english wikipedia pages
   */
  private static final String WIKI_PREFIX = "https://en.wikipedia.org/wiki/";

  /**
   * Only one single instance
   */
  private static final WikiParser PARSER = new WikiParser();

  /**
   * Parse the provided series
   *
   * @param series  Series to parse
   * @param rawDir  Directory where the raw series data should be stored
   * @param offline Use cached data if available, if data is not available, then connect and fetch data
   * @return Series instance
   * @throws IOException If something goes wrong while processing
   */
  public static Series parse(RawInfo series, Path rawDir, Set<Integer> seasonSkip, boolean offline) throws IOException {
    return PARSER.process(series, rawDir, seasonSkip, offline);
  }

  /**
   * Process the series and return the data
   *
   * @param rawInfo Series data to process
   * @param rawDir  Directory where the raw series data should be stored
   * @param offline Use cached data if available, if data is not available, then connect and fetch data
   * @return Series instance
   * @throws IOException If something goes wrong while reading the data
   */
  private Series process(RawInfo rawInfo, Path rawDir, Set<Integer> seasonSkip, boolean offline) throws IOException {
    Map<String, Object> series = rawInfo.getSeriesInfo();
    String title = (String) series.get(Keyz.TITLE);
    String page = (String) series.get(Keyz.PAGE);

    Configs layoutConfig = rawInfo.getLayoutConfig();
    String tocId = (String) layoutConfig.get(Keyz.TOC_ID);
    String epClz = (String) layoutConfig.get(Keyz.EPISODES_LINK);
    String rowClass = (String) layoutConfig.get(Keyz.TABLE_ROW_CLZ);
    String strDateFormat = (String) layoutConfig.getOptional(Keyz.DATE_FORMAT);
    DateTimeFormatter dateFormat = null;
    if (strDateFormat != null) {
      dateFormat = DateTimeFormatter.ofPattern(strDateFormat);
    }

    Map<String, List<String>> hints = rawInfo.getTableHints();

    String strUrl = WIKI_PREFIX + page;
    Path file = Utils.fetchUrl(title, strUrl, rawDir, offline);

    try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
      List<List<Episode>> list = parse(is, strUrl, title, tocId, epClz, rowClass, dateFormat, hints, seasonSkip);

      return new Series(strUrl, title, list);
    }

  }

  /**
   * Parse the provided data and return the data in the form of a list(Seasons.Episodes
   *
   * @param in         Input stream for the data
   * @param baseUrl    String form of the URL
   * @param title      Title of the series
   * @param tocId      Table of contents Id
   * @param epClz      Episodes Class name
   * @param rowClass   class attribute value to be used to filter rows (can be null if it does not have a class)
   * @param dateFormat Date format to apply
   * @param hints      hints that help identify the columns to fetch
   * @return A list containing all the seasons for the provided series
   * @throws IOException If there is a problem while parsing the data
   */
  private List<List<Episode>> parse(InputStream in, String baseUrl, String title, String tocId, String epClz,
                                    String rowClass, DateTimeFormatter dateFormat, Map<String, List<String>> hints,
                                    Set<Integer> seasonSkip) throws IOException {

    List<List<Episode>> allSeasons = new ArrayList<>();
    Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.name(), baseUrl);
    // get the table of contents which will help us to find the class names which have episodes tables list
    String query = '#' + tocId + " a[href=" + epClz + ']';
    Elements tocEpisodesLst = doc.select(query);
    if (tocEpisodesLst.isEmpty()) {
      // if not found, possibly no TOC is present, so lets see if we can fetch episodes list directly
      List<Episode> season = processSeason(epClz, doc, title, 1, rowClass, dateFormat, hints);
      if (season == null) {
        throw new IOException("Unable to find data for query [" + query + "]");
      }
      allSeasons.add(season);

    } else {
      Element tocEpisodes = tocEpisodesLst.get(0);
      Elements seasonIds = tocEpisodes.parent().select("ul > li > a");

      int adjustment = 0;
      for (int i = 0; i < seasonIds.size(); i++) {
        if (seasonSkip.contains(i + 1)) {
          adjustment++;
          continue;
        }

        int seasonNo = i + 1 - adjustment;
        Element elLink = seasonIds.get(i);
        String link = getLink(elLink);
        List<Episode> season = processSeason(link, doc, title, seasonNo, rowClass, dateFormat, hints);
        if (season != null) {
          allSeasons.add(season);
        }
      }

      if (allSeasons.isEmpty()) {
        // two possibilities: 1. First season 2. This wiki page does not follow our standard :(
        // since we are optimistic, try fetching the 'Episodes' and seeing if we are right
        String link = getLink(tocEpisodes);
        List<Episode> season = processSeason(link, doc, title, 1, rowClass, dateFormat, hints);
        if (season != null) {
          allSeasons.add(season);
        }
      }

    }

    return allSeasons;
  }

  /**
   * Process a single season
   *
   * @param link       Link pointing to the table containing all the info
   * @param doc        Document
   * @param title      Title of the series
   * @param season     Current season being parsed (1 based index)
   * @param rowClass   Row class to use to fetch the actual rows and avoid the descriptions
   * @param dateFormat Date format to apply
   * @param hints      hints that help identify the columns to fetch
   * @return Data for a single season
   */
  private List<Episode> processSeason(String link, Document doc, String title, int season, String rowClass,
                                      DateTimeFormatter dateFormat, Map<String, List<String>> hints) {

    if (link.charAt(0) == '#') {
      link = link.substring(1);
    }
    Element linkData = doc.getElementById(link);
    if (linkData == null) {
      System.err.println("No Episodes found for season [" + season + "] for title [" + title + "]");
      return null;
    }

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

    Map<String, Integer> colIdentifiers = getColumnIdentifiers(trTags, hints);

    if (trTags.isEmpty() && colIdentifiers.isEmpty()) {
      System.err.println("No Episodes found for season [" + season + "] for title [" + title + "]");
      return null;
    }

    int colTitle = Utils.getOrThrow(colIdentifiers, Keyz.TABLE_COL_TITLE);
    int colDate = Utils.getOrThrow(colIdentifiers, Keyz.TABLE_COL_AIRDATE);

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

  private Map<String, Integer> getColumnIdentifiers(Elements trTags, Map<String, List<String>> hints) {
    if (trTags.size() > 0) {
      Element tr0Tag = trTags.get(0).previousElementSibling();
      Map<String, Integer> result = new HashMap<>(tr0Tag.childNodeSize());
      for (Map.Entry<String, List<String>> entry : hints.entrySet()) {
        String key = entry.getKey();
        for (Element childTag : tr0Tag.children()) {
          String text = childTag.text().toLowerCase();
          for (String identifier : entry.getValue()) {
            if (text.contains(identifier)) {
              result.put(key, childTag.elementSiblingIndex() + 1);
            }
          }
        }
      }

      return result;
    } else {
      return Collections.emptyMap();
    }
  }

  /**
   * Extracts the date from the provided raw input This is achieved by extracting the contents of the brackets
   *
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
   *
   * @param episodeRow Episode row that contains data for a single episode
   * @param column     Column from which we should extract the data
   * @return String data from the column
   */
  private String getText(Elements episodeRow, int column) {
    return cleanData(episodeRow.get(column - 1).text());
  }

  /**
   * Extract the link (HREF) from the element
   *
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
   *
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
   *
   * @param childIndex index to start from
   * @param parent     Parent element to search for
   * @param limit      Limit the number of elements to search for to find the table
   * @return Table element
   */
  private Element findNextTable(int childIndex, Element parent, int limit) {
    Element elTable = null;

    for (int count = 0; count < limit; count++) {
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
