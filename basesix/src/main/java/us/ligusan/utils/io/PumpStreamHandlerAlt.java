package us.ligusan.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.io.IOUtils;

public class PumpStreamHandlerAlt implements ExecuteStreamHandler
{
    private static class StreamPumper implements Runnable
    {
        private final InputStream input;
        private final OutputStream output;
        private final boolean toCloseInput;
        private final boolean toCloseOutput;

        private StreamPumper(final InputStream pInput, final OutputStream pOutput, final boolean pCloseInputOnStop, final boolean pCloseOutputOnStop)
        {
            input = pInput;
            output = pOutput;

            toCloseInput = pCloseInputOnStop;
            toCloseOutput = pCloseOutputOnStop;
        }

        @Override
        public void run()
        {
            if(input != null) try
            {
                for(int lRead = -1; (lRead = input.read()) >= 0;)
                    if(output != null)
                    {
                        output.write(lRead);
                        output.flush();
                    }
            }
            catch(IOException e)
            {}
        }

        protected void stop()
        {
            if(toCloseInput) IOUtils.closeQuietly(input);
            if(toCloseOutput) IOUtils.closeQuietly(output);
        }
    }

    private final InputStream input;
    private final OutputStream output;
    private final OutputStream error;

    private StreamPumper processInputStreamReader;
    private StreamPumper processOutputStreamReader;
    private StreamPumper processErrorStreamReader;

    public PumpStreamHandlerAlt(final InputStream pInput, final OutputStream pOutput, final OutputStream pError)
    {
        input = pInput;
        output = pOutput;
        error = pError;
    }

    private List<StreamPumper> getStreamPumpers()
    {
        return Arrays.asList(processInputStreamReader, processOutputStreamReader, processErrorStreamReader);
    }

    @Override
    public void setProcessErrorStream(final InputStream pProcessErrorStream)
    {
        processErrorStreamReader = new StreamPumper(pProcessErrorStream, error, true, false);
    }

    @Override
    public void setProcessInputStream(final OutputStream pProcessInputStream)
    {
        processInputStreamReader = new StreamPumper(input, pProcessInputStream, false, true);
    }

    @Override
    public void setProcessOutputStream(final InputStream pProcessOutputStream)
    {
        processOutputStreamReader = new StreamPumper(pProcessOutputStream, output, true, false);
    }

    @Override
    public void start()
    {
        for(StreamPumper lStreamPumper : getStreamPumpers())
        {
            Thread lThread = new Thread(lStreamPumper);
            lThread.setDaemon(true);
            lThread.start();
        }
    }

    @Override
    public void stop()
    {
        for(StreamPumper lStoppable : getStreamPumpers())
            lStoppable.stop();
    }
}
