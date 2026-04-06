/*
 * This file is part of TimeBar.
 *
 * TimeBar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TimeBar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TimeBar.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.timebar.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormat {

    private static final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###", formatSymbols);

    /**
     * Formats a number to make it pretty. Example: 4322 to 4,322
     *
     * @param number The number to format.
     * @return The formatted number.
     */
    public static String formatInt(int number) {
        String finalNumber = COMMA_FORMAT.format(number);
        finalNumber = finalNumber.replaceAll("[\\x{202f}\\x{00A0}]", " ");
        return finalNumber;
    }
}
