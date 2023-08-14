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

package momo.command;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.google.inject.Inject;

import momo.MomoHome;
import momo.services.MonthlyHoursService;
import picocli.CommandLine.Command;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * TODO
 */
@Command(name = "track", description = "I'm a subcommand of `momo`")
public class MomoTrack implements Callable<Integer>
{
    @Inject
    private MonthlyHoursService service;

    @Inject
    @MomoHome
    private Path home;

    @Override
    public Integer call()
    {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(home, "home");

        try
        {
            if (service.createMonthlyRecordingIfAbsent(YearMonth.now()))
            {
                System.out.printf("New monthly recording for %s created%n", YearMonth.now());
            }
        }
        catch (IOException e)
        {
            System.err.printf("New monthly recording cannot be created: %s%n", e.getMessage());
            return 1;
        }

        try
        {
            var tRecord = LocalDateTime.now().truncatedTo(MINUTES);
            service.writeRecord(tRecord);
            System.out.printf("Record %02d:%02d was added to today", tRecord.getHour(), tRecord.getMinute());
        }
        catch (IOException e)
        {
            System.err.printf("New record cannot be written: %s%n", e.getMessage());
            return 1;
        }

        return 0;
    }
}
