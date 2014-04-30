package us.ligusan.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.io.IOUtils;

public class PumpStreamHandler implements ExecuteStreamHandler
{
    private static class StreamPumper implements Runnable
    {
        private final InputStream input;
        private final OutputStream output;
        private final boolean toCloseInput;
        private final boolean toCloseOutput;

        private volatile boolean stopped;

        public StreamPumper(final InputStream pInput, final OutputStream pOutput, final boolean pCloseInputOnStop, final boolean pCloseOutputOnStop)
        {
            input = pInput;
            output = pOutput;

            toCloseInput = pCloseInputOnStop;
            toCloseOutput = pCloseOutputOnStop;
        }

        protected InputStream getInput()
        {
            return input;
        }

        protected OutputStream getOutput()
        {
            return output;
        }

        protected boolean isToCloseInput()
        {
            return toCloseInput;
        }

        protected boolean isToCloseOutput()
        {
            return toCloseOutput;
        }

        protected boolean isStopped()
        {
            return stopped;
        }

        @Override
        public void run()
        {
            InputStream lInput = getInput();
            if(lInput != null) try
            {
                for(int lRead = -1; (lRead = lInput.read()) >= 0;)
                {
                    OutputStream lOutput = getOutput();
                    if(lOutput != null)
                    {
                        lOutput.write(lRead);
                        lOutput.flush();
                    }
                }
            }
            catch(IOException e)
            {}
        }

        protected void setStopped(final boolean pStopped)
        {
            stopped = pStopped;
        }

        protected void stop()
        {
            setStopped(true);
            if(isToCloseInput()) IOUtils.closeQuietly(getInput());
            if(isToCloseOutput()) IOUtils.closeQuietly(getOutput());
        }
    }

    private final InputStream input;
    private final OutputStream output;
    private final OutputStream error;

    private StreamPumper processInputStreamReader;
    private StreamPumper processOutputStreamReader;
    private StreamPumper processErrorStreamReader;

    public PumpStreamHandler(final InputStream pInput, final OutputStream pOutput, final OutputStream pError)
    {
        input = pInput;
        output = pOutput;
        error = pError;
    }

    protected OutputStream getError()
    {
        return error;
    }

    protected InputStream getInput()
    {
        return input;
    }

    protected OutputStream getOutput()
    {
        return output;
    }

    protected StreamPumper getProcessErrorStreamReader()
    {
        return processErrorStreamReader;
    }

    protected StreamPumper getProcessInputStreamReader()
    {
        return processInputStreamReader;
    }

    protected StreamPumper getProcessOutputStreamReader()
    {
        return processOutputStreamReader;
    }

    protected List<StreamPumper> getReaders()
    {
        return Arrays.asList(getProcessInputStreamReader(), getProcessOutputStreamReader(), getProcessErrorStreamReader());
    }

    @Override
    public void setProcessErrorStream(final InputStream pProcessErrorStream)
    {
        setProcessErrorStreamReader(new StreamPumper(pProcessErrorStream, getError(), true, false));
    }

    protected void setProcessErrorStreamReader(final StreamPumper pProcessErrorStreamReader)
    {
        processErrorStreamReader = pProcessErrorStreamReader;
    }

    @Override
    public void setProcessInputStream(final OutputStream pProcessInputStream)
    {
        setProcessInputStreamReader(new StreamPumper(getInput(), pProcessInputStream, false, true));
    }

    protected void setProcessInputStreamReader(final StreamPumper pProcessInputStreamReader)
    {
        processInputStreamReader = pProcessInputStreamReader;
    }

    @Override
    public void setProcessOutputStream(final InputStream pProcessOutputStream)
    {
        setProcessOutputStreamReader(new StreamPumper(pProcessOutputStream, getOutput(), true, false));
    }

    protected void setProcessOutputStreamReader(final StreamPumper pProcessOutputStreamReader)
    {
        processOutputStreamReader = pProcessOutputStreamReader;
    }

    @Override
    public void start()
    {
        for(StreamPumper lStreamPumper : getReaders())
        {
            Thread lThread = new Thread(lStreamPumper);
            lThread.setDaemon(true);
            lThread.start();
        }
    }

    @Override
    public void stop()
    {
        for(StreamPumper lStoppable : getReaders())
            lStoppable.stop();
    }
}
