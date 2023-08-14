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

package momo.model;

import java.time.YearMonth;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRecording
{
    @JsonProperty("month")
    private YearMonth month;

    @Builder.Default
    @JsonDeserialize(as = TreeSet.class)
    @JsonProperty("days")
    private NavigableSet<DailyRecording> days = new TreeSet<>();

    public void add(final DailyRecording dailyRecording)
    {
        days.add(dailyRecording);
    }

    public static MonthlyRecording createFor(final YearMonth month)
    {
        Objects.requireNonNull(month, "month");

        return MonthlyRecording.builder()
                .month(month)
                .build();
    }

    public static MonthlyRecording createForNow()
    {
        return MonthlyRecording.builder()
                .month(YearMonth.now())
                .build();
    }
}
