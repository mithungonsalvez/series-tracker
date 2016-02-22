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

package me.mikujo.series.filters;

import me.mikujo.series.Episode;
import me.mikujo.series.utils.Utils;

/**
 * Series filter, See {@linkplain #SeriesFilter}
 * @author mithun.gonsalvez
 */
public class SeriesFilter implements IFilter<Episode>, Comparable<SeriesFilter> {

  /** Start episode */
  private final int sStart;

  /** End episode */
  private final int sEnd;

  /**
   * Accepts a raw filter of the format [S{s1}E{e1}]-S{s2}E{e2} and builds a filter<br>
   * Here [S{s1}E{e1}] is optional indicating start from Season 0, Episode 0 (Basically anything)<br>
   * S{s2}E{e2} is mandatory indicating the end of the filter indicating Season s2 Episode e2<br>
   * Both parts are considered as inclusive, hence any episode that falls within the range will be omitted.
   * @param rawFilter String that matches the filter syntax
   */
  public SeriesFilter(String rawFilter) {
    String[] parts = rawFilter.split("-");
    parts[0] = parts[0].trim();
    switch (parts.length) {
      case 1:
        this.sStart = this.sEnd = Utils.parseWatched(parts[0].trim());
        break;
      case 2:
        this.sStart = (parts[0].isEmpty()) ? 0 : Utils.parseWatched(parts[0]);
        this.sEnd = Utils.parseWatched(parts[1].trim());
        break;
      default:
        throw new IllegalArgumentException("Illegal value for filter [" + rawFilter + "]");
    }
  }

  @Override
  public boolean allow(Episode episode) {
    int eUIdx = episode.unifiedEpisodeIndex;
    if (eUIdx > this.sEnd) {
      return true;
    } else if (eUIdx == this.sEnd) {
      return false;
    }

    return eUIdx < this.sStart;
  }

  @Override
  public int compareTo(SeriesFilter o) {
    return this.sStart - o.sStart;
  }

}
