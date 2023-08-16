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

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.LinkedList;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class DailyRecording implements Comparable<DailyRecording>
{
    @JsonProperty("day")
    private MonthDay day;

    @Builder.Default
    @JsonDeserialize(as = LinkedList.class)
    @JsonProperty("records")
    private LinkedList<TimeRecord> records = new LinkedList<>();

    @Override
    public int compareTo(final DailyRecording other)
    {
        return this.day.compareTo(other.day);
    }

    public void add(final LocalTime timeRecord)
    {
        if (!records.isEmpty() && records.getLast().isOpen())
        {
            records.getLast().setStop(timeRecord);
        }
        else
        {
            records.add(TimeRecord.builder().start(timeRecord).build());
        }
    }

    /**
     * @return the total daily duration in minutes
     */
    @JsonIgnore
    public int getDailyDuration()
    {
        return records.stream()
                .map(TimeRecord::getDuration)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public static DailyRecording createFor(final MonthDay day)
    {
        Objects.requireNonNull(day, "day");

        return DailyRecording.builder()
                .day(day)
                .build();
    }
}
