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

package me.mikujo.series.utils;

/**
 * A Tuple with two items
 * @author mithun.gonsalvez
 * @param <T1> Type 1 object
 * @param <T2> Type 2 object
 */
public class Tuple2<T1, T2> {

  /** Value 1 */
  public final T1 t1;

  /** Value 2 */
  public final T2 t2;

  public Tuple2(T1 t1, T2 t2) {
    this.t1 = t1;
    this.t2 = t2;
  }

}
