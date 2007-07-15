package org.ognl.test.objects;

/**
 * Simple object used to test indexed map references using "#this" references.
 */
public class IndexedMapObject {

    String property;

    public IndexedMapObject(String property)
    {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
