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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main class that starts everything
 * @author mithun.gonsalvez
 */
public class Main {

  /**
   * Main method
   * @param args Arguments
   * @throws Exception If something goes wrong
   */
  public static void main(String[] args) throws Exception {
    // TODO : use a proper command line argument parser
    Path input = Paths.get(args[0]);
    Path userConfig = Paths.get(args[1]);
    Path output = Paths.get(args[2]);
    Path cacheDir = Paths.get(args[3]);
    String outputFormat = args[4];
    boolean offline = false;
    if (args.length > 5) {
      offline = Boolean.parseBoolean(args[5]);
    }

    SeriesTracker tracker = new SeriesTracker(input, userConfig, output, cacheDir, outputFormat, offline);
    tracker.process();
  }

}
