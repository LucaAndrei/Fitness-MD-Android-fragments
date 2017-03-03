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

import android.util.Log;

import com.master.aluca.fitnessmd.library.Fields;
import com.master.aluca.fitnessmd.library.db.Database;

import java.util.HashMap;
import java.util.LinkedHashMap;

/** Database that is stored in memory */
public final class InMemoryDatabase implements Database {

	private static final String LOG_TAG = "InMemoryDatabase_Meteor";

	private static final String TAG = "InMemoryDatabase";
	/** The collections contained in the database */
	private final CollectionsMap mCollections;

	/** Creates a new database that is stored in memory */
	public InMemoryDatabase() {
		mCollections = new CollectionsMap();
	}

	@Override
	public InMemoryCollection getCollection(final String name) {
		if (mCollections.containsKey(name)) {
			return mCollections.get(name);
		}
		else {
			return new InMemoryCollection(name);
		}
	}

	@Override
	public String[] getCollectionNames() {
		return mCollections.keySet().toArray(new String[mCollections.size()]);
	}

	@Override
	public void onDataAdded(final String collectionName, final String documentId, final Fields newValues) {
		if (!mCollections.containsKey(collectionName)) {
			mCollections.put(collectionName, new InMemoryCollection(collectionName));
		}

		final LinkedHashMap<String, InMemoryDocument> collectionData = mCollections.get(collectionName).getDocumentsMap();

		if (newValues != null) {
			collectionData.put(documentId, new InMemoryDocument(documentId, newValues));
		}
	}

	@Override
	public void onDataChanged(final String collectionName, final String documentId, final Fields updatedValues, final String[] removedValues) {
		if (mCollections.containsKey(collectionName)) {
			final LinkedHashMap<String, InMemoryDocument> collectionData = mCollections.get(collectionName).getDocumentsMap();
			final Fields documentData = collectionData.get(documentId).getFields();

			if (updatedValues != null) {
				documentData.putAll(updatedValues);
			}

			if (removedValues != null) {
				for (String removedKey : removedValues) {
					documentData.remove(removedKey);
				}
			}
		}
		else {
			Log.d(LOG_TAG, TAG);
			Log.d(LOG_TAG,"  Cannot find document `"+documentId+"` to update in collection `"+collectionName+"`");

			onDataAdded(collectionName, documentId, updatedValues);
		}
	}

	@Override
	public void onDataRemoved(final String collectionName, final String documentId) {
		if (mCollections.containsKey(collectionName)) {
			mCollections.get(collectionName).getDocumentsMap().remove(documentId);
		}
		else {
			Log.d(LOG_TAG,TAG);
			Log.d(LOG_TAG,"  Cannot find document `"+documentId+"` to delete in collection `"+collectionName+"`");
		}
	}

	@Override
	public String toString() {
		return mCollections.toString();
	}

	/** Data type for the map backing the database */
	private static class CollectionsMap extends HashMap<String, InMemoryCollection> { }

}
