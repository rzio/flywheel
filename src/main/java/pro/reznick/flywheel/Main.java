package pro.reznick.flywheel;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.http.FlywheelServer;

/**
 * @author alex
 * @since 11/23/11 4:26 PM
 */

public class Main
{
    private static final Object stopEvent = new Object();

    public static void main(String[] args) throws InvalidConfigurationException, ConfigurationException, CollectionAlreadyExistsException, FailedSavingConfigurationException, InterruptedException
    {
        CommandLine line = parseCLA(args);
        String topologyFilePath = line.getOptionValue("topology");
        XMLConfiguration config = new XMLConfiguration(topologyFilePath);


        Injector injector = Guice.createInjector(new FlywheelModule(config));
        final FlywheelServer server = injector.getInstance(FlywheelServer.class);
//        final DB db = injector.getInstance(DB.class);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                server.stop();
//                db.close();
                synchronized (stopEvent)
                {
                    stopEvent.notifyAll();
                }

            }
        }));

        synchronized (stopEvent)
        {
            stopEvent.wait();
        }
    }

    private static CommandLine parseCLA(String[] args)
    {
        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption("t", "topology", true, "the path to the topology file");

        try
        {
            CommandLine line = parser.parse(options, args);
            if (!line.hasOption("t"))
            {
                printHelpAndExit(options);
            }
            return line;
        }
        catch (ParseException e)
        {
            printHelpAndExit(options);
        }
        return null;
    }

    private static void printHelpAndExit(Options options)
    {
        System.err.println("Failed parsing command line arguments");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("", options);
        System.exit(1);
    }
}


