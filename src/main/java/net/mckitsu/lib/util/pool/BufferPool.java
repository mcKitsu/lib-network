package net.mckitsu.lib.util.pool;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BufferPool<E> {
    private final Queue<E> idleByteBuffer = new ConcurrentLinkedQueue<>();
    private final Queue<E> availableByteBuffer = new ConcurrentLinkedQueue<>();

    private final int max;
    private final int min;

    private long lastRecode;
    private long maxLimit;
    private int recycleTimeInterval = 2;
    /* **************************************************************************************
     *  Abstract method
     */
    protected abstract E allocElement();
    protected abstract void freeElement(E element);
    protected abstract E resetElement(E element);

    /* **************************************************************************************
     *  Construct method
     */
    protected BufferPool(int max, int min){
        this.max = Math.max(min, max);
        this.min = min;
        this.lastRecode = Instant.now().getEpochSecond();
    }

    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */
    public int size(){
        return idleByteBuffer.size() + availableByteBuffer.size();
    }

    public int sizeIdle(){
        return this.idleByteBuffer.size();
    }

    public int sizeAvailable(){
        return this.availableByteBuffer.size();
    }

    public int max(){
        return this.max;
    }

    public int min(){
        return this.min;
    }

    public E alloc(){
        E element = this.idleByteBuffer.poll();
        if(element != null){
            this.availableByteBuffer.add(element);
        }else if(this.size()<this.max){
            element = allocElement();
            if(element != null)
                this.availableByteBuffer.add(element);
        }

        this.recycleAuto();
        return element;
    }

    public boolean free(E element){
        boolean listResult = this.availableByteBuffer.remove(element);
        this.recycleAuto();
        if(!listResult)
            return false;

        E clearElement = resetElement(element);
        if(clearElement != null)
            this.idleByteBuffer.add(clearElement);

        return true;
    }

    public int recycle(){
        return this.recycle(Instant.now().getEpochSecond());
    }

    public int getRecycleTimeInterval(){
        return this.recycleTimeInterval;
    }

    public void setRecycleTimeInterval(int millisecond){
        this.recycleTimeInterval = millisecond;
    }

    /* **************************************************************************************
     *  protected method
     */
    protected void poolInit(){
        for(int i=0; i<this.min; i++){
            E cache = allocElement();
            if(cache != null){
                this.idleByteBuffer.add(cache);
            }
        }
    }

    /* **************************************************************************************
     *  Private method
     */
    private void recycleAuto(){
        long now = Instant.now().getEpochSecond();
        if(now-this.lastRecode >= recycleTimeInterval){
            this.recycle(now);
        }else{
            this.maxLimit = Math.min(lastRecode, sizeAvailable());
        }
    }

    private int recycle(long now){
        this.lastRecode = now;
        if(this.size()<=this.min)
            return 0;

        int recycleNumb = (int)Math.ceil((size() - this.maxLimit) * 0.2);
        if(recycleNumb>0){
            if((this.size()-this.min) < recycleNumb)
                recycleNumb = this.size()-this.min;

            for(int i=0; i<recycleNumb; i++){
                this.freeElement(this.idleByteBuffer.poll());
            }
        }

        this.maxLimit = sizeAvailable();
        return recycleNumb;
    }

}