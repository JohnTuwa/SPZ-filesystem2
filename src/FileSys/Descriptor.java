package FileSys;

import java.util.ArrayList;
import java.util.List;

public class Descriptor {
    private final int id;
    private final int fileType;
    private int linkAm = 0;
    private int fileSize = 0;
    private int offset = 0;
    private Descriptor parent;
    private List<Integer> blockMap = new ArrayList<>();

    public Descriptor(int id, Descriptor parent, int fileType) {
        this.id = id;
        this.parent = parent;
        // false - file, true - directory
        this.fileType = fileType;
    }

    public int getId() {
        return id;
    }

    public Descriptor getParent() {
        return parent;
    }

    public String getFileType() {
        return switch (this.fileType) {
            case 0 -> "file";
            case 1 -> "directory";
            case 2 -> "symLink";
            default -> "";
        };
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        if (offset > fileSize) {
            System.out.println("Offset out of bounds\n" +
                    "Requested offset: " + offset +"\n" +
                    "Space available: " + fileSize);
            return;
        }
        this.offset = offset;
    }

    public void increaseLinksAmount(){
        linkAm++;
    }

    public void decreaseLinksAmount(){
        if(linkAm > 0){
            linkAm--;
        }
    }

    public int getLinkAm() {
        return linkAm;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public List<Integer> getBlockMap() {
        return blockMap;
    }
}
