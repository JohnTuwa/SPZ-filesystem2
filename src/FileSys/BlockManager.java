package FileSys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String read(Descriptor descriptor, int size, boolean logging) {
        int from = descriptor.getOffset();
        int to = from + size;
        if (to >= descriptor.getFileSize()) {
            to = descriptor.getFileSize();
        }
        List<Integer> blocksMap = descriptor.getBlockMap();

        if (logging) {
            System.out.println("Reading bytes from " + from + " to " + to);
        }

        StringBuilder buffer = new StringBuilder();
        for (int i = from; i < to; i++) {
            int blockIndex = i / Settings.BLOCK_SIZE;
            int offsetInBlock = i % Settings.BLOCK_SIZE;
            int blockId = blocksMap.get(blockIndex);
            Block block = blocks[blockId];
            buffer.append(block.read(offsetInBlock));
        }
        return buffer.toString();
    }

    public String read(Descriptor descriptor, int size) {
        return read(descriptor, size, false);
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

    public void addEntry(Descriptor descriptorCWD, String name, int descriptorId) {
        String entry = name + "@" + descriptorId + "/";
        write(descriptorCWD, entry);
    }

    public String readDirectory(Descriptor descriptorCWD) {
        int offset = descriptorCWD.getOffset();
        descriptorCWD.setOffset(0);
        String contents = read(descriptorCWD, offset);
        descriptorCWD.setOffset(offset);
        return contents;
    }

    public List<String[]> getEntries(Descriptor descriptorCWD) {
        String contents = readDirectory(descriptorCWD);
        String[] entries = contents.split("/");
        List<String[]> result = new ArrayList<>();
        for (String entry : entries) {
            result.add(entry.split("@"));
        }
        return result;
    }

    public int findDescriptorId(Descriptor descriptorCWD, String name) {
        String contents = readDirectory(descriptorCWD);
        String[] entries = contents.split("/");
        for (String entry : entries) {
            String[] buffer = entry.split("@");
            if (buffer[0].equals(name)) {
                return Integer.parseInt(buffer[1]);
            }
        }
        return -1;
    }

    public void deleteEntry(Descriptor descriptorCWD, String name) {
        String contents = readDirectory(descriptorCWD);
        int descriptorId = findDescriptorId(descriptorCWD, name);
        if (descriptorId == -1) {
            System.out.println("File '" + name + "' not found");
            return;
        }
        String entryToRemove = name + "@" + descriptorId + "/";
        String updatedContents = contents.replace(entryToRemove, "");
        descriptorCWD.setOffset(0);
        write(descriptorCWD, updatedContents);
    }

    public String getDirName(Descriptor descriptorCWD) throws WrongTypeException, PathResolutionException {
        if (!Objects.equals(descriptorCWD.getFileType(), "directory")) {
            throw new WrongTypeException("Not a directory");
        }
        String contents = readDirectory(descriptorCWD.getParent());
        String[] entries = contents.split("/");
        for (String entry : entries) {
            String[] buffer = entry.split("@");
            if (Integer.parseInt(buffer[1]) == descriptorCWD.getId()) {
                return buffer[0];
            }
        }
        throw new PathResolutionException("Directory not found");
    }
}
