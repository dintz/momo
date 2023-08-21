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

package momo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import momo.services.MonthlyHoursService;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * TODO
 */
@Command(name = "track", description = "I'm a subcommand of `momo`")
@Slf4j
public class MomoTrack implements Callable<Integer>
{
    @ParentCommand
    private MomoCli parent; // picocli injects reference to parent command

    @Inject
    private MonthlyHoursService service;

    @Override
    public Integer call()
    {
        Objects.requireNonNull(service, "service");

        final var home = parent.getHome();

        try
        {
            if (service.createMonthlyRecordingIfAbsent(home, YearMonth.now()))
            {
                log.debug("New monthly recording for {} created", YearMonth.now());
            }
        }
        catch (IOException e)
        {
            log.error("New monthly recording cannot be created: {}", e.getMessage(), e);
            return 1;
        }

        try
        {
            var tRecord = LocalDateTime.now().truncatedTo(MINUTES);
            final var daily = service.writeRecord(home, tRecord);

            var sActive = daily.isActive() ? "active" : "off";
            log.info("Time %02d:%02d was recorded (time tracking is %s, %d daily periods recognized)".formatted(
                    tRecord.getHour(), tRecord.getMinute(), sActive, daily.getRecords().size()));
            log.info("The today's working time is {} hours.", daily.getDailyHours());
        }
        catch (IOException e)
        {
            log.error("New record cannot be written: {}", e.getMessage(), e);
            return 1;
        }

        return 0;
    }
}
