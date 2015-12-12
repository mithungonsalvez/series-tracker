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

package me.simpletests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.junit.Test;

/**
 * @author mithun.gonsalvez
 */
public class JsNewFormatTests {

    /**
     * Basic test
     */
    @Test
    public void test() {
        Map<String, Object> tocVevEp25 = new HashMap<>();
        tocVevEp25.put("type", "wiki");
        tocVevEp25.put("toc.id", "toc");
        tocVevEp25.put("episodes.link", "#Episodes");
        tocVevEp25.put("table.row.class", "vevent");
        tocVevEp25.put("table.col.title", 2);
        tocVevEp25.put("table.col.air-date", 5);

        Map<String, Object> tocVevEp36 = new HashMap<>();
        tocVevEp36.put("extends", "wiki:toc-episodes-vevent-2:5");
        tocVevEp36.put("table.col.title", 3);
        tocVevEp36.put("table.col.air-date", 6);

        Map<String, Object> tocVevEp37 = new HashMap<>();
        tocVevEp37.put("extends", "wiki:toc-episodes-vevent-3:6");
        tocVevEp37.put("table.col.air-date", 7);

        Map<String, Object> formats = new HashMap<>();
        formats.put("wiki:toc-episodes-vevent-2:5", tocVevEp25);
        formats.put("wiki:toc-episodes-vevent-3:6", tocVevEp36);
        formats.put("wiki:toc-episodes-vevent-3:7", tocVevEp37);

        Map<String, Object> theWhispers = new HashMap<>();
        theWhispers.put("page", "The_Whispers_(TV_series)");
        theWhispers.put("imdb.id", "tt3487410");
        theWhispers.put("watched", "S01E02");
        theWhispers.put("format", "wiki:toc-episodes-vevent-2:5");

        Map<String, Object> mrRobot = new HashMap<>();
        mrRobot.put("page", "Mr._Robot_(TV_series)");
        mrRobot.put("imdb.id", "tt4686038");
        mrRobot.put("watched", "S01E01");
        mrRobot.put("format", "wiki:toc-episodes-vevent-2:5");

        List<Object> series = new ArrayList<>();
        series.add(theWhispers);
        series.add(mrRobot);

        Map<String, Object> data = new HashMap<>();
        data.put("formats", formats);
        data.put("series", series);

        System.out.println(JSONObject.toJSONString(data));
    }

}
