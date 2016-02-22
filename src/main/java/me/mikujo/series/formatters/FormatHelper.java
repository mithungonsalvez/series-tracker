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

package me.mikujo.series.formatters;

import java.io.IOException;
import java.io.Writer;

/**
 * Format helper class to simplify the formatting needs
 * @author mithun.gonsalvez
 */
public class FormatHelper {

  /**
   * Normalize the value to ensure that it is a two digit number
   * @param value Value to normalize
   * @param output Output writer
   * @throws IOException If there is a problem while writing
   */
  public static void normalize(int value, Writer output) throws IOException {
    if (value < 10) {
      output.append('0').append(Character.forDigit(value, 10));
    } else {
      output.append(String.valueOf(value));
    }
  }

}
