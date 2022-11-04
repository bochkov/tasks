package sb.tasks.util;

import java.nio.ByteBuffer;
import java.util.Comparator;

public final class DictionaryComparator implements Comparator<ByteBuffer> {

    public int bitCompare(byte b1, byte b2) {
        return (b1 & 0xFF) - (b2 & 0xFF);
    }

    public int compare(ByteBuffer o1, ByteBuffer o2) {
        byte[] byteString1 = o1.array();
        byte[] byteString2 = o2.array();
        int minLength = Math.min(byteString1.length, byteString2.length);
        for (var i = 0; i < minLength; i++) {
            int bitCompare = bitCompare(byteString1[i], byteString2[i]);
            if (bitCompare != 0)
                return bitCompare;
        }

        if (byteString1.length > byteString2.length)
            return 1;
        else if (byteString1.length < byteString2.length)
            return -1;
        return 0;
    }
}
