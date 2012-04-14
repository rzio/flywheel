package pro.reznick.flywheel.http;

import com.google.inject.Inject;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author alex
 * @since 12/9/11 10:06 AM
 */

public class FlywheelPipelineFactory implements ChannelPipelineFactory
{


    private final ChannelUpstreamHandler handler ;

    @Inject
    public FlywheelPipelineFactory(ChannelUpstreamHandler handler)
    {
        this.handler = handler;
    }


    public ChannelPipeline getPipeline() throws Exception
    {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(2097152));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", handler);
        return pipeline;
    }
}
