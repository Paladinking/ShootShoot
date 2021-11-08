package json;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FancyJSonParser {

    private String name;

    public JsonObject readFile(String fileName) throws IOException {
        this.name = fileName;
        BufferedReader reader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
        char in;
        do {
            in = (char) reader.read();
        } while (in != '{');
        return readObject(reader);
    }

    public JsonObject readResource(String path) throws IOException {
        return readFile(ClassLoader.getSystemResource(path).getFile());
    }

    private String readString(Reader reader) throws IOException {
        char in;
        StringBuilder s = new StringBuilder();
        while (true) {
            in = (char) reader.read();
            switch (in) {
                case '\n':
                case '\r':
                case (char) -1:
                    throw new IOException();
                case '\\':
                    in = (char) reader.read();
                    switch (in) {
                        case 'b' -> s.append('\b');
                        case 't' -> s.append('\t');
                        case 'f' -> s.append('\f');
                        case 'n' -> s.append('\n');
                        case '"' -> s.append('"');
                        case 'r' -> s.append('\r');
                        case '\\' -> s.append('\\');
                        case 'u' -> {
                            int i = Integer.valueOf(String.valueOf((char) reader.read()) + (char) reader.read() + (char) reader.read() + (char) reader.read(), 16);
                            s.append((char) i);
                        }
                        default -> {
                            System.out.println(s);
                            throw new IOException();
                        }
                    }
                    break;
                case '"':
                    return s.toString();
                default:
                    s.append(in);
            }

        }

    }

    private JsonObject readObject(Reader reader) throws IOException {
        char in;
        JsonObject jsonObject = new JsonObject();
        loop:
        while (true) {
            in = (char) reader.read();
            switch (in) {
                case '\"':
                    String label = readString(reader);
                    Object object;
                    while (in != ':') in = (char) reader.read();
                    innerLoop:
                    while (true) {
                        in = (char) reader.read();
                        switch (in) {
                            case '[':
                                object = readList(reader);
                                break innerLoop;
                            case '{':
                                object = readObject(reader);
                                break innerLoop;
                            case '"':
                                object = readString(reader);
                                break innerLoop;
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
                            case '-':
                                object = readNumber(reader, in);
                                break innerLoop;
                            case 't':
                                object = readBoolean(reader, true);
                                break innerLoop;
                            case 'f':
                                object = readBoolean(reader, false);
                                break innerLoop;
                            case ' ':
                            case '\n':
                            case '\r':
                            case '\t':
                            case ',':
                                break;
                            default:
                                throw new IOException(name);
                        }
                    }
                    jsonObject.put(label, object);
                    break;
                case '}':
                case (char) -1:
                    break loop;
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case ',':
                    break;
                default:
                    throw new IOException(name);
            }
        }
        return jsonObject;
    }

    private JsonList readList(Reader reader) throws IOException {
        char in;
        JsonList list = new JsonList();
        loop:
        while (true) {
            in = (char) reader.read();
            switch (in) {
                case '[':
                    list.add(readList(reader));
                case '{':
                    list.add(readObject(reader));
                    break;
                case '"':
                    list.add(readString(reader));
                    break;
                case ']':
                    break loop;
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
                case '-':
                    list.add(readNumber(reader, in));
                    break;
                case 't':
                    list.add(readBoolean(reader, true));
                    break;
                case 'f':
                    list.add(readBoolean(reader, false));
                    break;
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case ',':
                    break;
                default:
                    throw new IOException(name);
            }
        }
        return list;
    }

    private Boolean readBoolean(Reader reader, boolean value) throws IOException {
        char[] chars = value ? new char[]{'r', 'u', 'e'} : new char[]{'a', 'l', 's', 'e'};
        for (char c : chars) {
            char in = (char) reader.read();
            if (in != c) throw new IOException("Bad boolean: " + name);
        }
        char in = (char) reader.read();
        switch (in) {
            case ' ':
            case '\n':
            case '\t':
            case '\r':
            case ',':
                return value;
            default:
                throw new IOException("Bad boolean: " + name);
        }
    }


    private Number readNumber(Reader reader, char in) throws IOException {
        StringBuilder builder = new StringBuilder().append(in);
        boolean integer = true;
        while (true) {
            in = (char) reader.read();
            switch (in) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                case ',':
                case '}':
                    if (integer) return Integer.valueOf(builder.toString());
                    else return Double.valueOf(builder.toString());
                case '.':
                    if (integer) integer = false;
                    else throw new IOException(name);
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
                case '-':
                    builder.append(in);
                    break;
                default:
                case (char) -1:
                    throw new IOException(name);
            }
        }
    }


}
