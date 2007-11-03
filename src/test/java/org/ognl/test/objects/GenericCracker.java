package org.ognl.test.objects;

/**
 *
 */
public class GenericCracker implements Cracker<Integer> {

    Integer _param;

    public Integer getParam()
    {
        return _param;
    }

    public void setParam(Integer param)
    {
        _param = param;
    }
}
