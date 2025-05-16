package FileSys;


import java.util.ArrayList;
import java.util.List;

public class Directory {
    private final List<DirectoryEntry> entries = new ArrayList<>();

    public List<DirectoryEntry> getEntries() {
        return entries;
    }

    public void addEntry(String name, int descriptorId){
        entries.add(new DirectoryEntry(name, descriptorId));
    }

    public int findDescriptorId(String name){
        for (DirectoryEntry entry : entries) {
            if (entry.fileName().equals(name)) {
                return entry.descriptorId();
            }
        }
        return -1;
    }

    public int findEntryIndex(String name) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).fileName().equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
