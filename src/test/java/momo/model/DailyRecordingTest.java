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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * TODO
 */
@DisplayName("")
class DailyRecordingTest
{
    @Test
    @DisplayName("")
    void compareTo()
    {
    }

    @Test
    @DisplayName("")
    void getDailyDuration()
    {
        var recording = DailyRecording.builder().day(MonthDay.now()).build();
        recording.add(LocalTime.of(10, 30));
        recording.add(LocalTime.of(11, 30));
        recording.add(LocalTime.of(12, 30));
        recording.add(LocalTime.of(13, 30));
        recording.add(LocalTime.of(15, 0)); // should be ignored

        assertThat(recording.getDailyDuration(), is(120));
    }

    @Test
    void getEmptyDailyDuration()
    {
        var recording = DailyRecording.builder().day(MonthDay.now()).build();

        assertThat(recording.getDailyDuration(), is(0));
    }
}