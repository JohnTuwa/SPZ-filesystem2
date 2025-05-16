package FileSys;

import java.util.ArrayList;
import java.util.List;

public class Descriptor {
    private boolean fileType;
    private int linkAm = 0;
    private int fileSize = 0;
    private int offset = 0;
    private List<Integer> blockMap = new ArrayList<>();

    public Descriptor(boolean fileType) {
        // false - file, true - directory
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType ? "directory" : "file";
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
