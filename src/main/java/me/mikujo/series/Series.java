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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Series Object that contains all the information that has been parsed
 * @author mithun.gonsalvez
 */
public class Series {

    /** URL of the Series */
    public final String url;

    /** Title of the Series */
    public final String title;

    /** Episodes parsed */
    public final List<List<Episode>> episodes;

    /**
     * @param url URL
     * @param title Title String
     * @param episodes Episodes parsed
     */
    public Series(String url, String title, List<List<Episode>> episodes) {
        this.url = url;
        this.title = title;
        this.episodes = episodes;
    }

    /**
     * Converts the Series object into a Map
     * @return Map that can be directly serialized
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("title", title);
        map.put("episodes", episodes);

        return map;
    }

    /**
     * Converts from a Map to a Series object
     * @param input JSON data to be converted
     * @return Series object
     */
    public static Series fromMap(Map<String, Object> input) {
        String url = (String) input.get("url");
        String title = (String) input.get("title");
        @SuppressWarnings("unchecked")
        List<List<Episode>> episodes = (List<List<Episode>>) input.get("episodes");

        return new Series(url, title, episodes);
    }

}
