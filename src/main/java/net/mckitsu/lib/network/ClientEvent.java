package net.mckitsu.lib.network;

import net.mckitsu.lib.network.tcp.TcpClient;

public interface ClientEvent {
    void onConnect(TcpClient tcpClient);

    void onConnectFail(TcpClient tcpClient);

    void onDisconnect(TcpClient tcpClient);

    void onDisconnectRemote(TcpClient tcpClient);

    void onTransfer(TcpClient tcpClient, byte[] data);

    void onReceiver(TcpClient tcpClient, byte[] data);
}
