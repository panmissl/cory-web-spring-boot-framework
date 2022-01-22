package com.cory.util.addressdata;

import java.io.Serializable;
import java.util.List;

public class AddressData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String code;
	private String name;
	
	private List<AddressData> children;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AddressData> getChildren() {
		return children;
	}

	public void setChildren(List<AddressData> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "[id: " + id + ", code: " + code + ", name: " + name + "]";
	}
}
