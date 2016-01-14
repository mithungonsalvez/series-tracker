# Series Tracker

Read information regarding T.V series from the web and helps you extract bits and pieces.

Inputs
======

Series List Input File
----------------------
	{ "formats":{
	  "wiki:toc-episodes-vevent-2:5":{ "table.col.title":2, "episodes.link":"#Episodes", "table.row.class":"vevent", "type":"wiki", "toc.id":"toc", "table.col.air-date":5 },
	  "wiki:toc-episodes-vevent-3:6":{ "extends":"wiki:toc-episodes-vevent-2:5", "table.col.title":3, "table.col.air-date":6},
	  "wiki:toc-episodes-vevent-3:7":{ "extends":"wiki:toc-episodes-vevent-3:6", "table.col.air-date":7},
	},
	  "series":[
	{ "title":"Agents of S.H.I.E.L.D", "page":"List_of_Agents_of_S.H.I.E.L.D._episodes",     "format":"wiki:toc-episodes-vevent-3:6" },
	{ "title":"Arrow",                 "page":"List_of_Arrow_episodes",                      "format":"wiki:toc-episodes-vevent-3:6" },
	{ "title":"Limitless",             "page":"Limitless_(TV_series)",                       "format":"wiki:toc-episodes-vevent-2:5" },
	{ "title":"Mr. Robot",             "page":"Mr._Robot_(TV_series)",                       "format":"wiki:toc-episodes-vevent-2:5" },
	{ "title":"Person of Interest",    "page":"List_of_Person_of_Interest_episodes",         "format":"wiki:toc-episodes-vevent-3:6" },
	{ "title":"Suits",                 "page":"List_of_Suits_episodes",                      "format":"wiki:toc-episodes-vevent-3:6" },
	{ "title":"The Flash",             "page":"List_of_The_Flash_(2014_TV_series)_episodes", "format":"wiki:toc-episodes-vevent-3:6" },
	],
	  "completed-series":[
	{ "title":"Falling Skies",         "page":"List_of_Falling_Skies_episodes",              "format":"wiki:toc-episodes-vevent-3:6" },
	]}


Watched List
------------
	{ "watched":[
	{ "title":"Agents of S.H.I.E.L.D", "watched":"-S03E10" },
	{ "title":"Arrow",                 "watched":"-S04E09" },
	{ "title":"Limitless",             "watched":"-S01E12" },
	{ "title":"Mr. Robot",             "watched":"-S01E10" },
	{ "title":"Person of Interest",    "watched":"-S04E22" },
	{ "title":"Suits",                 "watched":"-S05E10" },
	{ "title":"The Flash",             "watched":"-S02E09" },
	]}


Output
======
	. https://en.wikipedia.org/wiki/Limitless_(TV_series)
	. Limitless                     S01E13        19 Jan 2016

	. https://en.wikipedia.org/wiki/List_of_The_Flash_(2014_TV_series)_episodes
	. The Flash                     S02E10        19 Jan 2016
	                                S02E11        26 Jan 2016
	                                S02E12        ?? ??? ????

	. https://en.wikipedia.org/wiki/List_of_Arrow_episodes
	. Arrow                         S04E10        20 Jan 2016
	                                S04E11        27 Jan 2016
	                                S04E12        ?? ??? ????
	                                S04E13        ?? ??? ????
	                                S04E14        ?? ??? ????

	. https://en.wikipedia.org/wiki/List_of_Suits_episodes
	. Suits                         S05E11        27 Jan 2016

	. https://en.wikipedia.org/wiki/List_of_Person_of_Interest_episodes
	. Person of Interest            S05E01        ?? ??? ????
	                                S05E02        ?? ??? ????
	                                S05E03        ?? ??? ????
	                                S05E04        ?? ??? ????
	                                S05E05        ?? ??? ????

	. https://en.wikipedia.org/wiki/List_of_Agents_of_S.H.I.E.L.D._episodes
	. Agents of S.H.I.E.L.D         S04E01        ?? ??? ????

	. https://en.wikipedia.org/wiki/Mr._Robot_(TV_series)
	. Mr. Robot                     S02E01        ?? ??? ????
