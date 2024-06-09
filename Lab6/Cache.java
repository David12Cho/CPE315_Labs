import java.util.*;


public class Cache{
    private CacheRow[] cache;
    private int blockSize;
    private int numLines;
    private int storageInKB;
    private int associativity;

    private int indexBitLength;
    private int blockOffSetLength;

    private int numHits;
    private int totalTries;

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
        for(int i = 0; i < cache.length; i++){
            cache[i] = new CacheRow(associativity);
        }
        // Arrays.fill(cache, new CacheRow(associativity));

        indexBitLength = log2(numLines);
        blockOffSetLength = log2(blockWordLength);

        numHits = 0;
        totalTries = 0;
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

    public int getNumHits(){
        return numHits;
    }

    public int getTotalTries(){
        return totalTries;
    }

    public int getNumLines(){
        return numLines;
    }

    public int getIndexBitLength(){
        return indexBitLength;
    }

    public int getBlockOffSetLength(){
        return blockOffSetLength;
    }

    public boolean analyzeAddress(int address){
        boolean wasHit = false;

        // find tag
        int shiftLeftBy = 2 + indexBitLength + blockOffSetLength;
        int tag = address >> shiftLeftBy;

        // find index
        shiftLeftBy = 2 + blockOffSetLength;
        int index = (address >> shiftLeftBy) % numLines;

        // System.out.println("Address: " + address + "\tTag: " + tag + "\tIndex: " + index);
        // System.out.print("\tBlocks: ");
        // for(int x : cache[index].getTags()){
        //     System.out.print(x + ", ");
        // }
        // System.out.println();

        // is tag at index (any where)
        wasHit = cache[index].hitOrMiss(tag);

        if (wasHit){
            numHits++;
        }
        totalTries++;
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
