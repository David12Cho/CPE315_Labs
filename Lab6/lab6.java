import java.io.File;
import java.io.IOException;
import java.util.*;

public class lab6 {

    public static void main(String[] args)throws IOException{
        Scanner scanner;
        // scanner = new Scanner(new File("mem_stream.1"));
        if (args.length == 1){
            scanner = new Scanner(new File(args[0]));
        } else {
            System.out.println("Error");
            return;
        }

        Cache[] caches = new Cache[7];
        caches[0] = new Cache(2, 1, 1);
        caches[1] = new Cache(2, 1, 2);
        caches[2] = new Cache(2, 1, 4);
        caches[3] = new Cache(2, 2, 1);
        caches[4] = new Cache(2, 4, 1);
        caches[5] = new Cache(2, 4, 4);
        caches[6] = new Cache(4, 1, 1);

        // int count = 0;            
        while (scanner.hasNextLine()  /* && count++ < 40 */ ){
            String line = scanner.nextLine();
            int address = Integer.parseInt(line.trim().split("\\s+")[1].trim(), 16);
            // System.out.println(line.trim() + ": " + address);

            // caches[5].analyzeAddress(address);

            for (Cache c : caches){
                c.analyzeAddress(address);
            }
        }

        for(int i = 0; i < caches.length ; i++){
            System.out.printf("Cache #%d\n", i + 1);
            System.out.printf("Cache size: %dB\tAssociativity: %d\tBlock size: %d\n",
                                caches[i].getStorage() * 1024,
                                caches[i].getAssociativity(),
                                caches[i].getBlockSize()
                            );
            System.out.printf("Hits: %d\tHit Rate: %.2f%%\n",
                                caches[i].getNumHits(),
                                ((double) caches[i].getNumHits() / caches[i].getTotalTries() * 100)
                            );

            // System.out.printf("numLines: %d\tindexBitLength: %d\tblockOffSetLength: %d\n",
            //                     caches[i].getNumLines(), 
            //                     caches[i].getIndexBitLength(), 
            //                     caches[i].getBlockOffSetLength()
            //                 );

            System.out.print("---------------------------\n");
        }

        scanner.close();
    }
}



