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

import me.mikujo.series.utils.Tuple2;

import java.util.Comparator;

/**
 * Series comparator to compare the tuple2 that contains series and the latest episode which may be null
 * @author mithun.gonsalvez
 */
public class SeriesComparator implements Comparator<Tuple2<Series, Episode>> {

  @Override
  public int compare(Tuple2<Series, Episode> o1, Tuple2<Series, Episode> o2) {
    if (o1 == null || o2 == null) {
      return -1;
    }
    Episode tEpisode = o1.t2;
    Episode iEpisode = o2.t2;
    if (tEpisode == null && iEpisode == null) {
      return 0;
    } else if (tEpisode == null) {
      return 1;
    } else if (iEpisode == null) {
      return -1;
    }

    int result = tEpisode.compareTo(iEpisode);
    if (result == 0) {
      return o1.t1.title.compareToIgnoreCase(o2.t1.title);
    } else {
      return result;
    }
  }

}
