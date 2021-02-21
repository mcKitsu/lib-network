package net.mckitsu.lib.network;

public enum NetworkExceptionList {
    CONNECT_FAIL,
    ALREADY_CONNECT,
    CHANNEL_OPEN_FAIL,
    CHANNEL_WRITE_FAIL,
    CHANNEL_READ_FAIL,
    OUT_OF_MTU,
    PARAMETER_NOT_MATCH,
    HANDSHAKE_FAIL,
    LOCAL_CRASH,
    ILLEGAL_ACCESS,
    SUCCESSFUL
}
