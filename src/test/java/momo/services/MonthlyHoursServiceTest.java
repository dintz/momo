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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

import momo.GuiceExtension;
import momo.model.DailyRecording;
import momo.model.MomoConfiguration;
import momo.model.MonthlyRecording;

import static java.time.temporal.ChronoUnit.MINUTES;
import static momo.TestModule.FAKE_HOME;
import static org.apache.commons.io.FileUtils.contentEquals;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FIXME
 */
@ExtendWith(GuiceExtension.class)
@DisplayName("Monthly-Hours-Service")
class MonthlyHoursServiceTest
{
    @Inject
    MonthlyHoursService service;

    @Inject
    Path fakeHome;

    @Nested
    @DisplayName("create monthly recording file")
    class CreateMonthlyRecordingIfAbsent
    {
        @Test
        @DisplayName("not if exists")
        void doNothingIfExist() throws IOException
        {
            assertThat(service.createMonthlyRecordingIfAbsent(fakeHome, YearMonth.of(2023, 8)), is(false));
        }

        @Test
        @DisplayName("if absent with correct content")
        void createIfAbsent() throws IOException
        {
            var expectedPath = FAKE_HOME.resolve("2023-07.momo");

            assertThat(service.createMonthlyRecordingIfAbsent(fakeHome, YearMonth.of(2023, 7)), is(true));
            assertTrue(contentEquals(expectedPath.toFile(), FAKE_HOME.resolve("2023-07.expected").toFile()));

            assertDoesNotThrow(() -> Files.delete(expectedPath));
        }

        @Test
        @DisplayName("if no file with correct extension exists")
        void createIfWrongExtension() throws IOException
        {
            assertThat(service.createMonthlyRecordingIfAbsent(fakeHome, YearMonth.of(2023, 5)), is(true));

            // TODO needs prepared folder that could be deleted
            assertDoesNotThrow(() -> Files.delete(FAKE_HOME.resolve("2023-05.momo")));
        }

        @Test
        @DisplayName("not if target file is already a directory")
        void errorIfFolderWithSameName()
        {
            // is folder not file
            FileNotFoundException ex = assertThrows(
                    FileNotFoundException.class,
                    () -> service.createMonthlyRecordingIfAbsent(fakeHome, YearMonth.of(2023, 6)),
                    "Expected createMonthlyRecordingIfAbsent() to throw, but it didn't"
            );

            assertThat(ex.getMessage(), endsWith("(not a regular file)"));
        }
    }

    @Nested
    @DisplayName("write record")
    class WriteRecord
    {
        @Test
        @DisplayName("with correct content")
        void addThreeRecords() throws IOException
        {
            var expectedPath = FAKE_HOME.resolve("2023-01.momo");

            assertThat(service.createMonthlyRecordingIfAbsent(fakeHome, YearMonth.of(2023, 1)), is(true));
            assertDoesNotThrow(
                    () -> service.writeRecord(fakeHome, LocalDateTime.of(2023, 1, 15, 10, 30).truncatedTo(MINUTES)));
            assertDoesNotThrow(
                    () -> service.writeRecord(fakeHome, LocalDateTime.of(2023, 1, 15, 11, 0).truncatedTo(MINUTES)));
            assertDoesNotThrow(
                    () -> service.writeRecord(fakeHome, LocalDateTime.of(2023, 1, 15, 15, 59).truncatedTo(MINUTES)));

            assertTrue(contentEquals(expectedPath.toFile(), FAKE_HOME.resolve("2023-01.expected").toFile()));
            assertDoesNotThrow(() -> Files.delete(expectedPath));

        }
    }

    @Test
    @DisplayName("generate report")
    void generateIntermediateReport() throws IOException
    {
        var cfg = MomoConfiguration.builder()
                .irwaz(32)
                .build();

        var monthly = MonthlyRecording.builder()
                .month(YearMonth.of(2023, 8))
                .build();

        var daily8 = DailyRecording.builder() // 368 min
                .day(MonthDay.of(8, 8))
                .build();
        daily8.add(LocalTime.of(8, 30));
        daily8.add(LocalTime.of(14, 38));
        var daily14 = DailyRecording.builder() // 180 min / TUESDAY
                .day(MonthDay.of(8, 14))
                .build();
        daily14.add(LocalTime.of(9, 0));
        daily14.add(LocalTime.of(12, 0));
        var daily16 = DailyRecording.builder() // 207 + 15 min / WEDNESDAY
                .day(MonthDay.of(8, 16))
                .build();
        daily16.add(LocalTime.of(8, 25));
        daily16.add(LocalTime.of(9, 52));
        daily16.add(LocalTime.of(12, 0));
        daily16.add(LocalTime.of(14, 0));
        daily16.add(LocalTime.of(16, 45));

        monthly.add(daily8);
        monthly.add(daily14);
        monthly.add(daily16);

        var report = service.generateIntermediateReport(fakeHome, cfg, monthly, LocalDateTime.of(2023, 8, 16, 17, 0));

        assertThat(report, is(notNullValue()));
        assertThat("dailyActualHours", report.getDailyActualHours(), is(new BigDecimal("3.70")));
        assertThat("weeklyActualHours", report.getWeeklyActualHours(), is(new BigDecimal("6.70")));
        assertThat("weeklyOvertime", report.getWeeklyOvertime(), is(new BigDecimal("-25.30")));
        assertThat("monthlyPlannedHours", report.getMonthlyPlannedHours(), is(new BigDecimal("147.20")));
        assertThat("monthlyActualHours", report.getMonthlyActualHours(), is(new BigDecimal("12.84")));
        assertThat("monthlyOvertime", report.getMonthlyOvertime(), is(new BigDecimal("-134.36")));
    }
}