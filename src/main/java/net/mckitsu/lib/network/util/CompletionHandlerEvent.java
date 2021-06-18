package net.mckitsu.lib.network.util;

import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

public class CompletionHandlerEvent<V, A> implements CompletionHandler<V, A> {
    private final BiConsumer<V, A> completed;
    private final BiConsumer<Throwable, A>failed;

    public CompletionHandlerEvent(BiConsumer<V, A> completed, BiConsumer<Throwable, A>failed){
        this.completed = completed;
        this.failed = failed;
    }

    @Override
    public void completed(V result, A attachment) {
        this.completed.accept(result, attachment);
    }

    @Override
    public void failed(Throwable exc, A attachment) {
        this.failed.accept(exc, attachment);
    }
}
