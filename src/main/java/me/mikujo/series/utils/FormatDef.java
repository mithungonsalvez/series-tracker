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

import java.util.Map;

/**
 * A simple hierarchical data-structure to hold format related information
 * @author mithun.gonsalvez
 */
public class FormatDef {

    /** Map containing the actual data */
    private final Map<String, Object> entries;

    /** Identifier */
    private final String id;

    /** Reference to the parent (nullable) */
    private FormatDef parent;

    /**
     * Constructor
     * @param id Id of the format definition
     * @param entries Map of properties that contains data for this definition
     */
    public FormatDef(String id, Map<String, Object> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("'null' entries are not supported for key [" + id + "]");
        }
        this.id = id;
        this.entries = entries;
    }

    /**
     * Returns the value defined by the key
     * @param key Key used to find the value
     * @return Value for the specified key
     * @throws IllegalArgumentException If the value is not found
     */
    public Object get(String key) throws IllegalArgumentException {
        Object value = getOptional(key);
        if (value == null) {
            throw new IllegalArgumentException("No value found for key [" + key + "], in definition [" + this.id + "]");
        }
        return value;
    }

    /**
     * Returns the value defined by the key or null if the value is not found
     * @param key Key used to find the value
     * @return Value for the specified key or null if the value is not found
     */
    public Object getOptional(String key) {
        Object value = this.entries.get(key);
        if (value == null && parent != null) {
            value = parent.getOptional(key);
        }
        return value;
    }

    /**
     * Sets the parent format definition
     * @param parent Parent format definition
     */
    public void setParent(FormatDef parent) {
        if (this.parent != null) {
            throw new IllegalArgumentException("Attempt to override parent of id [" + this.parent.id + "] in ["
                    + this.id + "] with [" + parent.id + "]");
        }
        this.parent = parent;
    }

}
