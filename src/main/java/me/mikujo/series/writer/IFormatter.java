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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Formatter interface, to format and write the data in different formats
 * @author mithun.gonsalvez
 */
public interface IFormatter {

  /** Indicates that the date is unknown */
  public static final String DATE_IS_UNKNOWN = "?? ??? ????";

  /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
  public static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

  /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
  public static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("?? MMM yyyy");

  /** Date format in which we are going to write the date in; format is 03 Feb 2015 */
  public static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("?? ??? yyyy");

  /**
   * Add padding
   * @param input Input to add padding
   * @param length Length of expected output
   * @return Padded array
   */
  public default char[] addPadding(String input, int length) {
    char[] fill = new char[length];
    System.arraycopy(input.toCharArray(), 0, fill, 0, input.length());
    Arrays.fill(fill, input.length(), length, ' ');

    return fill;
  }

  /**
   * Post process the list and prepare the final output
   * @param series Series that have to be written
   * @param filter Filter that can be used to filter out the episodes
   * @throws IOException If there is a problem while write the data
   */
  void write(Series series, IFilter<Episode> filter) throws IOException;

}
