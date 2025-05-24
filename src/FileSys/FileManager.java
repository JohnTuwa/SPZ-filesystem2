package FileSys;

import java.util.*;

public class FileManager {
    private final Descriptor rootDescriptor;
    private final BlockManager blockManager;
    private final Descriptor[] descriptors = new Descriptor[Settings.MAX_DESCRIPTORS];
    private final Descriptor[] openFiles = new Descriptor[Settings.MAX_DESCRIPTORS];
    private final List<Descriptor> symlinks = new ArrayList<>();
    private Descriptor cwd;

    public FileManager() {
        this.blockManager = new BlockManager();
        this.descriptors[0] = new Descriptor(0, null, 1);
        this.rootDescriptor = descriptors[0];
        this.cwd = rootDescriptor;
        truncate("/", Settings.DIR_DEFAULT_SIZE);
    }

    public String getPathString() throws WrongTypeException, PathResolutionException {
        if (cwd == rootDescriptor) {
            return "/";
        }
        StringBuilder result = new StringBuilder();
        String buffer;
        Descriptor currentDescriptor = this.cwd;
        do {
            buffer = blockManager.getDirName(currentDescriptor);
            currentDescriptor = currentDescriptor.getParent();
            result.insert(0, "/" + buffer);
        } while (currentDescriptor.getId() != 0);
        return result.toString();
    }

