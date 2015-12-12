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

package me.mikujo.series.filters;

/**
 * A Filter to abstract out the details of an actual filter
 * @author mithun.gonsalvez
 * @param <T> Type of value to be checked across a filter
 */
public interface IFilter<T> {

    /**
     * Check if a the provided input is allowed as per the filter and returns true, if allowed, else false
     * @param input Input to be checked
     * @return true, if the input is allowed as per the filter configuration
     */
    boolean allow(T input);

}
