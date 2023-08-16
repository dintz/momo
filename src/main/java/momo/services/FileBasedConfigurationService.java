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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.Inject;

import momo.config.MomoHome;
import momo.model.MomoConfiguration;

public class FileBasedConfigurationService implements ConfigurationService
{
    public static final String CONFIG_FILE_NAME = "momo.json";

    @Inject
    @MomoHome
    private Path home;

    @Inject
    private JsonMapper mapper;

    @Override
    public MomoConfiguration readConfiguration() throws IOException
    {
        final var filePath = home.resolve(CONFIG_FILE_NAME);

        if (Files.exists(filePath))
        {
            return mapper.readValue(filePath.toFile(), MomoConfiguration.class);
        }
        else
        {
            final var defaultCfg = MomoConfiguration.builder().build();
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), defaultCfg);
            return defaultCfg;
        }
    }
}
