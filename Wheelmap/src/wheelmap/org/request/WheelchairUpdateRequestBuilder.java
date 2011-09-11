package wheelmap.org.request;

import wheelmap.org.WheelchairState;

/**
 * Constructs the Uri of a <code>/api/nodes/{node_id}/update_wheelchair</code>
 * request
 */
public class WheelchairUpdateRequestBuilder extends RequestBuilder {

	private static final String RESOURCE = "nodes";
	private long id;
	private WheelchairState state;

	public WheelchairUpdateRequestBuilder(final String server,
			final String apiKey, final AcceptType acceptType, long id,
			WheelchairState state) {
		super(server, apiKey, acceptType);
		this.id = id;
		this.state = state;
	}

	@Override
	public String buildRequestUri() {
		final StringBuilder requestAsStringBuffer = new StringBuilder(200);
		requestAsStringBuffer.append(String.format(baseUrl()));
		requestAsStringBuffer.append("&wheelchair=");
		requestAsStringBuffer.append(state.asRequestParameter());

		return requestAsStringBuffer.toString();
	}

	@Override
	protected String resourcePath() {
		return RESOURCE + "/" + id + "/update_wheelchair";
	}
}
