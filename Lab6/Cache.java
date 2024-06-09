import java.util.*;


public class Cache {
    private CacheRow[] cache;
    private int cacheSize;
    private int blockSize;
    private int numLines;

    public Cache(int storageInKB, 
        int associativity, 
        int blockWordLength){
        
        blockSize = blockWordLength;
        
    }
}

/* 
 * 2, 1, 1 
 * 2, 1, 2
 * 2, 1, 4
 * 2, 2, 1
 * 2, 4, 1
 * 2, 4, 4
 * 4, 1, 1
 */
