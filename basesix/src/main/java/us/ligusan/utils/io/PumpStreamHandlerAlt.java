package us.ligusan.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Blocking alternative to {@link PumpStreamHandler}.
 * Implementation is reading constantly from inputs.
 *
 */
public class PumpStreamHandlerAlt implements ExecuteStreamHandler
{
    private final InputStream input;
    private final OutputStream output;
    private final OutputStream error;

    private Thread inputPump;
    private Thread outputPump;
    private Thread errorPump;

    public PumpStreamHandlerAlt(final InputStream pInput, final OutputStream pOutput, final OutputStream pError)
    {
        input = pInput;
        output = pOutput;
        error = pError;
    }

    private Thread createPump(final InputStream pInput, final OutputStream pOutput)
    {
        Thread ret = new Thread()
        {
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
        };
        ret.setDaemon(true);
        return ret;
    }

    @Override
    public void setProcessErrorStream(final InputStream pProcessErrorStream)
    {
        errorPump = createPump(pProcessErrorStream, error);
    }

    @Override
    public void setProcessInputStream(final OutputStream pProcessInputStream)
    {
        inputPump = createPump(input, pProcessInputStream);
    }

    @Override
    public void setProcessOutputStream(final InputStream pProcessOutputStream)
    {
        outputPump = createPump(pProcessOutputStream, output);
    }

    @Override
    public void start()
    {
        for(Thread lThread : Arrays.asList(errorPump, outputPump, inputPump))
            lThread.start();
    }

    @Override
    public void stop()
    {}
}
