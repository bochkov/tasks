package sb.tasks.jobs.trupd.metafile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Bencode {

    private static final String EOF = "Unexpected EOF found";

    private final SortedMap rootElement;

    public Bencode(InputStream is) throws IOException {
        if (!is.markSupported())
            throw new IOException("is.markSupported should be true");
        rootElement = (SortedMap) parse(is);
    }

    private Object parse(InputStream is) throws IOException {
        is.mark(0);
        int readChar = is.read();
        switch (readChar) {
            case 'i':
                return parseInteger(is);
            case 'l':
                return parseList(is);
            case 'd':
                return parseDictionary(is);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                is.reset();
                return parseByteString(is);
            default:
                throw new IOException("Problem parsing bencoded file");
        }
    }

    public ByteBuffer key(String keyStr) {
        return ByteBuffer.wrap(keyStr.getBytes());
    }

    public SortedMap rootElement() {
        return rootElement;
    }

    public SortedMap info() {
        return (SortedMap) rootElement.get(key("info"));
    }

    private Long parseInteger(InputStream is) throws IOException {
        int readChar = is.read();
        StringBuilder buff = new StringBuilder();
        do {
            if (readChar < 0)
                throw new IOException(EOF);
            buff.append((char) readChar);
            readChar = is.read();
        } while (readChar != 'e');
        return Long.parseLong(buff.toString());
    }

    private List<Object> parseList(InputStream is) throws IOException {
        List<Object> list = new LinkedList<>();
        is.mark(0);
        int readChar = is.read();
        while (readChar != 'e') {
            if (readChar < 0)
                throw new IOException(EOF);
            is.reset();
            list.add(parse(is));
            is.mark(0);
            readChar = is.read();
        }
        return list;
    }

    private SortedMap<ByteBuffer, Object> parseDictionary(InputStream is) throws IOException {
        SortedMap<ByteBuffer, Object> map = new TreeMap<>(new DictionaryComparator());
        is.mark(0);
        int readChar = is.read();
        while (readChar != 'e') {
            if (readChar < 0)
                throw new IOException(EOF);
            is.reset();
            map.put(parseByteString(is), parse(is));
            is.mark(0);
            readChar = is.read();
        }
        return map;
    }

    private ByteBuffer parseByteString(InputStream is) throws IOException {
        int readChar = is.read();
        StringBuilder buff = new StringBuilder();
        do {
            if (readChar < 0)
                throw new IOException(EOF);
            buff.append((char) readChar);
            readChar = is.read();
        } while (readChar != ':');
        int length = Integer.parseInt(buff.toString());
        byte[] byteString = new byte[length];
        for (int i = 0; i < byteString.length; i++)
            byteString[i] = (byte) is.read();
        return ByteBuffer.wrap(byteString);
    }
}
