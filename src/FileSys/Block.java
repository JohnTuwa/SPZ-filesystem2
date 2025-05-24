package FileSys;

public class Block {
    private final byte[] data = new byte[Settings.BLOCK_SIZE];

    public void write(int offset, byte value) {
        if (offset >= 0 && offset < data.length) {
            data[offset] = value;
        }
    }

    public char read(int offset) {
        if (offset >= 0 && offset < data.length) {
            return (char) data[offset];
        }
        return '\0';
    }
}
