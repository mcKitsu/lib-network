package net.mckitsu.lib.network;

import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface Channel {
    <A> void connect(SocketAddress remoteAddress, A attachment, CompletionHandlerEvent<Void, A> handlerEvent);

    boolean isConnect();

    boolean isOpen();

    void close();

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    <A> void read(ByteBuffer transferByteBuffer, A attachment, CompletionHandlerEvent<Integer, A> handlerEvent);

    <A> void write(ByteBuffer receiverByteBuffer, A attachment, CompletionHandlerEvent<Integer, A> handlerEvent);
}
