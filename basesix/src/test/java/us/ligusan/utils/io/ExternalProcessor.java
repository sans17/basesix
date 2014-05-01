package us.ligusan.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.IOUtils;

public class ExternalProcessor
{
    public static void main(final String... pArgs) throws IOException
    {
        BufferedReader lReader = null;
        try
        {
            lReader = new BufferedReader(new InputStreamReader(System.in));
            for(String lInput = null; (lInput = lReader.readLine()) != null;)
            {
                System.out.println("out: " + lInput);
                System.err.println("err: " + lInput);
            }
        }
        finally
        {
            IOUtils.closeQuietly(lReader);
        }
    }
}
