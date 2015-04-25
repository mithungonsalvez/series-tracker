/**
 * Utils.java Created 3:23:05 am 2015
 */

package me.mikujo.series.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    /** OMDB Base URL */
    private static final String OMDB_URL = "http://www.omdbapi.com/?r=json&i=";

    /** Pattern to match season and episode */
    private static final Pattern WATCHED_PATTERN = Pattern.compile("S(\\d+)E(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Fetch the URL as specified by the String URL and return the saved copy<br/>
     * If the URL contents have not changed, then fetch it from the cache
     * @param title Title of the URL
     * @param strUrl URL in string form
     * @param dirId Directory name where the cache should be created
     * @return Path containing the URL contents
     * @throws IOException If something goes wrong
     */
    public static Path fetchUrl(String title, String strUrl, String dirId) throws IOException {
        Path cacheDir = Paths.get(".cache", dirId);
        Files.createDirectories(cacheDir);

        Path cacheFile = cacheDir.resolve(title);
        Path cacheFileDate = cacheDir.resolve(title + "_date");
        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (Files.exists(cacheFile) && Files.exists(cacheFileDate)) {
            String dateValue = Files.readAllLines(cacheFileDate).get(0);
            connection.addRequestProperty("If-Modified-Since", dateValue);

            if (connection.getResponseCode() == 304) { // not modified
                // System.out.println("Using cached file");
            } else { // modified since last visit, so cache the new data
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
        Files.write(cacheFileDate, lastModified.getBytes());
    }

    /**
     * Parse the watched input with the appropriate pattern
     * @param watched Watched pattern of the form S{Season-Number}E{Episode-Number}
     * @return an array with two items 0th index as Season-Number and 1st index as the Episode-Number
     */
    public static int[] parseWatched(String watched) {
        Matcher matcher = WATCHED_PATTERN.matcher(watched);
        if (matcher.matches()) {
            return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) };
        } else {
            throw new IllegalArgumentException("Provided input indicating 'watched' [" + watched
                    + "] does not match valid format");
        }
    }

    /**
     * Fetches the IMDB title for the provided IMDB ID from the OMDB-API
     * @param imdbId IMDB Id
     * @return Name for the required IMDB Id
     * @throws IOException If fetching the data fails
     */
    public static Map<Object, Object> readOmdbJSON(String imdbId) throws IOException {
        Path cacheDir = Paths.get(".cache");
        Path cacheFile = cacheDir.resolve(imdbId);
        if (Files.exists(cacheFile)) {
            return readData(cacheFile);
        } else {
            Files.createDirectories(cacheDir);
            URL url = new URL(OMDB_URL + imdbId);
            try (InputStream rdr = url.openConnection().getInputStream()) {
                Files.copy(rdr, cacheFile);
                return cast(readData(cacheFile));
            }
        }
    }

    /**
     * Read the data from the path and return the object
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
     * Cast the object to the required type
     * @param input Input to be cast
     * @return Input that is cast
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object input) {
        return (T) input;
    }
}
