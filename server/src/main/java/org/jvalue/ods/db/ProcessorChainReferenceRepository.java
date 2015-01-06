package org.jvalue.ods.db;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.jvalue.ods.api.processors.ProcessorReferenceChain;

import java.util.List;


public final class ProcessorChainReferenceRepository extends RepositoryAdapter<
		ProcessorChainReferenceRepository.ProcessorChainReferenceCouchDbRepository,
		ProcessorChainReferenceRepository.ProcessorReferenceChainDocument,
		ProcessorReferenceChain> {

	private static final String DOCUMENT_ID = "doc.value.id != null && doc.value.processors != null";

	@Inject
	ProcessorChainReferenceRepository(CouchDbInstance couchDbInstance, @Assisted String databaseName) {
		super(new ProcessorChainReferenceCouchDbRepository(couchDbInstance, databaseName));
	}


	@View( name = "all", map = "function(doc) { if (" + DOCUMENT_ID + ") emit(null, doc) }")
	static final class ProcessorChainReferenceCouchDbRepository
			extends CouchDbRepositorySupport<ProcessorChainReferenceRepository.ProcessorReferenceChainDocument>
			implements DbDocumentAdaptable<ProcessorChainReferenceRepository.ProcessorReferenceChainDocument, ProcessorReferenceChain> {

		ProcessorChainReferenceCouchDbRepository(CouchDbInstance couchDbInstance, String databaseName) {
			super(ProcessorReferenceChainDocument.class, couchDbInstance.createConnector(databaseName, true));
			initStandardDesignDocument();
		}


		@Override
		@View(name = "by_id", map = "function(doc) { if (" + DOCUMENT_ID + ") emit(doc.value.id, doc._id) }")
		public ProcessorReferenceChainDocument findById(String processorChainId) {
			List<ProcessorReferenceChainDocument> chains = queryView("by_id", processorChainId);
			if (chains.isEmpty()) throw new DocumentNotFoundException(processorChainId);
			if (chains.size() > 1)
				throw new IllegalStateException("found more than one chain for id " + processorChainId);
			return chains.get(0);
		}


		@Override
		public ProcessorReferenceChainDocument createDbDocument(ProcessorReferenceChain reference) {
			return new ProcessorReferenceChainDocument(reference);
		}


		@Override
		public String getIdForValue(ProcessorReferenceChain reference) {
			return reference.getId();
		}

	}


	static final class ProcessorReferenceChainDocument extends DbDocument<ProcessorReferenceChain> {

		@JsonCreator
		public ProcessorReferenceChainDocument(
				@JsonProperty("value") ProcessorReferenceChain reference) {
			super(reference);
		}

	}
}
