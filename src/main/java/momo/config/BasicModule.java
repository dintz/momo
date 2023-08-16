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

package momo.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import momo.services.ConfigurationService;
import momo.services.FileBasedConfigurationService;
import momo.services.FileBasedMonthlyHoursService;
import momo.services.MonthlyHoursService;

public class BasicModule extends AbstractModule
{
    private static final String MOMO_FOLDER_NAME = ".momo";

    @Provides
    public JsonMapper provideMapper()
    {
        final var mapper = new JsonMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Provides
    @MomoHome
    public Path provideMomoHome()
    {
        Path userHome = SystemUtils.getUserHome().toPath();

        try
        {
            // an exception is not thrown if the directory could not be created because it already exists
            return Files.createDirectories(userHome.resolve(MOMO_FOLDER_NAME));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected void configure()
    {
        bind(ConfigurationService.class).to(FileBasedConfigurationService.class);
        bind(MonthlyHoursService.class).to(FileBasedMonthlyHoursService.class);
    }
}