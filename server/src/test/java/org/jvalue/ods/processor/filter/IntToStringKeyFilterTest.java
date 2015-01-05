package org.jvalue.ods.processor.filter;


import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvalue.ods.api.sources.DataSource;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public final class IntToStringKeyFilterTest {

	@Mocked private MetricRegistry registry;


	@Test
	public void testObjectParentNode(@Mocked final DataSource source) throws Exception {
		new Expectations() {{
			source.getDomainIdKey();
			result = JsonPointer.compile("/parent/id");
		}};

		ObjectNode baseNode = new ObjectNode(JsonNodeFactory.instance);
		ObjectNode parentNode = baseNode.putObject("parent");
		parentNode.put("id", 10);

		ObjectNode resultNode = new IntToStringKeyFilter(source, registry).doProcess(baseNode);

		Assert.assertTrue(resultNode.path("parent").path("id").isTextual());
	}


	@Test
	public void testArrayParentNode(@Mocked final DataSource source) throws Exception {
		new Expectations() {{
			source.getDomainIdKey();
			result = JsonPointer.compile("/parent/0");
		}};

		ObjectNode baseNode = new ObjectNode(JsonNodeFactory.instance);
		ArrayNode parentNode = baseNode.putArray("parent");
		parentNode.add(10);

		ObjectNode resultNode = new IntToStringKeyFilter(source, registry).doProcess(baseNode);

		Assert.assertTrue(resultNode.path("parent").get(0).isTextual());
	}

}
