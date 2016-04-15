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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

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
    // TODO : use a proper command line argument parser someday
    Path seriesList = null;
    Path watchedList = null;
    Path output = null;
    Path cacheDir = null;
    String outputFormat = null;
    boolean offline = false;
    boolean purgecache = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i].toLowerCase();
      if (arg.equals("-serieslist")) {
        seriesList = Paths.get(fetch("-seriesList", ++i, args));

      } else if (arg.equals("-watchedlist")) {
        watchedList = Paths.get(fetch("-watchedList", ++i, args));

      } else if (arg.equals("-output")) {
        output = Paths.get(fetch("-output", ++i, args));

      } else if (arg.equals("-cachedir")) {
        cacheDir = Paths.get(fetch("-cacheDir", ++i, args));

      } else if (arg.equals("-outputformat")) {
        outputFormat = fetch("-outputFormat", ++i, args);

      } else if (arg.equals("-offline")) {
        offline = true;

      } else if (arg.equals("-purgecache")) {
        purgecache = true;

      } else {
        throw new IllegalArgumentException("Unknown argument [" + args[i] + "], all arguments: [" + Arrays.toString(args) + "]");
      }
    }

    // in some cases, we will have to purge the cache, as it may have gotten stale
    if (purgecache) {
      System.out.println("Purging cache directory: " + cacheDir);
      Files.walkFileTree(cacheDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    SeriesTracker tracker = new SeriesTracker(seriesList, watchedList, output, cacheDir, outputFormat, offline);
    tracker.process();
  }

  private static String fetch(String key, int nextPos, String[] args) {
    if (nextPos < args.length) {
      return args[nextPos];
    }
    throw new IllegalArgumentException("Missing value for [" + key + "]");
  }

}
