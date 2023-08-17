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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;

import momo.MomoCli;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import static picocli.CommandLine.Spec.Target.MIXEE;

/**
 * This is a mixin that adds a {@code --verbose} option to a command.
 * This class will configure Log4j2, using the specified verbosity:
 * <ul>
 *   <li>{@code -vv} : TRACE level is enabled</li>
 *   <li>{@code -v} : DEBUG level is enabled</li>
 *   <li>(not specified) : WARN/INFO levels is enabled</li>
 * </ul>
 * <p>
 *   To add the {@code --verbose} option to a command, simply declare a {@code @Mixin}-annotated field with type
 *   {@code LoggingMixin}
 *   (if your command is a class), or a {@code @Mixin}-annotated method parameter of type {@code LoggingMixin} if
 *   your command
 *   is a {@code @Command}-annotated method.
 * </p>
 * <p>
 *   This mixin can be used on multiple commands, on any level in the command hierarchy.
 * </p>
 * <p>
 *   Make sure that {@link #configureLoggers} is called before executing any command.
 *   This can be accomplished with:
 * </p><pre>
 * public static void main(String... args) {
 *     new CommandLine(new MyApp())
 *             .setExecutionStrategy(LoggingMixin::executionStrategy))
 *             .execute(args);
 * }
 * </pre>
 */
public class LoggingMixin
{
    /**
     * This mixin is able to climb the command hierarchy because the
     * {@code @Spec(Target.MIXEE)}-annotated field gets a reference to the command where it is used.
     */
    private @Spec(MIXEE) CommandSpec mixee; // spec of the command where the @Mixin is used

    private boolean[] verbosity = new boolean[0];

    // Each subcommand that mixes in the LoggingMixin has its own instance of this class,
    // so there may be many LoggingMixin instances.
    // We want to store the verbosity value in a single, central place, so
    // we find the top-level command,
    // and store the verbosity level on our top-level command's LoggingMixin.
    //
    // In the main method, `LoggingMixin::executionStrategy` should be set as the execution strategy:
    // that will take the verbosity level that we stored in the top-level command's LoggingMixin
    // to configure Log4j2 before executing the command that the user specified.
    private static LoggingMixin getTopLevelCommandLoggingMixin(CommandSpec commandSpec)
    {
        return ((MomoCli) commandSpec.root().userObject()).getLoggingMixin();
    }

    /**
     * Sets the specified verbosity on the LoggingMixin of the top-level command.
     *
     * @param verbosity
     *         the new verbosity value
     */
    @Option(names = { "-v", "--verbose" }, description = {
            "Specify multiple -v options to increase verbosity.",
            "For example, `-v -v` or `-vv`"
    })
    public void setVerbose(boolean[] verbosity)
    {
        getTopLevelCommandLoggingMixin(mixee).verbosity = verbosity;
    }

    /**
     * Returns the verbosity from the LoggingMixin of the top-level command.
     *
     * @return the verbosity value
     */
    public boolean[] getVerbosity()
    {
        return getTopLevelCommandLoggingMixin(mixee).verbosity;
    }

    /**
     * Configures Log4j2 based on the verbosity level of the top-level command's LoggingMixin,
     * before invoking the default execution strategy ({@link picocli.CommandLine.RunLast RunLast}) and returning the
     * result.
     * <p>
     * Example usage:
     * </p>
     * <pre>
     * public void main(String... args) {
     *     new CommandLine(new MyApp())
     *             .setExecutionStrategy(LoggingMixin::executionStrategy))
     *             .execute(args);
     * }
     * </pre>
     *
     * @param parseResult
     *         represents the result of parsing the command line
     *
     * @return the exit code of executing the most specific subcommand
     */
    public static int executionStrategy(ParseResult parseResult)
    {
        getTopLevelCommandLoggingMixin(parseResult.commandSpec()).configureLoggers();
        return new CommandLine.RunLast().execute(parseResult);
    }

    /**
     * Configures the Log4j2 console appender(s), using the specified verbosity:
     * <ul>
     *   <li>{@code -vv} : enable TRACE level</li>
     *   <li>{@code -v} : enable DEBUG level</li>
     *   <li>(not specified) : enable WARN and INFO levels</li>
     * </ul>
     */
    public void configureLoggers()
    {
        Level level = getTopLevelCommandLoggingMixin(mixee).calcLogLevel();

        LoggerContext loggerContext = LoggerContext.getContext(false);
        LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
        for (Appender appender : rootConfig.getAppenders().values())
        {
            if (appender instanceof ConsoleAppender)
            {
                rootConfig.removeAppender(appender.getName());
                rootConfig.addAppender(appender, level, null);
            }
        }
        if (rootConfig.getLevel().isMoreSpecificThan(level))
        {
            rootConfig.setLevel(level);
        }
        loggerContext.updateLoggers(); // apply the changes
    }

    private Level calcLogLevel()
    {
        if (getVerbosity().length == 2)
        {
            return Level.TRACE;
        }
        else
        {
            return Level.TRACE;
        }
    }
}