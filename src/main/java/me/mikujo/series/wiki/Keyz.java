/**
 * Keyz.java Created 3:12:08 am 2015
 */

package me.mikujo.series.wiki;

/**
 * List of keys that are used from the list
 * @author mithun.gonsalvez
 */
public interface Keyz {

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
    String TABLE_TITLE = "table.col.title";

    /** Table Column Air-Date */
    String TABLE_AIRDATE = "table.col.air-date";

    /** Format of the air date column */
    String DATE_FORMAT = "air.date.format";

}
