/**
 * Episode.java Created 8:55:10 pm 2015
 */

package me.mikujo.series;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;

/**
 * Episode container
 * @author mithun.gonsalvez
 */
public class Episode implements Comparable<Episode> {

    /** Date the episode was aired */
    public final Temporal date;

    /** Title of the episode */
    public final String title;

    /** Episode number in the season */
    public final int episode;

    /** Season number */
    public final int season;

    /**
     * Episode Constructor
     * @param season Season number
     * @param episode Episode number in the season
     * @param title Title of the episode
     * @param date Date the episode was aired
     */
    public Episode(int season, int episode, String title, Temporal date) {
        this.season = season;
        this.episode = episode;
        this.title = title;
        this.date = date;
    }

    @Override
    public int compareTo(Episode iEpisode) {
        if (this.date != null && iEpisode.date != null) {
            return toLocalDate(this.date).compareTo(toLocalDate(iEpisode.date));
        } else if (this.date == null) {
            return 1;
        } else if (iEpisode.date == null) {
            return -1;
        }

        return 0;
    }

    /**
     * Converts the Temporal instance to a LocalDate by adding basic starting points to it
     * @param temporal temporal to be converted
     * @return LocalDate
     */
    private LocalDate toLocalDate(Temporal temporal) {
        LocalDate iLocalDate;
        if (temporal instanceof LocalDate) {
            iLocalDate = (LocalDate) temporal;
        } else if (temporal instanceof YearMonth) {
            iLocalDate = ((YearMonth) temporal).atDay(1);
        } else if (temporal instanceof Year) {
            iLocalDate = ((Year) temporal).atMonthDay(MonthDay.of(1, 1));
        } else {
            throw new IllegalArgumentException("Unknown type [" + temporal.getClass().getName());
        }
        return iLocalDate;
    }

}
