package json;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSonParser {


    public JsonObject readFile(String fileName) {
        try (FileInputStream in = new FileInputStream(fileName)) {
            String s = new String(in.readAllBytes());
            return readObject(s);
        } catch (IOException ignored) {
            return null;
        }
    }

    public Object readString(String s) {
        int i = 0;
        while (i < s.length()) {
            switch (s.charAt(i)) {
                case '{':
                    return readObject(s);
                case '[':
                    return readList(s);
                case '"':
                    int quoteStart = s.indexOf("\"") + 1;
                    int quoteEnd = s.indexOf("\"", quoteStart);
                    return s.substring(quoteStart, quoteEnd);
            }
            i++;
        }
        return null;
    }

    public JsonObject readObject(String s) {
        JsonObject jsonObject = new JsonObject();
        s = s.substring(s.indexOf('{') + 1, s.lastIndexOf('}'));
        Iterable<String> content = jsonSplit(s);
        for (String string : content) {
            int quoteStart = string.indexOf("\"") + 1;
            int quoteEnd = string.indexOf("\"", quoteStart);
            String label = string.substring(quoteStart, quoteEnd);
            jsonObject.put(label, readString(string.substring(string.indexOf(":", quoteEnd) + 1)));
        }
        return jsonObject;
    }

    public Iterable<String> jsonSplit(String jsonString){
        List<String> content = new ArrayList<>();
        int index = 0, lastIndex = 0;
        while (index < jsonString.length()) {
            if (jsonString.charAt(index) == ',') {
                content.add(jsonString.substring(lastIndex, index));
                lastIndex = index + 1;
            } else if (jsonString.charAt(index) == '{') {
                int toFind = 1;
                do {
                    index++;
                    char c = jsonString.charAt(index);
                    if (c == '{') toFind++;
                    else if (c == '}') toFind--;
                } while (toFind != 0);
            } else if (jsonString.charAt(index) == '[') {
                int toFind = 1;
                do {
                    index++;
                    char c = jsonString.charAt(index);
                    if (c == '[') toFind++;
                    else if (c == ']') toFind--;
                } while (toFind != 0);
            }
            index++;

        }
        content.add(jsonString.substring(lastIndex));
        return content;
    }


    public JsonList readList(String s) {
        s = s.substring(s.indexOf('[') + 1, s.lastIndexOf(']'));
        Iterable<String> content = jsonSplit(s);
        JsonList jsonList = new JsonList();
        for (String value : content) {
            jsonList.add(readString(value));
        }
        return jsonList;
    }


    public void writeFile(String fileName, JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonObject.toJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String jsonStringLiteral(Object obj)
    {
        String str = String.valueOf(obj);
        StringBuilder sb = new StringBuilder("\"");
        for (int i=0; i<str.length(); i++)
        {
            char c = str.charAt(i);
            if (c == '\n')
            {
                sb.append("\\n");
            }
            else if (c == '\r')
            {
                sb.append("\\r");
            }
            else if (c == '"')
            {
                sb.append("\\\"");
            }
            else if (c == '\\')
            {
                sb.append("\\\\");
            }
            else if (c < 0x20)
            {
                sb.append(String.format("\\%03o", (int)c));
            }
            else if (c >= 0x80)
            {
                sb.append(String.format("\\u%04x", (int)c));
            }
            else
            {
                sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }



    public String getJsonValueFromFile(String fileName, String key) {
        try (FileInputStream in = new FileInputStream(fileName)) {
            String s = new String(in.readAllBytes());
            return getJsonValue(s, key);
        } catch (IOException ignored) {
            return null;
        }
    }

    public String getJsonValue(String jsonString, String key) {
        Iterable<String> content = jsonSplit(jsonString);
        for (String string : content) {
            int quoteStart = string.indexOf("\"") + 1;
            int quoteEnd = string.indexOf("\"", quoteStart);
            String label = string.substring(quoteStart, quoteEnd);
            if (label.equals(key)) return string.substring(string.indexOf(":", quoteEnd) + 1);
        }
        return null;
    }

}
