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

package me.mikujo.series.writer;

import me.mikujo.series.Episode;
import me.mikujo.series.Series;
import me.mikujo.series.filters.IFilter;
import me.mikujo.series.formatters.FormatHelper;

import java.io.IOException;
import java.io.Writer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

/**
 * Text writer implementation
 * @author mithun.gonsalvez
 */
public class TextFormatter implements IFormatter {

  /** Used to format the output; An array of characters containing 32 spaces */
  private static final char[] FILLER_32 = new char[32];

  /** Used to format the output; An array of characters containing 8 spaces */
  private static final char[] FILLER_08 = new char[8];

  /** Writer instance */
  private final Writer writer;

  static {
    Arrays.fill(FILLER_32, ' ');
    Arrays.fill(FILLER_08, ' ');
  }

  /**
   * Constructor
   * @param writer Writer instance
   */
  public TextFormatter(Writer writer) {
    this.writer = writer;
  }

  /** {@inheritDoc} */
  @Override
  public void write(Series series, IFilter<Episode> episodeFilter) throws IOException {
    try {
      // write the series header
      writeSeriesHeader(series.url, series.title);

      boolean episodesWritten = false;
      for (List<Episode> season : series.episodes) {
        for (Episode episode : season) {
          if (episodeFilter.allow(episode)) {
            if (episodesWritten) {
              writer.write(FILLER_32);
            }
            episodesWritten = true;
            writeEpisode(episode);
          }
        }
      }

      if (!episodesWritten) {
        // We did not write any episodes, i.e, we not have any more episodes in this season...
        writeEpisode(series.episodes.size() + 1, 1);
        writer.append(DATE_IS_UNKNOWN).append('\n');
      }

      writer.write('\n');

    } catch (Exception ex) {
      System.err.println("Error for Title [" + series.title + "], URL [" + series.url + "]");
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
   * @param episode Episode to be written
   * @throws IOException If there is a problem while write the data
   */
  private void writeEpisode(Episode episode) throws IOException {
    writeEpisode(episode.season, episode.episode);
    String date = checkGetDate(episode.date);
    writer.write(date);
    writer.write('\n');
  }

  /**
   * Check and return the date in string form that can be written
   * @param date Instant in time
   * @return String form of the instant, if the input is null, then we will return {@link FormatHelper#DATE_IS_UNKNOWN}
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
    FormatHelper.normalize(season, writer.append('S'));
    FormatHelper.normalize(episode, writer.append('E'));
    writer.write(FILLER_08);
  }

}
