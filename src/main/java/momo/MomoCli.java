package momo;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "momo", mixinStandardHelpOptions = true, version = "0.1",
        description = "Records, checks and evaluates daily working hours.")
public class MomoCli implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("... test it ...");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MomoCli()).execute(args);
        System.exit(exitCode);
    }
}
