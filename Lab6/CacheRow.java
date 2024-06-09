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
        // System.out.println("\t" + lastUsed.toString());
        int aOffset = -1;
        for (int i = 0; i < tags.length; i++){
            if(tags[i] == tag){
                aOffset = i;
                break;
            }
        }
        
        // Tag isn't found (miss)
        if(aOffset == -1){

            // Try to find open space
            int unusedIndex = -1;
            for (int i = 0; i < tags.length; i++){
                if (tags[i] == -1){
                    unusedIndex = i;
                    break;
                }
            }

            // If no open space, replace least recently used
            if(unusedIndex == -1){
                tags[leastRecentlyUsed()] = tag;
                updateLastUsed(leastRecentlyUsed());

            // If open space, write tag in open slot
            } else {
                tags[unusedIndex] = tag;
                lastUsed.add(unusedIndex);
            }

            // System.out.println(false);
            return false;

        // Tag is found (hit)
        } else {
           updateLastUsed(aOffset);
        //    System.out.println(true);
           return true;
        }
    }

    
}
