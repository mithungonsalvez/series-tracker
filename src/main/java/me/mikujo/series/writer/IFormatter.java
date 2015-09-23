/**
 * IWriter.java Created 3:05:26 pm 2015
 */

package me.mikujo.series.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import me.mikujo.series.Series;

/**
 * Writer interface, to write data in different formats
 * @author mithun.gonsalvez
 */
public interface IFormatter {

    /**
     * Post process the list and prepare the final output
     * @param series Series that have to be written
     * @throws IOException If there is a problem while write the data
     */
    void write(Series series) throws IOException;

    /**
     * Ensure that the list has the required size
     * @param list List to check
     * @param size Size to check
     * @param errorMessage Error message if the size does not fit
     * @param title Title of the series
     */
    default void ensureSize(List<?> list, int size, String errorMessage, String title) {
        if (list.size() < size) {
            throw new IllegalArgumentException(errorMessage + size + " for series [" + title + "] only [" + list.size()
                    + "] items were found");
        }
    }

    /**
     * Add padding
     * @param input Input to add padding
     * @param length Length of expected output
     * @return Padded array
     */
    default char[] addPadding(String input, int length) {
        char[] fill = new char[length];
        System.arraycopy(input.toCharArray(), 0, fill, 0, input.length());
        Arrays.fill(fill, input.length(), length, ' ');
        return fill;
    }

    /**
     * Normalize the value to ensure that it is a two digit number
     * @param value Value to normalize
     * @param output Output writer
     * @throws IOException If there is a problem while writing
     */
    default void normalize(int value, Writer output) throws IOException {
        if (value < 10) {
            output.append('0').append(Character.forDigit(value, 10));
        } else {
            output.append(String.valueOf(value));
        }
    }

}
