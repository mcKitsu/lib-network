package net.mckitsu.lib.network.net;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class EventHandler {
    private final Executor executor;

    protected EventHandler(Executor executor){
        this.executor = executor;
    }

    protected EventHandler(){
        this.executor = null;
    }

    protected boolean execute(Consumer consumer, Object object){
        return execute(consumer, object, null);
    }

    protected boolean execute(Consumer consumer, Object object, Runnable finish){
        if(consumer == null)
            return false;

        if(this.executor != null){
            this.executor.execute(() -> {
                consumer.accept(object);
                if(finish!=null)
                    finish.run();
            });
        } else{
            consumer.accept(object);
            if(finish != null)
                finish.run();
        }
        return true;
    }

    protected boolean execute(Runnable runnable){
        return execute(runnable, null);
    }

    protected boolean execute(Runnable runnable, Runnable finish){
        if(runnable != null){
            if(this.executor != null) {
                this.executor.execute(() -> {
                    runnable.run();
                    if (finish != null)
                        finish.run();
                });


            }else {
                runnable.run();
                if(finish != null)
                    finish.run();
            }

            return true;
        }else{
            return false;
        }
    }
}
