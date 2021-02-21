package net.mckitsu.lib.network;

import java.net.SocketAddress;

public interface Client {
    void connect(SocketAddress socketAddress);

    void close();

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();

    boolean isConnect();

    boolean isOpen();

    void write(byte[] data);
}
