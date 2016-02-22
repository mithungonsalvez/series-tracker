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

package me.mikujo.series.wiki;

/**
 * List of keys that are used from the list
 * @author mithun.gonsalvez
 */
public interface Keyz {

  /** Hints */
  String HINTS = "hints";

  /** Page */
  String PAGE = "page";

  /** Title */
  String TITLE = "title";

  /** Key to identify which episode has been watched */
  String WATCHED = "watched";

  /** Key 'formats', group tag that contains all the the formats */
  String FORMATS = "formats";

  /** Key 'format', identifies the format to be used to parse the wiki-page */
  String FORMAT = "format";

  /** Key 'series', identifies the list of series to be parsed */
  String SERIES = "series";

  /** Key 'extends', identifies the parent from which we should inherit properties */
  String EXTENDS = "extends";

  /** Key 'type', identifies the type of parser to use */
  String TYPE = "type";

  /** Key 'wiki' indicates that we should be using the wiki parser */
  String TYPE_WIKI = "wiki";

  /** Key 'text', that identifies that the format is text */
  String FORMAT_TEXT = "text";

  /** TOC id */
  String TOC_ID = "toc.id";

  /** Episodes Link */
  String EPISODES_LINK = "episodes.link";

  /** Table Row Class */
  String TABLE_ROW_CLZ = "table.row.class";

  /** Table Column Season */
  String TABLE_COL_TITLE = "table.col.title";

  /** Table Column Air-Date */
  String TABLE_COL_AIRDATE = "table.col.air-date";

  /** Format of the air date column */
  String DATE_FORMAT = "air.date.format";

  /** Key for the setting used to fetch the episode unifier multiplier */
  String SEASON_MULTIPLIER = "unified.episode.season.multiplier";

}
