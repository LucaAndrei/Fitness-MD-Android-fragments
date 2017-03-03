/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.library.db.memory;

/*
 * Copyright (c) delight.im <info@delight.im>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.LinkedHashMap;

/** Collection that is stored in memory */
public final class InMemoryCollection {

	/** The name of the collection */
	private final String mName;
	/** The map of documents backing the collection */
	private final LinkedHashMap<String, InMemoryDocument> mDocuments;

	/**
	 * Creates a new collection that is stored in memory
	 *
	 * @param name the name of the collection to create
	 */
	protected InMemoryCollection(final String name) {
		mName = name;
		mDocuments = new LinkedHashMap<String, InMemoryDocument>();
	}

	public String getName() {
		return mName;
	}

	public InMemoryDocument getDocument(final String id) {
		return mDocuments.get(id);
	}

	public String[] getDocumentIds() {
		return mDocuments.keySet().toArray(new String[mDocuments.size()]);
	}

	/**
	 * Returns the raw map of documents backing this collection
	 *
	 * @return the raw map of documents
	 */
	protected LinkedHashMap<String, InMemoryDocument> getDocumentsMap() {
		return mDocuments;
	}

}
