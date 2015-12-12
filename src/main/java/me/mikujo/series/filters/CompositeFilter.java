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
 * A Composite filter that encapsulates multiple filters to provide a short-circuited match
 * @author mithun.gonsalvez
 * @param <M>
 */
public class CompositeFilter<M> implements IFilter<M> {

    /** Filters that have to be evaluated */
    private final IFilter<M>[] filters;

    /**
     * Composite filter constructor that accepts multiple filters
     * @param filters
     */
    @SuppressWarnings("unchecked")
    public CompositeFilter(IFilter<M>... filters) {
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("Filters provided is null or empty");
        }
        this.filters = filters;
    }

    @Override
    public boolean allow(M input) {
        for (IFilter<M> filter : this.filters) {
            if (!filter.allow(input)) {
                return false;
            }
        }
        return true;
    }

}
