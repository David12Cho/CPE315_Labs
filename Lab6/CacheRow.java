import java.util.*;

public class CacheRow {
    private int[] tags;
    private ArrayList<Integer> lastUsed;

    public CacheRow(int associativity){
        tags = new int[associativity];
        Arrays.fill(tags, -1);
        lastUsed = new ArrayList<Integer>();
    }

    public int getTagAt(int aOffset){
        return tags[aOffset];
    }

    public int[] getTags(){
        return tags;
    }

    public void setTagAt(int tag, int aOffset){
        tags[aOffset] = tag;
    }

    public boolean updateLastUsed(int aOffset){
        boolean removed = lastUsed.remove((Integer) aOffset);
        lastUsed.add(aOffset);

        return removed;
    }

    public int leastRecentlyUsed(){
        return lastUsed.get(0);
    }

    public boolean hitOrMiss(int tag){
        int aOffset = Arrays.binarySearch(tags, tag);
        
        if(aOffset == -1){
            int unusedIndex = Arrays.binarySearch(tags, -1);

            if(unusedIndex == -1){
                tags[leastRecentlyUsed()] = tag;
                updateLastUsed(leastRecentlyUsed());
            } else {
                tags[unusedIndex] = tag;
                int modified = lastUsed.remove(0);
                lastUsed.add(modified);
            }


            return false;
        } else {
           updateLastUsed(aOffset);
           return true;
        }
    }

    
}
