package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Block{
    boolean isValid;
    String tag;

    // will have a pretty bad performance in fully-associative cache,
    // as it takes O(n) to update all the lruBits. the performance of 4-way set associative would be acceptable
    int lruBits;

    public Block(boolean isValid, String tag, int lruBits) {
        this.isValid = isValid;
        this.tag = tag;
        this.lruBits = lruBits;
    }
}

// TODO, use class to beautify the code
class Cache{

}
public class CacheSim {

    public static void main(String[] args) throws IOException {
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
        int cacheSize = Integer.parseInt(args[1]); // e.g., 524288 (512 kB)
        int blockSize = Integer.parseInt(args[2]); // e.g., 16 (16B）
        int numberOfWays = Integer.parseInt(args[3]); // e.g., 4

        numberOfWays = numberOfWays == 0 ? (cacheSize / blockSize) : numberOfWays; // for fully associative

        // 2. compute some necessary value
        int blockNum = cacheSize / blockSize; // e.g., 32768
        int setNum = blockNum / numberOfWays; // e.g., 8192
        int bitsToIndex = getNumberOfBitsToIndex(setNum); // e.g., 13 (log_2 setNum)
        int bitsToOffset = getNumberOfBitsToOffset(blockSize); // e.g., 4 (log_2 blockSize)
        int tagLength = 32 - bitsToIndex - bitsToOffset;

        // 3. init the cache
        ArrayList<HashMap<String, Block>> cache = initCache(setNum, numberOfWays);

        String trace;
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        int hitCount = 0;
        int totalTrace = 0;
        int coldMiss = 0;
        while ((trace = br.readLine()) != null) {
            totalTrace ++;

            String[] tokens = trace.split(" ");
            String instrType = tokens[0]; // "L" or "S" (means "load" or "store")
            int address_offset = Integer.parseInt(tokens[1]);  // 100（decimal number)
            String address = truncateTo32Bits(Long.toHexString(Long.parseLong(tokens[2], 16) + address_offset)); // "7fffe7fefe0" (44 bit address, need to be truncated to 32 bits)

            // parse the address and get the index.
            String biAddress = hexToBin(address);
            int index;
            if(numberOfWays == blockNum) { // special case for fully associative
                index = 0;
            }else{
                index = Integer.parseInt(biAddress.substring(biAddress.length() - bitsToOffset - bitsToIndex,
                        biAddress.length() - bitsToOffset) ,2);
            }


            // find the set according to the index.
            HashMap<String, Block> set = cache.get(index);

            // cache finding and replacing
            String tag = biAddress.substring(0, biAddress.length() - bitsToIndex - bitsToOffset);
            increaseLRUBits(set);

            if(set.containsKey(tag)){
                // cache hit
                hitCount ++;

                set.get(tag).lruBits = 0;
            }else if(set.size() < numberOfWays){
                // cache miss (cold miss)
                set.put(tag, new Block(true,  tag, 0));

                coldMiss ++;
            }else{
                // cache miss (capacity miss
                String tagToBeReplaced = findBlockToEvict(set);

                set.remove(tagToBeReplaced);
                set.put(tag, new Block(true, tag, 0));
            }

        }
//        System.out.println(cache);
        System.out.println("Total trace count:\t" + totalTrace);
        System.out.println("Miss rate:\t" +  ((double)(totalTrace - hitCount) / totalTrace));
        System.out.println("Hit Rate: \t"+ (double) hitCount / totalTrace);

        System.out.println("Hit count: \t"+ hitCount);
        System.out.println("cold miss: \t"+ coldMiss);
        System.out.println("total miss:\t"+ (totalTrace - hitCount));

        System.out.println("# of sets: \t" + setNum);
        System.out.println("# of ways: \t" + numberOfWays);
        System.out.println("# of tag_bits: \t" + tagLength);
        System.out.println("# of index_bits: \t" + bitsToIndex);
        System.out.println("# of offset_bits: \t" + bitsToOffset);

    }

    static void increaseLRUBits(HashMap<String, Block> set){
        set.keySet().forEach(key->set.get(key).lruBits ++);
    }

    static String findBlockToEvict(HashMap<String, Block> set){
        // first try to find the invalid block
        for(Map.Entry<String, Block> entry: set.entrySet()){
            if (entry.getValue().isValid == false){
                return entry.getKey();
            }
        }

        // then we try to find the LRU block
        int biggestLRBits = Integer.MIN_VALUE;
        String tagOfLRUBlock = "";
        for(Map.Entry<String, Block> entry: set.entrySet()){
            if(entry.getValue().lruBits > biggestLRBits){
                biggestLRBits = entry.getValue().lruBits;
                tagOfLRUBlock = entry.getKey();
            }
        }
        return tagOfLRUBlock;
    }

    static String hexToBin(String s) {
        return new BigInteger(s, 16).toString(2);
    }

    // given a 44 bit hex address, truncate the most significant 12 bits to make it 32 bits.
    private static String truncateTo32Bits(String address) {
        int diff = 11 - address.length();

        if(diff < 3){
            address = address.substring(3 - diff, address.length());
        }

//        String bit11Address = String.format("%11s", address).replace(' ', '0');
        return address;
    }


    private static ArrayList<HashMap<String, Block>> initCache(int setNum, int numberOfWays) {
        ArrayList<HashMap<String, Block>> cache = new ArrayList<>();
        for(int i = 0; i < setNum; i++){
            HashMap<String, Block> set = new HashMap<>();
            cache.add(set);
        }

        return cache;
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
