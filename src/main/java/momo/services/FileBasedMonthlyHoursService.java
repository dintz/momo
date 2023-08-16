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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.Inject;

import momo.config.MomoHome;
import momo.model.DailyRecording;
import momo.model.IntermediateReport;
import momo.model.MomoConfiguration;
import momo.model.MonthlyRecording;
import momo.util.TimeUtil;

import static java.math.RoundingMode.UP;
import static momo.util.TimeUtil.BUSINESS_DAYS;
import static momo.util.TimeUtil.getWorkdays;
import static momo.util.TimeUtil.getWorkdaysInThisWeek;

/**
 * TODO
 */
public class FileBasedMonthlyHoursService implements MonthlyHoursService
{
    public static final String MOMO_FILE_EXTENSION = ".momo";
    public static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final BigDecimal MINUTES = new BigDecimal(60);

    @Inject
    @MomoHome
    private Path home;

    @Inject
    private JsonMapper mapper;

    @Override
    public void writeRecord(final LocalDateTime timeRecord) throws IOException
    {
        Objects.requireNonNull(timeRecord, "timeRecord");

        final var fileName = YearMonth.from(timeRecord).format(FILE_NAME_FORMATTER).concat(MOMO_FILE_EXTENSION);
        final var filePath = home.resolve(fileName);

        final var monthly = mapper.readValue(filePath.toFile(), MonthlyRecording.class);

        final var day = MonthDay.from(timeRecord);
        final DailyRecording daily;
        if (!monthly.getDays().isEmpty() && Objects.equals(monthly.getDays().last().getDay(), day))
        {
            daily = monthly.getDays().last();
        }
        else
        {
            daily = DailyRecording.createFor(day);
            monthly.add(daily);
        }

        daily.add(timeRecord.toLocalTime());

        mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), monthly);
    }

    @Override
    public boolean createMonthlyRecordingIfAbsent(final YearMonth month) throws IOException
    {
        Objects.requireNonNull(month, "month");

        final var fileName = month.format(FILE_NAME_FORMATTER).concat(MOMO_FILE_EXTENSION);
        final var filePath = home.resolve(fileName);

        if (Files.isRegularFile(filePath))
        {
            return false;
        }
        else
        {
            if (Files.isDirectory(filePath))
            {
                throw new FileNotFoundException("%s (not a regular file)".formatted(filePath.toAbsolutePath()));
            }

            final var monthly = MonthlyRecording.createFor(month);

            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), monthly);

            return true;
        }
    }

    @Override
    public IntermediateReport generateIntermediateReport(final MomoConfiguration configuration,
                                                         final MonthlyRecording monthlyRecording,
                                                         final LocalDate today)
    {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(monthlyRecording, "monthlyRecording");

        final var month = YearMonth.from(today);

        final var beginOfMonth = month.atDay(1);
        final var endOfMonth = month.atEndOfMonth();

        final var allDailyMinutes = monthlyRecording.getDays().stream()
                .collect(Collectors.toMap(DailyRecording::getDay, DailyRecording::getDailyDuration));

        final var monthlyMinutes = (int) allDailyMinutes.values().stream().reduce(Integer::sum).orElse(0);
        final var monthlyWorkdays = getWorkdays(beginOfMonth, endOfMonth).size();
        final var monthlyPlannedHours = new BigDecimal(monthlyWorkdays)
                .multiply(
                        new BigDecimal(configuration.getIrwaz()).divide(new BigDecimal(BUSINESS_DAYS.size()), 2, UP)
                );
        final var monthlyActualHours = monthlyMinutes > 0 ?
                new BigDecimal(monthlyMinutes).divide(MINUTES, 2, UP) : BigDecimal.ZERO;

        final var weekDays = getWorkdaysInThisWeek(today);
        final var weeklyActualMinutes = (int) allDailyMinutes.entrySet().stream()
                .filter(entry -> weekDays.contains(entry.getKey().atYear(today.getYear())))
                .map(Map.Entry::getValue)
                .reduce(Integer::sum)
                .orElse(0);
        final var weeklyActualHours = weeklyActualMinutes > 0 ?
                new BigDecimal(weeklyActualMinutes).divide(MINUTES, 2, UP) : BigDecimal.ZERO;

        final var reportBob = IntermediateReport.builder();

        reportBob.monthlyPlannedHours(monthlyPlannedHours);
        reportBob.monthlyActualHours(monthlyActualHours);
        reportBob.monthlyOvertime(monthlyActualHours.subtract(monthlyPlannedHours));
        reportBob.dailyActualHours(new BigDecimal(allDailyMinutes.get(MonthDay.now())).divide(MINUTES, 2, UP));
        reportBob.weeklyActualHours(weeklyActualHours);
        reportBob.weeklyOvertime(weeklyActualHours.subtract(new BigDecimal(configuration.getIrwaz())));

        return reportBob.build();
    }

    @Override
    public IntermediateReport generateIntermediateReport(final MomoConfiguration configuration) throws IOException
    {
        Objects.requireNonNull(configuration, "configuration");

        final var filePath = home.resolve(YearMonth.now().format(FILE_NAME_FORMATTER).concat(MOMO_FILE_EXTENSION));
        final var monthlyRecording = mapper.readValue(filePath.toFile(), MonthlyRecording.class);

        return generateIntermediateReport(configuration, monthlyRecording, LocalDate.now());
    }
}
