package net.mckitsu.lib.util.pool;

public class BufferPools {
    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct method
     */
    private BufferPools(){}

    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */
    public static ByteBufferPool newFixedBufferPool(int nBuffers, int bufferSize){
        return new ByteBufferPool(nBuffers, nBuffers, bufferSize);
    }

    public static ByteBufferPool newCacheBufferPool(int nBuffersMin, int nBuffersMax, int bufferSize){
        return new ByteBufferPool(nBuffersMin, nBuffersMax, bufferSize);
    }

    public static ByteBufferPool newCacheBufferPool(int nBuffersMin, int bufferSize){
        return new ByteBufferPool(nBuffersMin, 2147483647, bufferSize);
    }

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  Private method
     */
}
