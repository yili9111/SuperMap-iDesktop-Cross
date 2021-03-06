package com.supermap.desktop.ui.mdi.events;

import com.supermap.desktop.ui.mdi.MdiPage;

import java.util.EventObject;

/**
 * Created by highsad on 2016/9/22.
 */
public class PageClosedEvent extends EventObject {

	private MdiPage page;
	private int operation = Operation.CLOSE;
	private boolean isDeactived = false;

	public PageClosedEvent(Object source, MdiPage page, int operationType) {
		super(source);
		this.page = page;
		this.operation = operationType == Operation.CHANGE_GROUP ? Operation.CHANGE_GROUP : Operation.CLOSE;
		this.isDeactived = false;
	}

	/**
	 * Constructs a prototypical Event.
	 *
	 * @param source The object on which the Event initially occurred.
	 * @throws IllegalArgumentException if source is null.
	 */
	public PageClosedEvent(Object source, MdiPage page, int operationType, boolean isDeactived) {
		super(source);
		this.page = page;
		this.operation = operationType == Operation.CHANGE_GROUP ? Operation.CHANGE_GROUP : Operation.CLOSE;
		this.isDeactived = isDeactived;
	}

	public MdiPage getPage() {
		return page;
	}

	public int getOperation() {
		return operation;
	}

	public boolean isDeactived() {
		return isDeactived;
	}
}
