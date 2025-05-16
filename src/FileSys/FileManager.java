package FileSys;

public class FileManager {
    private final Directory directory;
    private final BlockManager blockManager;
    private final Descriptor[] descriptors = new Descriptor[Settings.MAX_DESCRIPTORS];
    private final Descriptor[] openFiles = new Descriptor[Settings.MAX_DESCRIPTORS];

    public FileManager() {
        this.directory = new Directory();
        this.blockManager = new BlockManager();
    }

    public void createFile(String name) {
        for (int i = 0; i < Settings.MAX_DESCRIPTORS; i++) {
            if (descriptors[i] == null) {
                descriptors[i] = new Descriptor(false);
                descriptors[i].increaseLinksAmount();
                directory.addEntry(name, i);
                System.out.println("File '" + name + "'" + " created");
                return;
            }
        }
        System.out.println("Failed to create file '" + name + "'");
    }

    public void ls() {
        for (DirectoryEntry entry : directory.getEntries()) {
            System.out.println("File name: " + entry.fileName() + " | Descriptor id: " + entry.descriptorId());
        }
    }

    public void link(String originalName, String newName) {
        if (originalName.equals(newName)) {
            System.out.println("New link should have different name");
            return;
        }
        int descriptorId = directory.findDescriptorId(originalName);
        if (descriptorId == -1) {
            System.out.println("Could not find " + "'" + originalName + "'");
            return;
        }
        directory.addEntry(newName, descriptorId);
        descriptors[descriptorId].increaseLinksAmount();
        System.out.println("'" + newName + "'" + " linked to " + "'" + originalName + "'");
    }

    public void unlink(String name) {
        int descriptorId = directory.findDescriptorId(name);
        if (descriptorId == -1) {
            System.out.println("Could not find " + "'" + name + "'");
            return;
        }
        Descriptor currentFileDescriptor = descriptors[descriptorId];
        if (currentFileDescriptor.getLinkAm() == 1) {
            blockManager.freeBlocks(currentFileDescriptor, currentFileDescriptor.getBlockMap().size());
            descriptors[descriptorId] = null;
            System.out.println("File '" + name + "' was fully removed.");
        } else {
            System.out.println("File '" + name + "' unlinked, but still has " + (currentFileDescriptor.getLinkAm() - 1) + " links.");
        }
        currentFileDescriptor.decreaseLinksAmount();
        int entryIndex = directory.findEntryIndex(name);
        if (entryIndex != -1) {
            directory.getEntries().remove(entryIndex);
        }
    }

    public void truncate(String name, int size) {
        int descriptorId = directory.findDescriptorId(name);
        if (descriptorId == -1) {
            System.out.println("Could not find '" + name + "'");
            return;
        }
        Descriptor currentFileDescriptor = descriptors[descriptorId];
        int currentSize = currentFileDescriptor.getFileSize();
        if (currentSize == size) {
            System.out.println("File '" + name + "' is already the size of " + size);
            return;
        }
        int currentBlocks = currentFileDescriptor.getBlockMap().size();
        int neededBlocks = blockManager.calculateBlockNeeded(size);
        int differenceBlocks = Math.abs(currentBlocks - neededBlocks);

        System.out.println("Blocks needed: " + neededBlocks);
        if (currentSize > size) { // Lowering fileSize
            if (currentBlocks > neededBlocks) {
                blockManager.freeBlocks(currentFileDescriptor, differenceBlocks);
            }
            currentFileDescriptor.setFileSize(size);
        } else { // Increasing fileSize
            if (currentBlocks < neededBlocks) {
                blockManager.allocateBlocks(currentFileDescriptor, differenceBlocks);
            }
            currentFileDescriptor.setFileSize(size);
            blockManager.fillWithZeroes(currentFileDescriptor);
        }
    }

    public void stat(String name) {
        int descriptorId = directory.findDescriptorId(name);
        if (descriptorId == -1) {
            System.out.println("Could not find '" + name + "'");
            return;
        }
        Descriptor currentFileDescriptor = descriptors[descriptorId];
        System.out.println("File type: " + currentFileDescriptor.getFileType());
        System.out.println("File size: " + currentFileDescriptor.getFileSize());
        System.out.println("Offset: " + currentFileDescriptor.getOffset());
        System.out.println("Blocks in use: " + currentFileDescriptor.getBlockMap());
        System.out.println("Links amount: " + currentFileDescriptor.getLinkAm());
    }

    public void seek(int fd, int offset) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find open file with fd = " + fd);
            return;
        }
        openFiles[fd].setOffset(offset);
        System.out.println("Offset set to: " + openFiles[fd].getOffset() + " for file with fd = " + fd);
    }

    public void open(String name) {
        int descriptorId = directory.findDescriptorId(name);
        if (descriptorId == -1) {
            System.out.println("Could not find " + "'" + name + "'");
            return;
        }
        Descriptor currentFileDescriptor = descriptors[descriptorId];
        for (int i = 0; i < Settings.MAX_DESCRIPTORS; i++) {
            if (openFiles[i] == null) {
                openFiles[i] = currentFileDescriptor;
                System.out.println("File '" + name + "' opened with fd = " + i);
                return;
            }
        }
        System.out.println("Failed to open file '" + name + "'");
    }

    public void close(int fd) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find  file with fd = " + fd);
            return;
        }
        openFiles[fd] = null;
        System.out.println("Closed file with fd = " + fd);
    }

    public void read(int fd, int size) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find  file with fd = " + fd);
            return;
        }
        Descriptor currentFileDescriptor = openFiles[fd];
        blockManager.read(currentFileDescriptor, size);
    }

    public void write(int fd, String data) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find  file with fd = " + fd);
            return;
        }
        Descriptor currentFileDescriptor = openFiles[fd];
        blockManager.write(currentFileDescriptor, data);
    }
}
