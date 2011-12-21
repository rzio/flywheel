package pro.reznick.flywheel.http;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @author alex
 * @since 12/15/11 3:58 PM
 */

public class FlywheelServer
{
    public static final String serverName = "flywheel-http";
    private final ChannelPipelineFactory pipelineFactory;
    private final int port;
    private final ChannelGroup allChannels = new DefaultChannelGroup(serverName);
    private final NioServerSocketChannelFactory channelFactory;

    @Inject
    public FlywheelServer(ChannelPipelineFactory pipelineFactory,@Named("serverPort") int port)
    {
        this.pipelineFactory = pipelineFactory;
        this.port = port;
        channelFactory = new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
    }


    public void start()
    {
        ServerBootstrap bootstrap = new ServerBootstrap(
                channelFactory);

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(pipelineFactory);

        // Bind and start to accept incoming connections.
        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        allChannels.add(channel);
    }

    public void stop()
    {
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        channelFactory.releaseExternalResources();
    }
}
