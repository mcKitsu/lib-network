package net.mckitsu.lib.util.event;

import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

public class CompletionHandlerEvent<R, T> implements CompletionHandler<R, T> {
    private final BiConsumer<R, T> completed;
    private final BiConsumer<Throwable, T>failed;

    public CompletionHandlerEvent(BiConsumer<R, T> completed, BiConsumer<Throwable, T>failed){
        this.completed = completed;
        this.failed = failed;
    }

    @Override
    public void completed(R result, T attachment) {
        this.completed.accept(result, attachment);
    }

    @Override
    public void failed(Throwable exc, T attachment) {
        this.failed.accept(exc, attachment);
    }
}
