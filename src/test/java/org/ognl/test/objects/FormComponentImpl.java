package org.ognl.test.objects;

/**
 *
 */
public class FormComponentImpl extends ComponentImpl implements IFormComponent {

    IForm _form;


    public IForm getForm()
    {
        return _form;
    }

    public void setForm(IForm form)
    {
        _form = form;
    }
}
