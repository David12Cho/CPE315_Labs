import java.util.*;


public class Cache{
    private CacheRow[] cache;
    private int blockSize;
    private int numLines;
    private int storageInKB;
    private int associativity;

    private int indexBitLength;
    private int blockOffSetLength;

    public Cache(
            int storageInKB, 
            int associativity, 
            int blockWordLength
        ){
        
        this.storageInKB = storageInKB;
        this.associativity = associativity;
        blockSize = blockWordLength;

        numLines = (int) (Math.pow(2, 10) * storageInKB / associativity / (blockWordLength * 4));
        cache = new CacheRow[numLines];        
        Arrays.fill(cache, new CacheRow(associativity));

        indexBitLength = log2(numLines);
        blockOffSetLength = log2(blockWordLength);
    }

    public int getStorage(){
        return storageInKB;
    }
    public int getBlockSize(){
        return blockSize;
    }
    public int getAssociativity(){
        return associativity;
    }

    public boolean analyzeAddress(int address){
        boolean wasHit = false;

        // find tag
        int shiftLeftBy = 2 + indexBitLength + blockOffSetLength;
        int tag = address << shiftLeftBy;

        // find index
        shiftLeftBy = 2 + blockOffSetLength;
        int index = (address << shiftLeftBy) % numLines;

        // is tag at index (any where)
        wasHit = cache[index].hitOrMiss(tag);

        return wasHit;
    }

    private int log2(int x){
        return (int) (Math.log(x) / Math.log(2));
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
