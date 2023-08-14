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
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

import momo.GuiceExtension;

import static java.time.temporal.ChronoUnit.MINUTES;
import static momo.TestModule.FAKE_HOME;
import static org.apache.commons.io.FileUtils.contentEquals;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
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

    @Nested
    @DisplayName("create monthly recording file")
    class CreateMonthlyRecordingIfAbsent
    {
        @Test
        @DisplayName("not if exists")
        void doNothingIfExist() throws IOException
        {
            assertThat(service.createMonthlyRecordingIfAbsent(YearMonth.of(2023, 8)), is(false));
        }

        @Test
        @DisplayName("if absent with correct content")
        void createIfAbsent() throws IOException
        {
            final var expectedPath = FAKE_HOME.resolve("2023-07.momo");

            assertThat(service.createMonthlyRecordingIfAbsent(YearMonth.of(2023, 7)), is(true));
            assertTrue(contentEquals(expectedPath.toFile(), FAKE_HOME.resolve("2023-07.expected").toFile()));

            assertDoesNotThrow(() -> Files.delete(expectedPath));
        }

        @Test
        @DisplayName("if no file with correct extension exists")
        void createIfWrongExtension() throws IOException
        {
            assertThat(service.createMonthlyRecordingIfAbsent(YearMonth.of(2023, 5)), is(true));

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
                    () -> service.createMonthlyRecordingIfAbsent(YearMonth.of(2023, 6)),
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
            final var expectedPath = FAKE_HOME.resolve("2023-01.momo");

            assertThat(service.createMonthlyRecordingIfAbsent(YearMonth.of(2023, 1)), is(true));
            assertDoesNotThrow(() -> service.writeRecord(LocalDateTime.of(2023, 1, 15, 10, 30).truncatedTo(MINUTES)));
            assertDoesNotThrow(() -> service.writeRecord(LocalDateTime.of(2023, 1, 15, 11, 0).truncatedTo(MINUTES)));
            assertDoesNotThrow(() -> service.writeRecord(LocalDateTime.of(2023, 1, 15, 15, 59).truncatedTo(MINUTES)));
            assertTrue(contentEquals(expectedPath.toFile(), FAKE_HOME.resolve("2023-01.expected").toFile()));

            assertDoesNotThrow(() -> Files.delete(expectedPath));
        }
    }
}