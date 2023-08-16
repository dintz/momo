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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Time Record")
class TimeRecordTest
{
    @Test
    @DisplayName("is open if `stop` is null")
    void isOpen()
    {
        var openRecord = TimeRecord.builder().start(LocalTime.now()).build();
        var closedRecord = TimeRecord.builder().start(LocalTime.now()).stop(LocalTime.now()).build();

        assertThat(openRecord.isOpen(), is(true));
        assertThat(closedRecord.isOpen(), is(false));
    }

    @Test
    @DisplayName("calculates duration correct")
    void getDuration()
    {
        var record = TimeRecord.builder()
                .start(LocalTime.of(10,13))
                .stop(LocalTime.of(11,30))
                .build();

        assertThat(record.getDuration(), is(77));

        record = TimeRecord.builder()
                .start(LocalTime.of(0,0))
                .stop(LocalTime.of(23,59))
                .build();

        assertThat(record.getDuration(), is(1439));

        record = TimeRecord.builder()
                .start(LocalTime.of(11,0))
                .stop(null)
                .build();

        assertThat(record.getDuration(), is(0));
    }
}