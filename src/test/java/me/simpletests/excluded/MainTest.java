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
        String[] args = {
            "J:/Synked/GoogleDrive/Series/SeriesList.json",
            "J:/Synked/GoogleDrive/Series/Watched.json",
            "J:/Synked/GoogleDrive/L4.txt",
            "J:/Coding/Java/code/SeriesTracker/.cache",
            "text",
            "false"
        };

        Main.main(args);
    }

}
