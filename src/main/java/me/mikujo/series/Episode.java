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

package me.mikujo.series;

import me.mikujo.series.utils.Utils;

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

  /** Unified episode id */
  public final int unifiedEpisodeIndex;

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
    this.unifiedEpisodeIndex = Utils.toUnifiedEpisodeIndex(season, episode);
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
      iLocalDate = ((YearMonth) temporal).atEndOfMonth();
    } else if (temporal instanceof Year) {
      iLocalDate = ((Year) temporal).atMonthDay(MonthDay.of(12, 31)).plusDays(1);
    } else {
      throw new IllegalArgumentException("Unknown type [" + temporal.getClass().getName());
    }
    return iLocalDate;
  }

}