    public void createFile(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            for (int i = 0; i < Settings.MAX_DESCRIPTORS; i++) {
                if (descriptors[i] == null) {
                    descriptors[i] = new Descriptor(i, parent, 0);
                    descriptors[i].increaseLinksAmount();
                    blockManager.addEntry(parent, name, i);
                    System.out.println("File '" + name + "'" + " created");
                    return;
                }
            }
            System.out.println("Failed to create file '" + name + "'");
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ls() {
        List<String[]> result = blockManager.getEntries(cwd);
        for (String[] entry : result) {
            System.out.println("File name: " + entry[0] + " | Descriptor id: " + entry[1] + " | " + descriptors[Integer.parseInt(entry[1])].getFileType());
        }
    }

    public void link(String path, String newName) {
        try {
            PathTarget result = resolvePath(path);
            String originalName = result.name();
            Descriptor parent = result.parent();

            if (originalName.equals(newName)) {
                System.out.println("New link should have different name");
                return;
            }
            int descriptorId = blockManager.findDescriptorId(parent, originalName);
            if (descriptorId == -1) {
                System.out.println("Could not find " + "'" + originalName + "'");
                return;
            }
            if (Objects.equals(descriptors[descriptorId].getFileType(), "directory")) {
                System.out.println("File: '" + originalName + "' is a directory");
                return;
            }
            blockManager.addEntry(cwd, newName, descriptorId);
            descriptors[descriptorId].increaseLinksAmount();
            System.out.println("'" + newName + "'" + " linked to " + "'" + originalName + "'");
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void unlink(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = blockManager.findDescriptorId(parent, name);
            if (descriptorId == -1) {
                System.out.println("Could not find " + "'" + name + "'");
                return;
            }
            if (Objects.equals(descriptors[descriptorId].getFileType(), "directory")) {
                System.out.println("File: '" + name + "' is a directory");
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
            if (Arrays.asList(openFiles).contains(currentFileDescriptor)) {
                openFiles[descriptorId] = null;
            }

            currentFileDescriptor.decreaseLinksAmount();
            blockManager.deleteEntry(parent, name);
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void truncate(String path, int size) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = 0;
            if (!name.equals("/")) {
                descriptorId = blockManager.findDescriptorId(parent, name);
            }
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
                currentFileDescriptor.setOffset(0);
            } else { // Increasing fileSize
                if (currentBlocks < neededBlocks) {
                    blockManager.allocateBlocks(currentFileDescriptor, differenceBlocks);
                }
                currentFileDescriptor.setFileSize(size);
                blockManager.fillWithZeroes(currentFileDescriptor);
            }
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stat(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = blockManager.findDescriptorId(parent, name);
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
            System.out.println("Id: " + currentFileDescriptor.getId());
            System.out.println("Parent: " + currentFileDescriptor.getParent().getId());
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void seek(int fd, int offset) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find open file with fd = " + fd);
            return;
        }
        openFiles[fd].setOffset(offset);
        System.out.println("Offset set to: " + openFiles[fd].getOffset() + " for file with fd = " + fd);
    }

    public void open(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = blockManager.findDescriptorId(parent, name);
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
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close(int fd) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find file with fd = " + fd);
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
        System.out.println(blockManager.read(currentFileDescriptor, size, true));
    }

    public void write(int fd, String data) {
        if (openFiles[fd] == null) {
            System.out.println("Could not find  file with fd = " + fd);
            return;
        }
        Descriptor currentFileDescriptor = openFiles[fd];
        blockManager.write(currentFileDescriptor, data);
    }

    private final int max_links = 2;
    private int depth = 0;

    public String resolveSymlinks(String path) {
        if (depth > max_links) {
            return null;
        }
        String[] buffer = path.split("/");
        int cwId = 0;

        //If given path is local check if first file on path exists and is a symlink
        if (!path.startsWith("/")) {
            cwId = blockManager.findDescriptorId(cwd, buffer[0]);

            //In this method if findDescriptor returns -1 it is likely that user is trying to create file so it won`t exist of course
            //since we handle this situations in resolvePath we can just return path back
            if (cwId == -1) {
                return path;
            }
            if (symlinks.contains(descriptors[cwId])) {
                path = path.replace(buffer[0], blockManager.readDirectory(descriptors[cwId]));
                depth++;
                return resolveSymlinks(path);
            }
        }

        //Check other elements in path if there are any symlinks
        for (int i = 1; i < buffer.length-1; i++) {
            cwId = blockManager.findDescriptorId(descriptors[cwId], buffer[i]);
            if (symlinks.contains(descriptors[cwId])) {
                path = path.replace(buffer[i], blockManager.readDirectory(descriptors[cwId]));
                depth++;
                return resolveSymlinks(path);
            }
        }
        return path;
    }

    public PathTarget resolvePath(String path) throws PathResolutionException {
        depth = 0;

        path = resolveSymlinks(path);

        if (path == null) {
            throw new PathResolutionException("Too many symbolic links");
        }

        //I am a bit lazy so to allocate memory for root directory I use truncate,
        //since truncate is checking for name == / we just pass it, and we don`t care about descriptor
        if (Objects.equals(path, "/")) {
            return new PathTarget(null, "/");
        }

        String[] buffer = path.split("/");

        //If path is just a name of a file we skip everything and return name
        if (buffer.length == 1) {
            return new PathTarget(cwd, buffer[0]);
        }

        int cwId = 0; //descriptorId that will contain next file on path

        //If given path is local check if first file on path exists and is a directory
        if (!Objects.equals(buffer[0], "")) {
            cwId = blockManager.findDescriptorId(cwd, buffer[0]);
            if (cwId == -1) {
                throw new PathResolutionException("Could not find '" + buffer[0] + "'");
            }
            if (Objects.equals(descriptors[cwId].getFileType(), "file")) {
                throw new PathResolutionException("File '" + buffer[0] + "' is not a directory");
            }
        }

        //Check if other files on path exist and are directories
        for (int i = 1; i < buffer.length - 1; i++) {
            cwId = blockManager.findDescriptorId(descriptors[cwId], buffer[i]);
            if (cwId == -1) {
                throw new PathResolutionException("Could not find '" + buffer[i] + "'");
            }
            if (Objects.equals(descriptors[cwId].getFileType(), "file")) {
                throw new PathResolutionException("File '" + buffer[i] + "' is not a directory");
            }
        }

        return new PathTarget(descriptors[cwId], buffer[buffer.length - 1]);
    }

    public void mkdir(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            for (int i = 0; i < Settings.MAX_DESCRIPTORS; i++) {
                if (descriptors[i] == null) {
                    descriptors[i] = new Descriptor(i, parent, 1);
                    descriptors[i].increaseLinksAmount();
                    blockManager.addEntry(parent, name, i);

                    // Allocating memory to newly created directory and adding essential links
                    truncate(path, Settings.DIR_DEFAULT_SIZE);
                    blockManager.addEntry(descriptors[i], ".", i);
                    blockManager.addEntry(descriptors[i], "..", parent.getId());

                    System.out.println("Directory '" + name + "'" + " created");
                    return;
                }
            }
            System.out.println("Failed to create directory '" + name + "'");
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void rmdir(String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = blockManager.findDescriptorId(parent, name);
            if (descriptorId == -1) {
                System.out.println("Could not find " + "'" + name + "'");
                return;
            }
            if (!Objects.equals(descriptors[descriptorId].getFileType(), "directory")) {
                System.out.println("File '" + name + "' is not a directory");
                return;
            }
            Descriptor currentFileDescriptor = descriptors[descriptorId];
            if (currentFileDescriptor.getLinkAm() == 1) {
                blockManager.freeBlocks(currentFileDescriptor, currentFileDescriptor.getBlockMap().size());
                descriptors[descriptorId] = null;
                System.out.println("File '" + name + "' was fully removed.");
            }

            currentFileDescriptor.decreaseLinksAmount();
            blockManager.deleteEntry(parent, name);
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cd(String path) {
        try {
            if (Objects.equals(path, "/")) {
                cwd = rootDescriptor;
                return;
            }

            if (Objects.equals(path, ".")) {
                return;
            }

            if (Objects.equals(path, "..")) {
                cwd = cwd.getParent();
                return;
            }

            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();

            int descriptorId = blockManager.findDescriptorId(parent, name);
            if (descriptorId == -1) {
                System.out.println("Could not find " + "'" + name + "'");
                return;
            }
            if (!Objects.equals(descriptors[descriptorId].getFileType(), "directory")) {
                System.out.println("File '" + name + "' is not a directory");
                return;
            }

            cwd = descriptors[descriptorId];
        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createSymLink(String link, String path) {
        try {
            PathTarget result = resolvePath(path);
            String name = result.name();
            Descriptor parent = result.parent();
            if (link.length() > 32) {
                System.out.println("Link is too long");
                return;
            }

            for (int i = 0; i < Settings.MAX_DESCRIPTORS; i++) {
                if (descriptors[i] == null) {
                    descriptors[i] = new Descriptor(i, parent, 2);
                    descriptors[i].increaseLinksAmount();
                    blockManager.addEntry(parent, name, i);

                    // Allocating memory to newly created link and adding to lin
                    truncate(path, 32);
                    blockManager.write(descriptors[i], link);
                    descriptors[i].setOffset(link.length());
                    symlinks.add(descriptors[i]);
                    System.out.println("Link '" + name + "' created successfully");
                    return;
                }
            }

        } catch (PathResolutionException e) {
            System.out.println(e.getMessage());
        }

    }
}
