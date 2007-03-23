package org.ognl.test.objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MenuItem {

    private String page;
	private String label;
	private List<MenuItem> children = new ArrayList<MenuItem>();

	public MenuItem(String page, String label){
		this(page, label, new ArrayList<MenuItem>());
	}

	public MenuItem(String page, String label, List<MenuItem> children){
		this.page = page;
		this.label = label;
		this.children = children;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public String getLabel() {
		return label;
	}

	public String getPage() {
		return page;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer("MenuItem[");
		sb.append("page="+getPage());
		sb.append(",label="+getLabel());
		sb.append(",children="+getChildren().size());
		sb.append("]");
		return sb.toString();
	}
}
