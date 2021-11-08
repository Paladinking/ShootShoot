package json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("unused")
public class JsonList implements JsonParsable, Iterable<Object> {

    private final List<Object> content;

    public JsonList() {
        content = new ArrayList<>();
    }

    public boolean add(Object o) {
        return content.add(o);
    }

    public boolean remove(Object o) {
        return content.remove(o);
    }

    @Override
    public String toJsonString(){
        StringBuilder s = new StringBuilder("[");
        for (Object object : content) {
            if (object instanceof JsonParsable){
                s.append(((JsonParsable) object).toJsonString());
            } else if(object instanceof Number || object == null){
                s.append(object);
            } else {
                s.append(JSonParser.jsonStringLiteral(object));
            }
            s.append(",");
        }
        s.replace(s.length() - 1, s.length(), "");
        s.append("]");
        return s.toString();
    }

    @Override
    public JsonParsable copy() {
        JsonList copy = new JsonList();
        for (Object object : this){
            if (object instanceof JsonParsable) copy.add(((JsonParsable) object).copy());
            else copy.add(object);
        }
        return copy;
    }

    @Override
    public String toString(){
        return this.toJsonString();
    }

    @Override
    public Iterator<Object> iterator() {
        return content.iterator();
    }

    public List<Object> toList(){
        return content;
    }

    public int size() {
        return content.size();
    }

    public JsonObject getObject(int i){
        return (JsonObject) content.get(i);
    }

    public Object get(int i){
        return content.get(i);
    }

    public int getInt(int i){
        return (int) content.get(i);
    }

    public double getDouble(int i){
        return (double) content.get(i);
    }

    public JsonList getList(int i){
        return (JsonList) content.get(i);
    }

    public String getString(int i) {
        return (String) content.get(i);
    }
}
