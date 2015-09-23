/**
 * TextWriter.java Created 3:12:24 pm 2015
 */
package me.mikujo.series.writer;

import java.io.IOException;
import java.io.Writer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

import me.mikujo.series.Episode;
import me.mikujo.series.Series;

/**
 * Text writer implementation
 * @author mithun.gonsalvez
 */
public class TextFormatter implements IFormatter {

    /** Indicates that the date is unknown */
    private static final String DATE_IS_UNKNOWN = "?? ??? ????";

    /** Used to format the output */
    private static final char[] FILLER_32 = new char[32];

    /** Used to format the output */
    private static final char[] FILLER_08 = new char[8];

    /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
    private static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("?? MMM yyyy");

    /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("?? ??? yyyy");

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
    public void write(Series series) throws IOException {
        ensureSize(series.episodes, series.watched[0], "Season not found with number ", series.title);
        List<Episode> episodes = series.episodes.get(series.watched[0] - 1);
        ensureSize(episodes, series.watched[1], "Episode not found with number ", series.title);

        // handle edge cases
        if (episodes.size() == series.watched[1]) { // we are at the last episode of the season
            if (series.episodes.size() > series.watched[0]) { // check if next season is available
                // write from next season, 1st episode
                write(series.url, series.title, series.episodes, series.watched[0], 0);
            } else {
                // We do not have any more series.episodes...
                writeSeriesHeader(series.url, series.title);
                writeEpisode(series.episodes.size() + 1, 1);
                writer.append(DATE_IS_UNKNOWN).append('\n');
            }
        } else {
            // start from current season after current episode
            write(series.url, series.title, series.episodes, series.watched[0] - 1, series.watched[1]);
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
     * @throws IOException If there is a problem while write the data
     */
    private void write(String url, String title, List<List<Episode>> seasons, int seasonIdx, int episodeIdx)
            throws IOException {

        try {
            writeSeriesHeader(url, title);
            write(seasons, seasonIdx, episodeIdx);

            episodeIdx++;
            for (int i = seasonIdx; i < seasons.size(); i++) {
                for (int j = episodeIdx; j < seasons.get(i).size(); j++) {
                    writer.write(FILLER_32);
                    write(seasons, i, j);
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
     * @throws IOException If there is a problem while write the data
     */
    private void write(List<List<Episode>> seasons, int seasonIdx, int episodeIdx) throws IOException {
        writeEpisode(seasonIdx + 1, episodeIdx + 1);
        Episode episode = seasons.get(seasonIdx).get(episodeIdx);
        String date = checkGetDate(episode.date);
        writer.write(date);
        writer.write('\n');
    }

    /**
     * Check and return the date in string form that can be written
     * @param date Instant in time
     * @return String form of the instant, if the input is null, then we will return {@link #DATE_IS_UNKNOWN}
     */
    private String checkGetDate(Temporal date) {
        try {
            if (date == null) {
                return DATE_IS_UNKNOWN;
            } else if (date instanceof LocalDate) {
                return LOCAL_DATE_FORMAT.format(date);
            } else if (date instanceof YearMonth) {
                return YEAR_MONTH_FORMAT.format(date);
            } else if (date instanceof Year) {
                return YEAR_FORMAT.format(date);
            } else {
                throw new IllegalArgumentException("Unknown type [" + date.getClass().getName());
            }
        } catch (DateTimeException ex) {
            return DATE_IS_UNKNOWN;
        }
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

}
