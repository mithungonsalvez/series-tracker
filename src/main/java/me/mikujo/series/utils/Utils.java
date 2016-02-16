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

package me.mikujo.series.utils;

import me.mikujo.series.Episode;
import me.mikujo.series.Series;
import me.mikujo.series.filters.CompositeFilter;
import me.mikujo.series.filters.IFilter;
import me.mikujo.series.filters.SeriesFilter;
import me.mikujo.series.wiki.Keyz;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Utilities to help with doing some of our work
 * @author mithun.gonsalvez
 */
public class Utils {

    /** Multiplier value of the unifier episode setting */
    private static final int SEASON_MULTIPLER = Integer.parseInt(System.getProperty(Keyz.SEASON_MULTIPLIER, "10000"));

    /** An Allow All filter */
    @SuppressWarnings("rawtypes")
    public static final IFilter ALLOW_ALL_FILTER = new IFilter<Object>() {

        @Override
        public boolean allow(Object input) {
            return true;
        }

    };

    /** Pattern to match season and episode */
    private static final Pattern WATCHED_PATTERN = Pattern.compile("S(\\d+)E(\\d+)", Pattern.CASE_INSENSITIVE);

    /** Date time formatter instances that are the default patterns that are found */
    public static final DateTimeFormatter[] DATE_TIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-M-dd"),
        DateTimeFormatter.ofPattern("MMMM d, yyyy"),};

    /** Only contains patters for year */
    public static final DateTimeFormatter YEAR_ONLY = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Fetch the URL as specified by the String URL and return the saved copy<br>
     * If the URL contents have not changed, then fetch it from the cache
     * @param title Title of the URL
     * @param strUrl URL in string form
     * @param outDir Directory where the data should be created created
     * @param offline Use cached data if available, if data is not available, then connect and fetch data
     * @return Path containing the URL contents
     * @throws IOException If something goes wrong
     */
    public static Path fetchUrl(String title, String strUrl, Path outDir, boolean offline) throws IOException {
        System.err.println("Fetching: " + title + " Url: " + strUrl);

        Path cacheFile = outDir.resolve(title);
        Path cacheFileDate = outDir.resolve(title + "_date");

        boolean exists = Files.exists(cacheFile) && Files.exists(cacheFileDate);
        if (exists && offline) {
            return cacheFile;
        }

        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (exists) {
            String dateValue = Files.readAllLines(cacheFileDate).get(0);
            connection.addRequestProperty("If-Modified-Since", dateValue);

            if (connection.getResponseCode() != 304) { // modified since last visit, so cache the new data
                writeFiles(connection, cacheFile, cacheFileDate);
            }
        } else {
            writeFiles(connection, cacheFile, cacheFileDate);
        }

        return cacheFile;
    }

    /**
     * Read from the connection and write the data to the files
     * @param connection Connection to use
     * @param cacheFile Cache file path
     * @param cacheFileDate Cache file date path
     * @throws IOException If there is any problem while reading writing/reading the data
     */
    private static void writeFiles(URLConnection connection, Path cacheFile, Path cacheFileDate) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, cacheFile, StandardCopyOption.REPLACE_EXISTING);
        }
        String lastModified = connection.getHeaderField("Last-Modified");
        if (lastModified == null) {
            System.out.println("Last Modified time not available, unable to cache");
        } else {
            Files.write(cacheFileDate, lastModified.getBytes());
        }
    }

    /**
     * Parse the watched input with the appropriate pattern
     * @param watched Watched pattern of the form S{Season-Number}E{Episode-Number}
     * @return the unified index
     */
    public static int parseWatched(String watched) {
        Matcher matcher = WATCHED_PATTERN.matcher(watched);
        if (matcher.matches()) {
            return Utils.toUnifiedEpisodeIndex(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } else {
            throw new IllegalArgumentException("Provided input indicating 'watched' [" + watched
                    + "] does not match valid format");
        }
    }

    /**
     * Read the data from the path and return the object
     * @param <T> Type of data that is returned
     * @param jsonPath Path that contains the JSON data
     * @return JSON Object
     * @throws IOException If there is a problem while parsing
     */
    @SuppressWarnings("unchecked")
    public static <T> T readData(Path jsonPath) throws IOException {
        JSONParser parser = new JSONParser();
        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            return (T) parser.parse(reader);
        } catch (ParseException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Parse the raw string date using the date time formatter
     * @param episodeAiredDate Raw episode date
     * @param dateFormats Formats
     * @return Instant or null
     */
    public static Temporal parseDate(String episodeAiredDate, DateTimeFormatter... dateFormats) {
        if (episodeAiredDate.length() >= 4) { // TODO : Think of a better way than this to eliminate illegal dates
            if (dateFormats == null || (dateFormats.length > 0 && dateFormats[0] == null)) {
                dateFormats = Utils.DATE_TIME_FORMATTERS;
            }

            String msg = null;
            for (DateTimeFormatter dateFormat : dateFormats) {
                try {
                    TemporalAccessor accessor = dateFormat.parse(episodeAiredDate);
                    return LocalDate.from(accessor);
                } catch (DateTimeException ex) {
                    msg = ex.getMessage();
                }
            }

            // TODO : make this generic, this method sucks
            try {
                TemporalAccessor accessor = Utils.YEAR_ONLY.parse(episodeAiredDate);
                return Year.from(accessor);
            } catch (DateTimeException ex) {
                // ignore this exception
            }

            System.err.println(msg);
        }

        return null;
    }

    /**
     *
     * @param userRawData
     * @return
     */
    public static Map<String, IFilter<Episode>> readFilters(Map<String, Object> userRawData) {

        @SuppressWarnings("unchecked")
        List<Map<String, String>> rawWatchedInfo = (List<Map<String, String>>) userRawData.get(Keyz.WATCHED);

        Map<String, IFilter<Episode>> filtersMap = new HashMap<>();
        for (Map<String, String> watchedMap : rawWatchedInfo) {
            String rawWatched = watchedMap.get(Keyz.WATCHED);
            String[] rawParts = rawWatched.split(",");
            List<IFilter<Episode>> filters = new ArrayList<>(rawParts.length);
            for (int i = 0; i < rawParts.length; i++) {
                rawParts[i] = rawParts[i].trim();
                if (!rawParts[i].isEmpty()) {
                    filters.add(new SeriesFilter(rawParts[i]));
                }
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            IFilter<Episode>[] filtersArr = filters.toArray(new IFilter[filters.size()]);
            filtersMap.put(watchedMap.get(Keyz.TITLE), new CompositeFilter<>(filtersArr));
        }

        return filtersMap;
    }

    /**
     * Get the first episode from the series that passes through the filter
     * @param series Series
     * @param filter Filter
     * @return First episode
     */
    public static Episode getFirstEpisode(Series series, IFilter<Episode> filter) {
        for (List<Episode> season : series.episodes) {
            for (Episode episode : season) {
                if (filter.allow(episode)) {
                    return episode;
                }
            }
        }
        return null;
    }

    /**
     * Create a unified value that is used for comparisons among episodes
     * @param season Season number of the episode
     * @param episode Episode number
     * @return Unified index that can be compared across all the series
     */
    public static int toUnifiedEpisodeIndex(int season, int episode) {
        return (season * SEASON_MULTIPLER) + episode;
    }

    /**
     * Returns an allow all filter
     * @param <T> Type of object being passed in
     * @return the Allow-All Filter
     */
    @SuppressWarnings("unchecked")
    public static <T> IFilter<T> getAllowAllFilter() {
        return ALLOW_ALL_FILTER;
    }

    public static <K, V> V getOrThrow(Map<K, V> map, K key) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Value for Key [" + key + "] is null");
        }
        return value;
    }
}
