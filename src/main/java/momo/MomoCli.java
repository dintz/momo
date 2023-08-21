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
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import com.google.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import momo.config.GuiceFactory;
import momo.config.MomoHome;
import momo.services.ConfigurationService;
import momo.services.MonthlyHoursService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static picocli.CommandLine.ScopeType.INHERIT;

/**
 * FIXME
 */
@Command(name = "momo",
        mixinStandardHelpOptions = true,
        version = "0.1",
        description = "Records, checks and evaluates daily working hours.",
        subcommands = { MomoTrack.class }
)
@Slf4j
public class MomoCli implements Callable<Integer>
{
    @Mixin
    @Getter
    private LoggingMixin loggingMixin;

    @Option(names = { "-H", "--alt-home" }, scope = INHERIT, paramLabel = "ALT_HOME_DIR",
            description = "the alternative home directory")
    private Path altHome;

    @Inject
    private ConfigurationService cfgService;

    @Inject
    private MonthlyHoursService monthlyService;

    @Inject
    @MomoHome
    private Path home;

    @Override
    public Integer call() throws IOException
    {
        final var config = cfgService.readConfiguration(getHome());
        final var report = monthlyService.generateIntermediateReport(getHome(), config);

        log.info("Time tracking is active and records your working time.");
        log.info("  (use \"momo track\" to stop the tracking)");
        log.info("");

        log.info("Your current daily working time:");
        log.info("");
        log.info("    {} hours",
                report.getDailyActualHours());
        log.info("");
        log.info("Recorded working time for this week:");
        log.info("");
        log.info("    {} hours ({} hours to planned)",
                report.getWeeklyActualHours(), report.getWeeklyOvertime());
        log.info("");
        log.info("Overview of monthly working time:");
        log.info("");
        log.info("    {} of {} hours ({} hours to planned)",
                report.getMonthlyActualHours(), report.getMonthlyPlannedHours(), report.getMonthlyOvertime());

        return 0;
    }

    protected Path getHome()
    {
        Objects.requireNonNull(home, "home");

        if (Objects.isNull(altHome))
        {
            return home;
        }
        else
        {
            return altHome;
        }
    }

    /**
     * TODO
     *
     * @param args
     */
    public static void main(String[] args)
    {
        var exitCode = new CommandLine(MomoCli.class, new GuiceFactory())
                .setExecutionStrategy(LoggingMixin::executionStrategy)
                .execute(args);

        System.exit(exitCode);
    }
}
