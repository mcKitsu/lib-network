package net.mckitsu.lib.network;

public class NetworkException extends Exception {
    public NetworkException(NetworkExceptionList channelExceptionList){
        super(channelExceptionList.name());
    }

    public NetworkException(NetworkExceptionList channelExceptionList, Throwable throwable){
        super(channelExceptionList.name(), throwable);
    }
}
