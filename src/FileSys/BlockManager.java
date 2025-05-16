package FileSys;

import java.util.List;

public class BlockManager {
    private final Block[] blocks = new Block[Settings.MAX_BLOCKS];

    public int calculateBlockNeeded(int size){
        return (size + Settings.BLOCK_SIZE - 1) / Settings.BLOCK_SIZE;
    }

    public void freeBlocks(Descriptor descriptor, int difference){
        int c = 0;
        List<Integer> blockMap = descriptor.getBlockMap();
        System.out.println("difference: " + difference);
        System.out.println("blockMap: " + blockMap);
        for (int i = 0; i < difference; i++){
            int deletedElement = blockMap.removeLast();
            System.out.println("Freed block " + deletedElement);
            blocks[deletedElement] = null;
            c++;
        }
        System.out.println("Freed " + c + " blocks");
        System.out.println("blockMap: " + blockMap);
    }

    public void allocateBlocks(Descriptor descriptor, int difference){
        System.out.println("Allocating " + difference + " blocks");
        List<Integer> blockMap = descriptor.getBlockMap();
        int c = 0;
        for (int i = 0; i < Settings.MAX_BLOCKS && c < difference; i++) {
            if (blocks[i] == null) {
                blocks[i] = new Block();
                blockMap.add(i);
                System.out.println("Block " + i + " allocated");
                c++;
            }
        }
        if (c == 0){
            System.out.println("Failed to allocate blocks");
        } else {
            System.out.println("Allocated " + c + " blocks");
            System.out.println(blockMap);
        }
    }

    public void fillWithZeroes(Descriptor descriptor) {
        int from = descriptor.getOffset();
        int to = descriptor.getFileSize();
        if (from >= to) return;
        int c = 0;

        List<Integer> blocksMap = descriptor.getBlockMap();
        for (int i = from; i < to; i++) {
            int blockIndex = i / Settings.BLOCK_SIZE;
            int offsetInBlock = i % Settings.BLOCK_SIZE;
            int blockId = blocksMap.get(blockIndex);
            Block block = blocks[blockId];
            block.write(offsetInBlock, (byte) 0);
            c++;
        }
        System.out.println("Wrote " + c + " zeroes");
    }

    public void read(Descriptor descriptor, int size) {
        int from = descriptor.getOffset();
        int to = from + size;
        if (to >= descriptor.getFileSize()) {
            to = descriptor.getFileSize();
        }
        List<Integer> blocksMap = descriptor.getBlockMap();
        System.out.println("Reading bytes from " + from + " to " + to);
        for (int i = from; i < to; i++) {
            int blockIndex = i / Settings.BLOCK_SIZE;
            int offsetInBlock = i % Settings.BLOCK_SIZE;
            int blockId = blocksMap.get(blockIndex);
            Block block = blocks[blockId];
            block.read(offsetInBlock);
        }
        System.out.println();
    }

    public void write(Descriptor descriptor, String data) {
        int from = descriptor.getOffset();
        int to = from + data.length();
        if (to >= descriptor.getFileSize()) {
            System.out.println("Data exceeds file size. Aborting...");
            return;
        }
        descriptor.setOffset(to);
        List<Integer> blockMap = descriptor.getBlockMap();
        int c = 0;
        for (int i = from; i < to; i++) {
            int blockIndex = i / Settings.BLOCK_SIZE;
            int offsetInBlock = i % Settings.BLOCK_SIZE;
            int blockId = blockMap.get(blockIndex);
            Block block = blocks[blockId];
            block.write(offsetInBlock, (byte) data.charAt(c++));
        }
    }
}
