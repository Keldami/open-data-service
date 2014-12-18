/*  Open Data Service
    Copyright (C) 2013  Tsysin Konstantin, Reischl Patrick

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package org.jvalue.ods.processor.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.ektorp.DocumentNotFoundException;
import org.jvalue.ods.data.DataSource;
import org.jvalue.ods.db.DataRepository;
import org.jvalue.ods.utils.Assert;


final class DbInsertionFilter extends AbstractFilter<ObjectNode, ObjectNode> {

	private final DataRepository dataRepository;
	private final DataSource source;

	@Inject
	DbInsertionFilter(
			@Assisted DataRepository dataRepository,
			@Assisted DataSource source) {

		Assert.assertNotNull(source);
		this.dataRepository = dataRepository;
		this.source = source;
	}


	@Override
	protected ObjectNode doProcess(ObjectNode node) {
		String domainKey = node.at(source.getDomainIdKey()).asText();
		try {
			// update existing element
			JsonNode oldNode = dataRepository.findByDomainId(domainKey);
			node.put("_id", oldNode.get("_id").asText());
			node.put("_rev", oldNode.get("_rev").asText());
			dataRepository.update(node);

		} catch (DocumentNotFoundException dnfe) {
			// insert new element
			dataRepository.add(node);
		}
		return node;
	}


	@Override
	protected void doOnComplete() {
		// nothing to do here
	}

}