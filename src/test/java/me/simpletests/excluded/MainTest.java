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

package me.simpletests.excluded;

import me.mikujo.series.Main;

import org.junit.Test;

/**
 *
 * @author Mj
 */
public class MainTest {

  /**
   * Test of main method, of class Main.
   * @throws java.lang.Exception
   */
  @Test
  public void testMain() throws Exception {
    System.out.println("main");
//        String[] args = {
//            "J:/Synked/GoogleDrive/Series/SeriesList.json",
//            "J:/Synked/GoogleDrive/Series/Watched.json",
//            "J:/Synked/GoogleDrive/Series/L4.txt",
//            "J:/Coding/Java/code/SeriesTracker/.cache",
//            "text",
//            "false"
//        };

//VERSION="1.2"
//DR_PERSONAL="/media/mc/Personal"
//
//SDIR="${DR_PERSONAL}/Synked/GoogleDrive/Series"
//APP_HOME="${DR_PERSONAL}/Coding/Java/install/SeriesTracker-${VERSION}"
//OUT_FILE=${SDIR}/L4.txt
//
//"${APP_HOME}/bin/SeriesTracker" "${SDIR}/SeriesList.json" "${SDIR}/Watched.json" "${OUT_FILE}" "${APP_HOME}/.cache" text

    String version = "1.2";
    String personalDir = "/media/mc/Personal";
    String sDir = personalDir + "/Synked/GoogleDrive/Series";
    String appHome = personalDir + "/Coding/Java/install/SeriesTracker-" + version;

    String[] args = {
      "-seriesList",
      sDir + "/SeriesList.json",
      "-watchedList",
      sDir + "/Watched.json",
      "-output",
      sDir + "/L4.txt",
      "-cacheDir",
      appHome + "/.cache",
      "-outputformat",
      "text",
    };

    Main.main(args);
  }

}
