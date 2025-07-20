package sb.tasks.job.torrents.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class Bencode {

    private static final String EOF = "Unexpected EOF found";

    public Object parse(InputStream is) throws IOException {
        if (!is.markSupported())
            throw new IOException("is.markSupported should be true");
        is.mark(0);
        int readChar = is.read();
        switch (readChar) {
            case 'i':
                return parseInteger(is);
            case 'l':
                return parseList(is);
            case 'd':
                return parseDictionary(is);
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
                is.reset();
                return parseString(is);
            default:
                throw new IOException("Problem parsing bencoded file");
        }
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

    private SortedMap<String, Object> parseDictionary(InputStream is) throws IOException {
        SortedMap<String, Object> map = new TreeMap<>();
        is.mark(0);
        int readChar = is.read();
        while (readChar != 'e') {
            if (readChar < 0)
                throw new IOException(EOF);
            is.reset();
            map.put(parseString(is), parse(is));
            is.mark(0);
            readChar = is.read();
        }
        return map;
    }

    private String parseString(InputStream is) throws IOException {
        int readChar = is.read();
        StringBuilder buff = new StringBuilder();
        do {
            if (readChar < 0)
                throw new IOException(EOF);
            buff.append((char) readChar);
            readChar = is.read();
        } while (readChar != ':');
        byte[] byteString = new byte[Integer.parseInt(buff.toString())];
        for (int i = 0; i < byteString.length; i++)
            byteString[i] = (byte) is.read();
        return new String(byteString);
    }
}
