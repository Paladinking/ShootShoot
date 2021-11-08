package json;

public interface JsonParsable extends Cloneable{

    String toJsonString();

    JsonParsable copy();
}
