package org.ognl.test.objects;

/**
 *
 */
public interface IFormComponent extends IComponent {
    
    String getClientId();

    IForm getForm();
    
    void setForm(IForm form);
}
