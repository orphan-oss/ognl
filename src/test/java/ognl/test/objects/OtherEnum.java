package ognl.test.objects;

/**
 *
 */
public enum OtherEnum {

    ONE(1);

    public static final String STATIC_STRING = "string";

    private int _value;

    private OtherEnum(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }
}
