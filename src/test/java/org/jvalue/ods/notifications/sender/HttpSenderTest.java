package org.jvalue.ods.notifications.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.jvalue.ods.data.DataSource;
import org.jvalue.ods.data.DummyDataSource;
import org.jvalue.ods.notifications.clients.ClientFactory;
import org.jvalue.ods.notifications.clients.HttpClient;
import org.jvalue.ods.utils.RestException;


public final class HttpSenderTest {

	private final DataSource source = DummyDataSource.newInstance("dummy", "dummy");
	private final HttpSender sender = new HttpSender();
	private final HttpClient 
		noDataClient = ClientFactory.newHttpClient("dummy", "dummy", "dummy", false),
		dataClient = ClientFactory.newHttpClient("dummy", "dummy", "dummy", true);


	@Test
	public final void testFailNoData() {
		
		SenderResult result = sender.notifySourceChanged(noDataClient, source, null);

		assertNotNull(result);
		assertEquals(result.getStatus(), SenderResult.Status.ERROR);
		assertTrue(result.getErrorCause() instanceof RestException);

	}


	@Test
	public final void testFail() {
		
		SenderResult result = sender.notifySourceChanged(
				dataClient, 
				source, 
				"hello");

		assertNotNull(result);
		assertEquals(result.getStatus(), SenderResult.Status.ERROR);
		assertTrue(result.getErrorCause() instanceof RestException);

	}

}
