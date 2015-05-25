/**
 * TextWriter.java
 * Created 3:12:24 pm 2015
 */
package me.mikujo.series.writer;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Text writer implementation
 * @author mithun.gonsalvez
 */
public class TextFormatter implements IFormatter {

    /** Indicates that the date is unknown */
    private static final String DATE_IS_UNKNOWN = "?? ??? ????";

    /** Output date format: 03 Feb 2015 */
    private static final String OUTPUT_DATE_FORMAT = "dd MMM yyyy";

    /** Used to format the output */
    private static final char[] FILLER_32 = new char[32];

    /** Used to format the output */
    private static final char[] FILLER_08 = new char[8];

    /** Expected date format */
    private static final ThreadLocal<SimpleDateFormat> EXPECTED_FORMAT = new ThreadLocal<SimpleDateFormat>() {

        /** {@inheritDoc} */
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(OUTPUT_DATE_FORMAT);
        };

    };

    static {
        Arrays.fill(FILLER_32, ' ');
        Arrays.fill(FILLER_08, ' ');
    }

    /** Writer instance */
    private final Writer writer;

    /**
     * Constructor
     * @param writer Writer instance
     */
    public TextFormatter(Writer writer) {
        this.writer = writer;
    }

    /** {@inheritDoc} */
    @Override
    public void write(String url, String title, List<List<String[]>> seasons, int[] seasonEpisode, SimpleDateFormat sdf)
            throws IOException {

        ensureSize(seasons, seasonEpisode[0], "Season not found with number ", title);
        List<String[]> episodes = seasons.get(seasonEpisode[0] - 1);
        ensureSize(episodes, seasonEpisode[1], "Episode not found with number ", title);

        // handle edge cases
        if (episodes.size() == seasonEpisode[1]) { // we are at the last episode of the season
            if (seasons.size() > seasonEpisode[0]) { // check if next season is available
                // write from next season, 1st episode
                write(url, title, seasons, seasonEpisode[0], 0, sdf);
            } else {
                // We do not have any more seasons...
                writeSeriesHeader(url, title);
                writeEpisode(seasons.size() + 1, 1);
                writer.append(DATE_IS_UNKNOWN).append('\n');
            }
        } else {
            // start from current season after current episode
            write(url, title, seasons, seasonEpisode[0] - 1, seasonEpisode[1], sdf);
        }
        writer.write('\n');
    }

    /**
     * Write the final output
     * @param url URL
     * @param title Title of the series
     * @param seasons List that has been fetched from the URL
     * @param seasonIdx Season Index (Starting from 0)
     * @param episodeIdx Episode Index (Starting from 0)
     * @param sdf Date formatter to parse the date of the wiki
     * @throws IOException If there is a problem while write the data
     */
    private void write(String url, String title, List<List<String[]>> seasons, int seasonIdx, int episodeIdx,
            SimpleDateFormat sdf) throws IOException {

        try {
            writeSeriesHeader(url, title);
            write(seasons, seasonIdx, episodeIdx, sdf);

            episodeIdx++;
            for (int i = seasonIdx; i < seasons.size(); i++) {
                for (int j = episodeIdx; j < seasons.get(i).size(); j++) {
                    writer.write(FILLER_32);
                    write(seasons, i, j, sdf);
                }
                episodeIdx = 0;
            }
        } catch (Exception ex) {
            System.err.println("Error for Title [" + title + "], URL [" + url + "]");
            throw ex;
        }
    }

    /**
     * Write the series header
     * @param url URL
     * @param title Title of the series
     * @throws IOException If there is a problem while write the data
     */
    private void writeSeriesHeader(String url, String title) throws IOException {
        writer.append(". ").append(url).append('\n');
        writer.append(". ").write(addPadding(title, 30));
    }

    /**
     * Write the final output
     * @param seasons List that has been fetched from the URL
     * @param seasonIdx Season Index (Starting from 0)
     * @param episodeIdx Episode Index (Starting from 0)
     * @param sdf Date formatter to parse the date of the wiki
     * @throws IOException If there is a problem while write the data
     */
    private void write(List<List<String[]>> seasons, int seasonIdx, int episodeIdx, SimpleDateFormat sdf) throws IOException {
        writeEpisode(seasonIdx + 1, episodeIdx + 1);
        String[] episode = seasons.get(seasonIdx).get(episodeIdx);
        String rawDate = episode[episode.length - 1]; // TODO : Fix this hard coding to fetch the last item
        String date = parseDate(rawDate, sdf);
        writer.write(date);
        writer.write('\n');
    }

    /**
     * Write the episode
     * @param season Season number
     * @param episode Episode number
     * @throws IOException If writing fails
     */
    private void writeEpisode(int season, int episode) throws IOException {
        normalize(season, writer.append('S'));
        normalize(episode, writer.append('E'));
        writer.write(FILLER_08);
    }

    /**
     * Parse the date
     * @param rawDate Raw date
     * @param sdf Date formatter to parse the date of the wiki
     * @return Date in our required format
     * @throws IOException If parsing fails
     */
    private String parseDate(String rawDate, SimpleDateFormat sdf) throws IOException {
        if (rawDate.isEmpty() || rawDate.equals("TBA") || rawDate.equals("TBD")) {
            return DATE_IS_UNKNOWN;
        }
        try {
            Date date = sdf.parse(rawDate);
            return EXPECTED_FORMAT.get().format(date);
        } catch (ParseException ex) {
            throw new IOException(ex);
        }
    }

}
