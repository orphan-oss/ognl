package ognl.test.objects;

import java.io.Serial;
import java.util.ArrayList;

public class ListSourceImpl extends ArrayList<Object> implements ListSource {

    @Serial
    private static final long serialVersionUID = 6144140702137776331L;

    public ListSourceImpl() {
    }

    public int getTotal() {
        return super.size();
    }

    public Object addValue(Object value) {
        return super.add(value);
    }

    public Object getName() {
        return null;
    }
}
