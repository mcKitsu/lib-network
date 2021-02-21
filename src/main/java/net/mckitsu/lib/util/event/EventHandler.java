package net.mckitsu.lib.util.event;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

public class EventHandler<T, U> {
    private final ConcurrentLinkedQueue<BiConsumer<T, U>> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    public void clear(){
        this.concurrentLinkedQueue.clear();
    }

    public boolean add(BiConsumer<T, U> event){
        return this.concurrentLinkedQueue.add(event);
    }

    public boolean remove(BiConsumer<T, U> event){
        return this.concurrentLinkedQueue.remove(event);
    }

    public void execute(T t, U u){
        for(BiConsumer<T, U> event : this.concurrentLinkedQueue){
            try{
                event.accept(t, u);
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
}
