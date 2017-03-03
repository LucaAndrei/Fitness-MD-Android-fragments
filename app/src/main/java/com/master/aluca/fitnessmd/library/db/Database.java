/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.library.db;

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

import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;

/** Storage for data that exposes both read and write access */
public interface Database extends DataStore {

	/**
	 * Returns the collection with the specified name from the database
	 *
	 * The collection may or may not actually exist
	 *
	 * If the collection does not exist, an empty collection is implicitly created
	 *
	 * @param name the name of the collection to return
	 * @return a collection object (never `null`)
	 */
	InMemoryCollection getCollection(String name);

	/**
	 * Lists all collections from the database by returning a set of their names
	 *
	 * @return an array containing the names of all collections
	 */
	String[] getCollectionNames();
}
