package org.alveolo.butterfly.test.cocoon.xpath;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.NotEmpty;


@XmlRootElement
public class Foo {
	private long id;

	@NotEmpty @Size(max=64)
	private String name;

	public Foo() {}

	public Foo(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
