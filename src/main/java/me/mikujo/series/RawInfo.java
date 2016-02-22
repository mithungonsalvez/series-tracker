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

import me.mikujo.series.utils.Configs;

import java.util.List;
import java.util.Map;

/**
 * Container to hold all the required information
 * @author mithun.gonsalvez
 */
public class RawInfo {

  private final Map<String, Object> seriesInfo;

  private final Configs layoutConfig;

  private final Map<String, List<String>> tableHints;

  public RawInfo(Map<String, Object> seriesInfo, Configs layoutConfig, Map<String, List<String>> tableHints) {
    this.seriesInfo = seriesInfo;
    this.layoutConfig = layoutConfig;
    this.tableHints = tableHints;
  }

  public Map<String, Object> getSeriesInfo() {
    return seriesInfo;
  }

  public Configs getLayoutConfig() {
    return layoutConfig;
  }

  public Map<String, List<String>> getTableHints() {
    return this.tableHints;
  }

}
