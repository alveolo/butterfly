package org.alveolo.butterfly.test.cocoon.xpath;

import java.util.Date;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Bar {
	private UUID id;

	@NotNull
	private Date created;

	public Bar() {}

	public Bar(UUID id, Date created) {
		this.id = id;
		this.created = created;
	}

	public UUID getId() {
		return id;
	}

	public Date getCreated() {
		return created;
	}
}
