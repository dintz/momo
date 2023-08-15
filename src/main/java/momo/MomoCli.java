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

import java.util.concurrent.Callable;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import momo.command.MomoTrack;
import momo.config.GuiceFactory;
import momo.config.LoggingMixin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

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

    @Override
    public Integer call()
    {
        log.trace("Starting... (trace) from app");
        log.debug("Starting... (debug) from app");
        log.info("Starting... (info)  from app");
        log.warn("Starting... (warn)  from app");

        return 0;
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
