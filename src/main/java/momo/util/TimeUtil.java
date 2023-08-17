/*
 * MIT License
 *
 * Copyright (c) 2023 Daniel Hintz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package momo.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import lombok.NoArgsConstructor;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class TimeUtil
{
    // for the sake of efficiency the business days put into a Set
    // in general, a Set has a better lookup speed than a List
    public static final List<DayOfWeek> BUSINESS_DAYS = List.of(
            MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
    );
    private static final DayOfWeek firstBusinessDay = MONDAY;
    private static final DayOfWeek lastBusinessDay = FRIDAY;

    public static List<LocalDate> getWorkdays(final LocalDate startDateInclusive, final LocalDate endDateInclusive)
    {
        /*
        final Set<LocalDate> holidays = Set.of(
                LocalDate.of(2018, 7, 4)
        );
        */

        // Note: End date itself is NOT included
        return startDateInclusive.datesUntil(endDateInclusive.plusDays(1))

                // Retain all business days. Use static imports from
                // java.time.DayOfWeek.*
                .filter(t -> BUSINESS_DAYS.contains(t.getDayOfWeek()))

                // Retain only dates not present in our holidays list
                // .filter(t -> !holidays.contains(t))

                // Collect them into a List. If you only need to know the number of
                // dates, you can also use .count()
                .toList();
    }

    public static List<LocalDate> getWeekWorkdaysUntil(final LocalDate dateInclusive)
    {
        // Note: End date itself is NOT included
        return dateInclusive.with(previousOrSame(firstBusinessDay))
                .datesUntil(dateInclusive.plusDays(1))
                .toList();
    }
}
