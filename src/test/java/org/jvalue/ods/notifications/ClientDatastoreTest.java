package org.jvalue.ods.notifications;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvalue.ods.notifications.clients.GcmClient;
import org.jvalue.ods.notifications.db.ClientDatastoreFactory;


public final class ClientDatastoreTest {

	@BeforeClass
	@AfterClass
	public static final void clearDatabase() {
		ClientDatastore store = ClientDatastoreFactory.getCouchDbClientDatastore();
		for (Client client : store.getRegisteredClients()) {
			store.unregisterClient(client);
		}
	}


	@Test
	public final void testRegisterUnregister() {
		ClientDatastore store = ClientDatastoreFactory.getCouchDbClientDatastore();

		List<Client> clients = new ArrayList<Client>();
		clients.add(new GcmClient("foo", "pegelonline"));
		clients.add(new GcmClient("bar", "pegelonline"));
		clients.add(new GcmClient("bar", "pegeloffline"));
		clients.add(new GcmClient("foobar", "nopegel"));

		for (Client client : clients) {
			assertTrue(!store.isClientRegistered(client));
			assertTrue(!store.getRegisteredClients().contains(client));

			store.registerClient(client);

			assertTrue(store.isClientRegistered(client));
			assertTrue(store.getRegisteredClients().contains(client));

			store.unregisterClient(client);

			assertTrue(!store.isClientRegistered(client));
			assertTrue(!store.getRegisteredClients().contains(client));
		}
	}
}
