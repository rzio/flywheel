package pro.reznick.flywheel.http;


import com.google.inject.Inject;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.MissingCollectionException;
import pro.reznick.flywheel.exceptions.OperationFailedException;
import pro.reznick.flywheel.service.CollectionManagementService;
import pro.reznick.flywheel.service.DataService;
import pro.reznick.flywheel.service.KeyFactory;
import pro.reznick.flywheel.service.OperationStatus;

import java.util.List;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;

/**
 * @author alex
 * @since 12/9/11 10:08 AM
 */

public class FlywheelHandler extends SimpleChannelUpstreamHandler
{
    DataService service;
    private CollectionManagementService collectionManagementService;
    KeyFactory factory;

    @Inject
    public FlywheelHandler(DataService service, CollectionManagementService collectionManagementService, KeyFactory factory)
    {
        this.service = service;
        this.collectionManagementService = collectionManagementService;
        this.factory = factory;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        HttpRequest request = (HttpRequest) e.getMessage();

        final HttpMethod requestMethod = request.getMethod();
        final String path = request.getUri();
        if (path == null)
        {
            error(ctx, request.getProtocolVersion(), FORBIDDEN);
            return;
        }

        Key k;
        try
        {
            k = parseKeyFromPath(path);
        }
        catch (MissingCollectionException ex)
        {
            if (requestMethod == PUT)
            { // Only set operation can create a new collection
                try
                {
                    collectionManagementService.createCollection(ex.getCollectionName());
                    k = parseKeyFromPath(path);
                }
                catch (Exception e1)
                {
                    error(ctx, request.getProtocolVersion(), INTERNAL_SERVER_ERROR);
                    return;
                }
            }
            else
            {
                error(ctx, request.getProtocolVersion(), NOT_FOUND);
                return;
            }

        }

        if (k == null)
        {
            error(ctx, request.getProtocolVersion(), NOT_FOUND);
            return;
        }


        if (requestMethod == PUT)
            parsePutRequest(k, request, path, ctx, e);
        else if (requestMethod == GET)
            parseGetRequest(k, request, path, ctx, e);
        else if (requestMethod == HEAD)
            parseHeadRequest(k, request, path, ctx, e);
        else if (requestMethod == DELETE)
            parseDeleteRequest(k, request, path, ctx, e);
        else if (requestMethod == POST)
            parsePostRequest(k, request, path, ctx, e);
        else
            error(ctx, request.getProtocolVersion(), METHOD_NOT_ALLOWED);
    }


    private void parsePutRequest(Key k, HttpRequest request, String path, ChannelHandlerContext ctx, MessageEvent e)
    {
        try
        {
            ChannelBuffer buff = request.getContent();
            String contentType = request.getHeader(CONTENT_TYPE);
            HttpResponseStatus st = INTERNAL_SERVER_ERROR;
            if (buff.readable())
            {

                switch (service.put(k, new Entity(buff.array(), contentType)))
                {
                    case CREATED_ENTITY:
                        st = CREATED;
                        break;
                    case REPLACED_ENTITY:
                        st = NO_CONTENT;
                        break;
                    case ACCEPTED_REQUEST:
                        st = ACCEPTED;
                        break;
                    case FAILED:
                    default:
                        break;
                }
            }

            if (st.getCode() >= 200 && st.getCode() < 300)
                respond(request, ctx, st);
            else
                error(ctx, request.getProtocolVersion(), INTERNAL_SERVER_ERROR);
        }
        catch (OperationFailedException e1)
        {
            error(ctx, request.getProtocolVersion(), INTERNAL_SERVER_ERROR);
        }

    }

    private void parseGetRequest(Key k, HttpRequest request, String path, ChannelHandlerContext ctx, MessageEvent e)
    {
        Entity entity = service.get(k);
        if (entity == null)
        {
            error(ctx, request.getProtocolVersion(), NOT_FOUND);
            return;
        }

        String accept = request.getHeader(ACCEPT);
        if (!MediaTypeMatcher.matchAcceptHeader(entity.getMediaType(), accept))
        {
            error(ctx, request.getProtocolVersion(), NOT_ACCEPTABLE);
            return;
        }
        writeEntity(request, ctx, entity);
    }


    private void parseHeadRequest(Key k, HttpRequest request, String path, ChannelHandlerContext ctx, MessageEvent e)
    {
        Entity entity = service.get(k);
        if (entity == null)
        {
            error(ctx, request.getProtocolVersion(), NOT_FOUND);
            return;
        }

        String accept = request.getHeader(ACCEPT);
        if (!MediaTypeMatcher.matchAcceptHeader(entity.getMediaType(), accept))
        {
            error(ctx, request.getProtocolVersion(), NOT_ACCEPTABLE);
            return;
        }
        respond(request, ctx, OK);

    }

    private void parseDeleteRequest(Key k, HttpRequest request, String path, ChannelHandlerContext ctx, MessageEvent e)
    {
        OperationStatus st = service.delete(k);
        if (st == OperationStatus.OK)
            respond(request, ctx, NO_CONTENT);
        if (st == OperationStatus.FAILED)
            error(ctx,request.getProtocolVersion(),NOT_FOUND);
    }

    private void parsePostRequest(Key k, HttpRequest request, String path, ChannelHandlerContext ctx, MessageEvent e)
    {

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception
    {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException)
        {
            error(ctx, HTTP_1_0, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected())
        {
            error(ctx, HTTP_1_0, INTERNAL_SERVER_ERROR);
        }
    }


    private Key parseKeyFromPath(String path) throws MissingCollectionException
    {
        List<String> tokenized = PathTokenizer.tokenize(path);
        if (tokenized.size() == 2)
            return factory.keyFromCollectionAndKey(tokenized.get(0), tokenized.get(1));
        else if (tokenized.size() == 1)
            return factory.keyFromKey(tokenized.get(0));
        return null;
    }


    private void writeEntity(HttpRequest request, ChannelHandlerContext ctx, Entity entity)
    {
        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), OK);
        setContentLength(response, entity.getData().length);
        response.setHeader(CONTENT_TYPE, entity.getMediaType());
        Channel ch = ctx.getChannel();

        ch.write(response);
        ChannelFuture writeFuture;
        writeFuture = ch.write(ChannelBuffers.wrappedBuffer(entity.getData()));


        // Decide whether to close the connection or not.
        if (!isKeepAlive(request))
        {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }


    private void respond(HttpRequest request, ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        boolean keepAlive = isKeepAlive(request);
        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (status == OK)
        {
            response.setContent(ChannelBuffers.copiedBuffer("OK", CharsetUtil.UTF_8));
        }
        if (keepAlive)
        {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }
        ChannelFuture future = ctx.getChannel().write(response);
        if (!keepAlive)
            future.addListener(ChannelFutureListener.CLOSE);

    }

    private void error(ChannelHandlerContext ctx, HttpVersion protocolVersion, HttpResponseStatus status)
    {
        HttpResponse response = new DefaultHttpResponse(protocolVersion, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8));

        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}