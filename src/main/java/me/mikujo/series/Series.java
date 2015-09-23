/**
 * Series.java Created 2:12:36 am 2015
 */
package me.mikujo.series;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Series Object that contains all the information that has been parsed
 * @author mithun.gonsalvez
 */
public class Series implements Comparable<Series> {

    /** URL of the Series */
    public final String url;

    /** Title of the Series */
    public final String title;

    /** Episodes parsed */
    public final List<List<Episode>> episodes;

    /** Watched season and Episode */
    public final int[] watched;

    /**
     * @param url URL
     * @param title Title String
     * @param episodes Episodes parsed
     * @param watched Watched Season and Episode
     */
    public Series(String url, String title, List<List<Episode>> episodes, int[] watched) {
        this.url = url;
        this.title = title;
        this.episodes = episodes;
        this.watched = watched;
    }

    /**
     * Converts the Series object into a Map
     * @return Map that can be directly serialized
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("title", title);
        map.put("episodes", episodes);
        map.put("watched", watched);

        return map;
    }

    /**
     * Converts from a Map to a Series object
     * @param input JSON data to be converted
     * @return Series object
     */
    public static Series fromMap(Map<String, Object> input) {
        String url = (String) input.get("url");
        String title = (String) input.get("title");
        @SuppressWarnings("unchecked")
        List<List<Episode>> episodes = (List<List<Episode>>) input.get("episodes");
        int[] watched = (int[]) input.get("watched");

        return new Series(url, title, episodes, watched);
    }

    @Override
    public int compareTo(Series series) {
        // return getEpisodeToCompare(this).compareTo(getEpisodeToCompare(series));
        Episode tEpisode = getEpisodeToCompare(this);
        Episode iEpisode = getEpisodeToCompare(series);

        if (tEpisode == null && iEpisode == null) {
            return 0;
        } else if (tEpisode == null) {
            return 1;
        } else if (iEpisode == null) {
            return -1;
        }

        return tEpisode.compareTo(iEpisode);
    }

    /**
     * Fetches the first episode for the provided series that the user has not watched
     * @param series Series to get the episode from
     * @return Episode or null, if the episode has not been released yet
     */
    private static Episode getEpisodeToCompare(Series series) {
        ensureSize(series.episodes, series.watched[0], "Season not found with number ", series.title);
        List<Episode> episodes = series.episodes.get(series.watched[0] - 1);
        ensureSize(episodes, series.watched[1], "Episode not found with number ", series.title);

        // handle edge cases
        Episode episode;
        if (episodes.size() == series.watched[1]) { // we are at the last episode of the season
            if (series.episodes.size() > series.watched[0]) { // check if next season is available
                episode = series.episodes.get(series.watched[0]).get(0);
            } else {
                episode = null;
            }
        } else {
            // start from current season after current episode
            episode = series.episodes.get(series.watched[0] - 1).get(series.watched[1]);
        }

        return episode;
    }

    /**
     * Ensure that the list has the required size
     * @param list List to check
     * @param size Size to check
     * @param errorMessage Error message if the size does not fit
     * @param title Title of the series
     */
    private static void ensureSize(List<?> list, int size, String errorMessage, String title) {
        if (list.size() < size) {
            throw new IllegalArgumentException(errorMessage + size + " for series [" + title + "] only [" + list.size()
                    + "] items were found");
        }
    }

}
