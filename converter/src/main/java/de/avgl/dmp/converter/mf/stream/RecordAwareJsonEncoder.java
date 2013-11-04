package de.avgl.dmp.converter.mf.stream;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(String.class)
public class RecordAwareJsonEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private final StreamPipe delegate;

	public RecordAwareJsonEncoder(StreamPipe delegate) {
		this.delegate = delegate;
	}

	@Override
	public void startRecord(String id) {
		delegate.startRecord("");
		delegate.literal("record_id", id);
		delegate.startEntity("record_data");
	}

	@Override
	public void endRecord() {
		delegate.endEntity();
		delegate.endRecord();
	}

	@Override
	public void startEntity(String name) {
		delegate.startEntity(name);
	}

	@Override
	public void endEntity() {
		delegate.endEntity();
	}

	@Override
	public void literal(String name, String value) {
		delegate.literal(name, value);
	}
}