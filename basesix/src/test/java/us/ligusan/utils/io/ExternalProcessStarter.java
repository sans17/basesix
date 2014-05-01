package us.ligusan.utils.io;

import java.io.File;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ExternalProcessStarter
{
    public static void main(final String... pArgs)
    {
        int lExitStatus = 1;
        try
        {
            StringBuilder lStringBuilder = new StringBuilder();
            for(File lFile : FileUtils.listFiles(new File("."), new String[] {"jar"}, false))
                lStringBuilder.append(lFile.getPath()).append(File.pathSeparatorChar);
            String lClassPath = StringUtils.removeEnd(lStringBuilder.toString(), File.pathSeparator);

            CommandLine lCommandLine = new CommandLine("java").addArgument("-cp").addArgument(lClassPath).addArgument(ExternalProcessor.class.getName()).addArguments(pArgs);

            DefaultExecutor lDefaultExecutor = new DefaultExecutor();
            lDefaultExecutor.setStreamHandler(new PumpStreamHandlerAlt(System.in, System.out, System.err));
            lDefaultExecutor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

            lExitStatus = lDefaultExecutor.execute(lCommandLine);
        }
        catch(Exception e)
        {}

        System.exit(lExitStatus);
    }
}
