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

package momo.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import momo.model.MomoConfiguration;
import momo.model.IntermediateReport;
import momo.model.MonthlyRecording;

public interface MonthlyHoursService
{
    /**
     * @param timeRecord
     *         the record to add, not null
     *
     * @throws FileNotFoundException
     *         if the monthly recording file for the given month doesn't exist
     */
    void writeRecord(final LocalDateTime timeRecord) throws IOException;

    /**
     * @param month
     *         the month of the recording to create, not null
     *
     * @return <code>true</code> if new recording is created
     */
    boolean createMonthlyRecordingIfAbsent(final YearMonth month) throws IOException;

    /**
     * FIXME
     *
     * @param configuration
     *
     * @return
     *
     * @throws IOException
     */
    IntermediateReport generateIntermediateReport(final MomoConfiguration configuration) throws IOException;

    /**
     * TODO
     *
     * @param configuration
     * @param monthlyRecording
     *
     * @return
     *
     * @throws IOException
     */
    IntermediateReport generateIntermediateReport(final MomoConfiguration configuration,
                                                  final MonthlyRecording monthlyRecording,
                                                  final LocalDateTime today) throws IOException;
}
