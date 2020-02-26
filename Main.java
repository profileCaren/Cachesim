package com.company;

import java.util.ArrayList;

class Block{
    boolean isValid;
    String tag;
    int lruBits;

    public Block(boolean isValid, String tag, int lruBits) {
        this.isValid = isValid;
        this.tag = tag;
        this.lruBits = lruBits;
    }
}
public class Main {

    public static void main(String[] args) {
        // 1. read arguments (FILE_NAME, TOTAL_CACHE_SIZE, CACHE_BLOCK_SIZE, NUMBER_OF_WAYS)
        if(args.length < 4){
            System.out.println("Usage: java Branchscim [FILE_NAME] [M_HISTORY_BIT] [N_BIT_PREDICTOR] [BITS_TO_INDEX]");
            System.out.println("FILE_NAME ∈ {gcc-1M.memtrace, gcc-10K.memtrace}");
            System.out.println("TOTAL_CACHE_SIZE <= 4MB >");
            System.out.println("CACHE_BLOCK_SIZE ∈ { }");
            System.out.println("NUMBER_OF_WAYS ∈ { }");
            return;
        }

        // 1. get arguments from terminal
        String fileName = args[0];
        int cacheSize = Integer.parseInt(args[1]); // e.g., 512000 (512 kB)
        int blockSize = Integer.parseInt(args[2]); // e.g., 16 (16B）
        int numberOfWays = Integer.parseInt(args[3]); // e.g., 4

        // 2. compute some necessary value
        int blockNum = cacheSize / blockSize; // e.g., 32000
        int setNum = blockNum / numberOfWays; // e.g., 8000
        int bitsToIndex = getNumberOfBitsToOffset(setNum); // e.g., 13 (log_2 setNum)
        int bitsToOffset = getNumberOfBitsToOffset(blockSize); // e.g., 4 (log_2 blockSize)

        // 3. init the cache
        ArrayList<ArrayList<Block>> cache = new ArrayList<>();


    }

    private static int getNumberOfBitsToIndex(int setNum){
        int bitsToIndex;
        for(bitsToIndex = 0; bitsToIndex  < 32; bitsToIndex++){
            if(Math.pow(2, bitsToIndex) >= setNum){
                break;
            }
        }
        return bitsToIndex;
    }
    private static int getNumberOfBitsToOffset(int blockSize){
        int bitsToOffset;
        for(bitsToOffset = 0; bitsToOffset  < 32; bitsToOffset++){
            if(Math.pow(2, bitsToOffset) >= blockSize){
                break;
            }
        }
        return bitsToOffset;
    }

}
