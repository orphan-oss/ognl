package ognl.test.race;

public class Person extends Base {
    private String name = "abc";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
