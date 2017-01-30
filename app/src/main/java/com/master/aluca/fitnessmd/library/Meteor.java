package com.master.aluca.fitnessmd.library;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.tubesock.WebSocket;
import com.firebase.tubesock.WebSocketEventHandler;
import com.firebase.tubesock.WebSocketException;
import com.firebase.tubesock.WebSocketMessage;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.library.db.DataStore;
import com.master.aluca.fitnessmd.library.db.Database;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDatabase;
import com.master.aluca.fitnessmd.library.listeners.Listener;
import com.master.aluca.fitnessmd.library.listeners.ResultListener;
import com.master.aluca.fitnessmd.library.listeners.SubscribeListener;
import com.master.aluca.fitnessmd.library.listeners.UnsubscribeListener;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Provides a single access point to the `Meteor` class that can be used across `Activity` instances */
public class Meteor {

	private static final String LOG_TAG = "Fitness_Meteor";
	/** Supported versions of the DDP protocol in order of preference */
	private static final String[] SUPPORTED_DDP_VERSIONS = {"1", "pre2", "pre1"};
	/** The maximum number of attempts to re-connect to the server over WebSocket */
	private static final int RECONNECT_ATTEMPTS_MAX = 5;
	/** Instance of Jackson library's ObjectMapper that converts between JSON and Java objects (POJOs) */
	private static final ObjectMapper mObjectMapper = new ObjectMapper();
	/** The WebSocket connection that will be used for the data transfer */
	private WebSocket mWebSocket;
	/** The callback that handles messages and events received from the WebSocket connection */
	private final WebSocketEventHandler mWebSocketEventHandler;
	/** Map that tracks all pending Listener instances */
	private final Map<String, Listener> mListeners;
	/** Messages that couldn't be dispatched yet and thus had to be queued */
	private final Queue<String> mQueuedMessages;
	/** Whether logging should be enabled or not */
	private static boolean mLoggingEnabled;
	private String mServerUri;
	private String mDdpVersion = "1";
	/** The number of unsuccessful attempts to re-connect in sequence */
	private int mReconnectAttempts;
	/** The callbacks that will handle events and receive messages from this client */
	protected final CallbackProxy mCallbackProxy = new CallbackProxy();
	private String mSessionID;
	private boolean mConnected;
	private String mLoggedInUserId;
	private final DataStore mDataStore;

	private Context mContext;

	/**
	 * Returns a new instance for a client connecting to a server via DDP over websocket
	 *
	 * The server URI should usually be in the form of `ws://example.meteor.com/websocket`
	 *
	 * @param context
	 * @param serverUri the server URI to connect to
	 */
	public Meteor(Context context, final String serverUri) {
		// create a new handler that processes the messages and events received from the WebSocket connection
		mContext = context;
		mDataStore = new InMemoryDatabase();

		mWebSocketEventHandler = new WebSocketEventHandler() {

			@Override
			public void onOpen() {
				Log.d(LOG_TAG, "onOpen");

				mConnected = true;
				mReconnectAttempts = 0;
				initConnection(mSessionID);
			}

			@Override
			public void onClose() {
				Log.d(LOG_TAG, "onClose");

				if (mConnected) {
					mReconnectAttempts++;
					if (mReconnectAttempts <= RECONNECT_ATTEMPTS_MAX) {
						// try to re-connect automatically
						reconnect();
					} else {
						disconnect();
					}
				}
				mConnected = false;
				mCallbackProxy.onDisconnect();
			}

			@Override
			public void onMessage(final WebSocketMessage message) {
				Log.d(LOG_TAG, "onTextMessage");

				if (message.isText()) {
					Log.d(LOG_TAG, "payload == " + message.getText());
					handleMessage(message.getText());
				}
				else {
					Log.d(LOG_TAG, "binary. IGNORED");
				}
			}

			@Override
			public void onError(final WebSocketException e) {
				Log.d(LOG_TAG, "onError");
				e.printStackTrace();
			}

			@Override
			public void onLogMessage(final String msg) {
				Log.d(LOG_TAG, "onLogMessage : " + msg);
			}

		};
		// create a map that holds the pending Listener instances
		mListeners = new HashMap<String, Listener>();

		// create a queue that holds undispatched messages waiting to be sent
		mQueuedMessages = new ConcurrentLinkedQueue<String>();

		// save the server URI
		mServerUri = serverUri;
		// count the number of failed attempts to re-connect
		mReconnectAttempts = 0;
	}

	/** Attempts to establish the connection to the server */
	public void connect() {
		// create a new WebSocket connection for the data transfer
		mWebSocket = new WebSocket(URI.create(mServerUri));

		// attach the handler to the connection
		mWebSocket.setEventHandler(mWebSocketEventHandler);

		try {
			mWebSocket.connect();
		}
		catch (WebSocketException e) {
			mCallbackProxy.onException(e);
		}
	}

	/** Manually attempt to re-connect if necessary */
	public void reconnect() {
		if (mConnected) {
			Log.d(LOG_TAG, "isConnected. Trying to reconnect...");
			initConnection(mSessionID);
			return;
		}
	}

	/**
	 * Disconnect the client from the server
	 */
	public void disconnect() {
		mConnected = false;
		mListeners.clear();
		mSessionID = null;

		if (mWebSocket != null) {
			try {
				mWebSocket.close();
			} catch (Exception e) {
				mCallbackProxy.onException(e);
			}
		} else {
			throw new IllegalStateException("You must have called the 'connect' method before you can disconnect again");
		}
	}

	/**
	 * Returns whether this client is connected or not
	 *
	 * @return whether this client is connected
	 */
	public boolean isConnected() {
		return mConnected;
	}


	/**
	 * Establish the connection to the server as requested by the DDP protocol (after the websocket has been opened)
	 *
	 * @param existingSessionID an existing session ID or `null`
	 */
	private void initConnection(final String existingSessionID) {
		Log.d(LOG_TAG, "initConnection : " + existingSessionID);
		final Map<String, Object> data = new Fields();
		data.put(Protocol.Field.MESSAGE, Protocol.Message.CONNECT);
		data.put(Protocol.Field.VERSION, mDdpVersion);
		data.put(Protocol.Field.SUPPORT, SUPPORTED_DDP_VERSIONS);
		if (existingSessionID != null) {
			data.put(Protocol.Field.SESSION, existingSessionID);
		}
		send(data);
	}

	private void send(final Object obj) {
		// serialize the object to JSON
		final String jsonStr = toJson(obj);

		if (jsonStr == null) {
			throw new IllegalArgumentException("Object would be serialized to `null`");
		}

		// send the JSON string
		send(jsonStr);
	}

	/**
	 * Sends a string over the websocket
	 *
	 * @param message the string to send
	 */
	private void send(final String message) {
		Log.d(LOG_TAG, "send");
		Log.d(LOG_TAG, "message == " + message);

		if (message == null) {
			throw new IllegalArgumentException("You cannot send `null` messages");
		}

		if (mConnected) {
			Log.d(LOG_TAG, "dispatching");

			if (mWebSocket != null) {
				mWebSocket.send(message);
			}
			else {
				throw new IllegalStateException("You must have called the 'connect' method before you can send data");
			}
		}
		else {
			Log.d(LOG_TAG, "queueing");
			mQueuedMessages.add(message);
		}
	}

	/**
	 * Adds a callback that will handle events and receive messages from this client
	 *
	 * @param callback the callback instance
	 */
	public void addCallback(MeteorCallback callback) {
		mCallbackProxy.addCallback(callback);
	}

	/** Removes all callbacks that were to handle events and receive messages from this client */
	public void removeCallbacks() {
		mCallbackProxy.removeCallbacks();
	}

	private String toJson(Object obj) {
		try {
			return mObjectMapper.writeValueAsString(obj);
		}
		catch (Exception e) {
			Log.d(LOG_TAG, "toJson : ");
			e.printStackTrace();
			return null;
		}
	}

	private <T> T fromJson(final String json, final Class<T> targetType) {
		try {
			if (json != null) {
				final JsonNode jsonNode = mObjectMapper.readTree(json);
				return mObjectMapper.convertValue(jsonNode, targetType);
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			Log.d(LOG_TAG, "fromJson : ");
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Called whenever a JSON payload has been received from the websocket
	 *
	 * @param payload the JSON payload to process
	 */
	private void handleMessage(final String payload) {
		Log.d(LOG_TAG, "  handleMessage : " + payload);
		final JsonNode data;
		try {
			data = mObjectMapper.readTree(payload);
		} catch (JsonProcessingException e) {
			mCallbackProxy.onException(e);

			return;
		} catch (IOException e) {
			mCallbackProxy.onException(e);

			return;
		}

		if (data != null) {
			if (data.has(Protocol.Field.MESSAGE)) {
				Log.d(LOG_TAG, "  has MESSAGE");
				final String message = data.get(Protocol.Field.MESSAGE).getTextValue();

				if (message.equals(Protocol.Message.CONNECTED)) {
					Log.d(LOG_TAG, "  has CONNECTED");
					if (data.has(Protocol.Field.SESSION)) {
						Log.d(LOG_TAG, "  has SESSION");
						mSessionID = data.get(Protocol.Field.SESSION).getTextValue();
					}


					initSession();

				} else if (message.equals(Protocol.Message.FAILED)) {
					Log.d(LOG_TAG, "  has FAILED");
					if (data.has(Protocol.Field.VERSION)) {
						Log.d(LOG_TAG, "  has VERSION");
						// the server wants to use a different protocol version
						final String desiredVersion = data.get(Protocol.Field.VERSION).getTextValue();

						// if the protocol version that was requested by the server is supported by this client
						if (isVersionSupported(desiredVersion)) {
							Log.d(LOG_TAG, "  has desiredVersion : " + desiredVersion);
							// remember which version has been requested
							mDdpVersion = desiredVersion;

							// the server should be closing the connection now and we will re-connect afterwards
						} else {
							throw new RuntimeException("Protocol version not supported: " + desiredVersion);
						}
					}
				} else if (message.equals(Protocol.Message.PING)) {
					Log.d(LOG_TAG, "  has PING");
					final String id;
					if (data.has(Protocol.Field.ID)) {
						Log.d(LOG_TAG, "  has ID");
						id = data.get(Protocol.Field.ID).getTextValue();
					} else {
						id = null;
					}

					final Map<String, Object> sendPongData = new Fields();
					sendPongData.put(Protocol.Field.MESSAGE, Protocol.Message.PONG);
					if (id != null) {
						sendPongData.put(Protocol.Field.ID, id);
					}
					send(sendPongData);


				} else if (message.equals(Protocol.Message.ADDED) || message.equals(Protocol.Message.ADDED_BEFORE)) {
					Log.d(LOG_TAG, "  has ADDED");
					final String documentID;
					if (data.has(Protocol.Field.ID)) {
						documentID = data.get(Protocol.Field.ID).getTextValue();
					} else {
						documentID = null;
					}

					final String collectionName;
					if (data.has(Protocol.Field.COLLECTION)) {
						collectionName = data.get(Protocol.Field.COLLECTION).getTextValue();
					} else {
						collectionName = null;
					}

					final String newValuesJson;
					if (data.has(Protocol.Field.FIELDS)) {
						newValuesJson = data.get(Protocol.Field.FIELDS).toString();
					} else {
						newValuesJson = null;
					}

					if (mDataStore != null) {
						mDataStore.onDataAdded(collectionName, documentID, fromJson(newValuesJson, Fields.class));
					}

					mCallbackProxy.onDataAdded(collectionName, documentID, newValuesJson);
				} else if (message.equals(Protocol.Message.CHANGED)) {
					Log.d(LOG_TAG, "  has CHANGED");
					final String documentID;
					if (data.has(Protocol.Field.ID)) {
						documentID = data.get(Protocol.Field.ID).getTextValue();
					} else {
						documentID = null;
					}

					final String collectionName;
					if (data.has(Protocol.Field.COLLECTION)) {
						collectionName = data.get(Protocol.Field.COLLECTION).getTextValue();
					} else {
						collectionName = null;
					}

					final String updatedValuesJson;
					if (data.has(Protocol.Field.FIELDS)) {
						updatedValuesJson = data.get(Protocol.Field.FIELDS).toString();
					} else {
						updatedValuesJson = null;
					}

					final String removedValuesJson;
					if (data.has(Protocol.Field.CLEARED)) {
						removedValuesJson = data.get(Protocol.Field.CLEARED).toString();
					} else {
						removedValuesJson = null;
					}

					if (mDataStore != null) {
						mDataStore.onDataChanged(collectionName, documentID, fromJson(updatedValuesJson, Fields.class),
								fromJson(removedValuesJson, String[].class));
					}

					mCallbackProxy.onDataChanged(collectionName, documentID, updatedValuesJson, removedValuesJson);
				} else if (message.equals(Protocol.Message.REMOVED)) {
					Log.d(LOG_TAG, "  has REMOVED");
					final String documentID;
					if (data.has(Protocol.Field.ID)) {
						documentID = data.get(Protocol.Field.ID).getTextValue();
					} else {
						documentID = null;
					}

					final String collectionName;
					if (data.has(Protocol.Field.COLLECTION)) {
						collectionName = data.get(Protocol.Field.COLLECTION).getTextValue();
					} else {
						collectionName = null;
					}

					if (mDataStore != null) {
						mDataStore.onDataRemoved(collectionName, documentID);
					}

					mCallbackProxy.onDataRemoved(collectionName, documentID);
				} else if (message.equals(Protocol.Message.RESULT)) {
					Log.d(LOG_TAG, "  has RESULT");
					// check if we have to process any result data internally
					if (data.has(Protocol.Field.RESULT)) {
						final JsonNode resultData = data.get(Protocol.Field.RESULT);
						// if the result is from a previous login attempt
						if (isLoginResult(resultData)) {
							Log.d(LOG_TAG, "  isLoginResult");
							// extract the login token for subsequent automatic re-login
							final String loginToken = resultData.get(Protocol.Field.TOKEN).getTextValue();
							SharedPreferencesManager.getInstance(mContext).saveServerLoginToken(loginToken);
							Log.d(LOG_TAG, "  loginToken : " + loginToken);

							// extract the user's ID
							mLoggedInUserId = resultData.get(Protocol.Field.ID).getTextValue();
							Log.d(LOG_TAG, "  mLoggedInUserId : " + mLoggedInUserId);
						}
					}

					final String id;
					if (data.has(Protocol.Field.ID)) {
						id = data.get(Protocol.Field.ID).getTextValue();
					} else {
						id = null;
					}

					final Listener listener = mListeners.get(id);

					if (listener instanceof ResultListener) {
						mListeners.remove(id);

						final String result;
						if (data.has(Protocol.Field.RESULT)) {
							result = data.get(Protocol.Field.RESULT).toString();
						} else {
							result = null;
						}

						if (data.has(Protocol.Field.ERROR)) {
							final Protocol.Error error = Protocol.Error.fromJson(data.get(Protocol.Field.ERROR));
							mCallbackProxy.forResultListener((ResultListener) listener).onError(error.getError(), error.getReason(), error.getDetails());
						} else {
							mCallbackProxy.forResultListener((ResultListener) listener).onSuccess(result);
						}
					}
				} else if (message.equals(Protocol.Message.READY)) {
					Log.d(LOG_TAG, "  has READY");
					if (data.has(Protocol.Field.SUBS)) {
						final Iterator<JsonNode> elements = data.get(Protocol.Field.SUBS).getElements();
						String subscriptionId;
						while (elements.hasNext()) {
							subscriptionId = elements.next().getTextValue();

							final Listener listener = mListeners.get(subscriptionId);

							if (listener instanceof SubscribeListener) {
								mListeners.remove(subscriptionId);

								mCallbackProxy.forSubscribeListener((SubscribeListener) listener).onSuccess();
							}
						}
					}
				} else if (message.equals(Protocol.Message.NOSUB)) {
					Log.d(LOG_TAG, "  has NOSUB");
					final String subscriptionId;
					if (data.has(Protocol.Field.ID)) {
						subscriptionId = data.get(Protocol.Field.ID).getTextValue();
					} else {
						subscriptionId = null;
					}

					final Listener listener = mListeners.get(subscriptionId);

					if (listener instanceof SubscribeListener) {
						mListeners.remove(subscriptionId);

						if (data.has(Protocol.Field.ERROR)) {
							final Protocol.Error error = Protocol.Error.fromJson(data.get(Protocol.Field.ERROR));
							mCallbackProxy.forSubscribeListener((SubscribeListener) listener).onError(error.getError(), error.getReason(), error.getDetails());
						} else {
							mCallbackProxy.forSubscribeListener((SubscribeListener) listener).onError(null, null, null);
						}
					} else if (listener instanceof UnsubscribeListener) {
						mListeners.remove(subscriptionId);

						mCallbackProxy.forUnsubscribeListener((UnsubscribeListener) listener).onSuccess();
					}
				}
			}
		}
	}

	/**
	 * Returns whether the specified version of the DDP protocol is supported or not
	 *
	 * @param protocolVersion the DDP protocol version
	 * @return whether the version is supported or not
	 */
	public static boolean isVersionSupported(final String protocolVersion) {
		return Arrays.asList(SUPPORTED_DDP_VERSIONS).contains(protocolVersion);
	}

	/**
	 * Returns whether the client is currently logged in as some user
	 *
	 * @return whether the client is logged in (`true`) or not (`false`)
	 */
	public boolean isLoggedIn() {
		return mLoggedInUserId != null;
	}

	/**
	 * Returns the ID of the user who is currently logged in
	 *
	 * @return the ID or `null`
	 */
	public String getUserId() {
		return mLoggedInUserId;
	}

	/**
	 * Creates and returns a new unique ID
	 *
	 * @return the new unique ID
	 */
	public static String uniqueID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Sign in the user with the given email address and password
	 *
	 * Please note that this requires the `accounts-password` package
	 *
	 * @param email the email address to sign in with
	 * @param password the password to sign in with
	 * @param listener the listener to call on success/error
	 */
	public void loginWithEmail(final String email, final String password, final ResultListener listener) {
		final Map<String, Object> userData = new Fields();
		if (email != null) {
			userData.put("email", email);
		}
		else {
			throw new IllegalArgumentException("You must provide an email address");
		}

		final Map<String, Object> authData = new Fields();
		authData.put("user", userData);
		authData.put("password", password);

		call("login", new Object[] { authData }, listener);
	}

	public void logout(final ResultListener listener) {
		call("logout", new Object[]{}, new ResultListener() {

			@Override
			public void onSuccess(final String result) {
				// remember that we're not logged in anymore
				mLoggedInUserId = null;

				// delete the last login token which is now invalid
				SharedPreferencesManager.getInstance(mContext).saveServerLoginToken(null);
				SharedPreferencesManager.getInstance(mContext).setLoggedIn(false);

				if (listener != null) {
					mCallbackProxy.forResultListener(listener).onSuccess(result);
				}
			}

			@Override
			public void onError(final String error, final String reason, final String details) {
				if (listener != null) {
					mCallbackProxy.forResultListener(listener).onError(error, reason, details);
				}
			}

		});
	}

	/**
	 * Registers a new user with the specified username, email address and password
	 *
	 * This method will automatically login as the new user on success
	 *
	 * Please note that this requires the `accounts-password` package
	 *
	 * @param username the username to register with (either this or `email` is required)
	 * @param email the email address to register with (either this or `username` is required)
	 * @param password the password to register with
	 * @param listener the listener to call on success/error
	 */
	public void registerAndLogin(final String username, final String email, final String password, final ResultListener listener) {
		if (username == null && email == null) {
			throw new IllegalArgumentException("You must provide either a username or an email address");
		}

		final Map<String, Object> accountData = new Fields();
		if (username != null) {
			accountData.put("username", username);
		}
		if (email != null) {
			accountData.put("email", email);
		}
		accountData.put("password", password);

		call("createUser", new Object[]{accountData}, listener);
	}

	/**
	 * Executes a remote procedure call (any Java objects (POJOs) will be serialized to JSON by the Jackson library)
	 *
	 * @param methodName the name of the method to call, e.g. `/someCollection.insert`
	 * @param params the objects that should be passed to the method as parameters
	 * @param listener the listener to trigger when the result has been received or `null`
	 */
	public void call(final String methodName, final Object[] params, final ResultListener listener) {
		// create a new unique ID for this request
		final String callId = uniqueID();

		// save a reference to the listener to be executed later
		if (listener != null) {
			mListeners.put(callId, listener);
		}

		// send the request
		final Map<String, Object> data = new Fields();
		data.put(Protocol.Field.MESSAGE, Protocol.Message.METHOD);
		data.put(Protocol.Field.METHOD, methodName);
		data.put(Protocol.Field.ID, callId);
		if (params != null) {
			data.put(Protocol.Field.PARAMS, params);
		}
		send(data);
	}

	/**
	 * Subscribes to a specific subscription from the server
	 *
	 * @param subscriptionName the name of the subscription
	 * @param params the subscription parameters
	 * @param listener the listener to call on success/error
	 * @return the generated subscription ID (must be used when unsubscribing)
	 */
	public String subscribe(final String subscriptionName, final Object[] params, final SubscribeListener listener) {
		// create a new unique ID for this request
		final String subscriptionId = uniqueID();

		// save a reference to the listener to be executed later
		if (listener != null) {
			mListeners.put(subscriptionId, listener);
		}

		// send the request
		final Map<String, Object> data = new Fields();
		data.put(Protocol.Field.MESSAGE, Protocol.Message.SUBSCRIBE);
		data.put(Protocol.Field.NAME, subscriptionName);
		data.put(Protocol.Field.ID, subscriptionId);
		if (params != null) {
			data.put(Protocol.Field.PARAMS, params);
		}
		send(data);

		// return the generated subscription ID
		return subscriptionId;
	}

	/**
	 * Unsubscribes from the subscription with the specified name
	 *
	 * @param subscriptionId the ID of the subscription
	 * @param listener the listener to call on success/error
	 */
	public void unsubscribe(final String subscriptionId, final UnsubscribeListener listener) {
		// save a reference to the listener to be executed later
		if (listener != null) {
			mListeners.put(subscriptionId, listener);
		}

		// send the request
		final Map<String, Object> data = new Fields();
		data.put(Protocol.Field.MESSAGE, Protocol.Message.UNSUBSCRIBE);
		data.put(Protocol.Field.ID, subscriptionId);
		send(data);
	}

	/**
	 * Returns whether the given JSON result is from a previous login attempt
	 *
	 * @param result the JSON result
	 * @return whether the result is from a login attempt (`true`) or not (`false`)
	 */
	private static boolean isLoginResult(final JsonNode result) {
		return result.has(Protocol.Field.TOKEN) && result.has(Protocol.Field.ID);
	}

	/**
	 * Attempts to sign in with the given login token
	 *
	 * @param token    the login token
	 * @param listener the listener to call on success/error
	 */
	private void loginWithToken(final String token, final ResultListener listener) {
		final Map<String, Object> authData = new HashMap<String, Object>();
		authData.put("resume", token);
		Log.d(LOG_TAG, "  loginWithToken : " + token);

		call("login", new Object[]{authData}, listener);
	}

	private void initSession() {
		// get the last login token

		Log.d(LOG_TAG, "  initSession");
		final String loginToken = SharedPreferencesManager.getInstance(mContext).getServerLoginToken();
		Log.d(LOG_TAG, "  initSession loginToken : " + loginToken);

		// if we found a login token that might work
		if (loginToken != null) {
			// try to sign in with that token
			loginWithToken(loginToken, new ResultListener() {

				@Override
				public void onSuccess(final String result) {
					Log.d(LOG_TAG, "loginWithToken : onSuccess " + result);
					announceSessionReady(true);
					SharedPreferencesManager.getInstance(mContext).setLoggedIn(true);
				}

				@Override
				public void onError(final String error, final String reason, final String details) {
					// clear the user ID since automatic sign-in has failed
					mLoggedInUserId = null;
					Log.d(LOG_TAG, "  loginWithToken : onError " + error + " >> " + reason + " >> " + details);

					// discard the token which turned out to be invalid
					SharedPreferencesManager.getInstance(mContext).saveServerLoginToken(null);
					SharedPreferencesManager.getInstance(mContext).setLoggedIn(false);



					announceSessionReady(false);
				}

			});
		}
		// if we didn't find any login token
		else {
			announceSessionReady(false);
			SharedPreferencesManager.getInstance(mContext).setLoggedIn(false);
		}
	}

	/**
	 * Announces that the new session is now ready to use
	 *
	 */
	private void announceSessionReady(boolean shouldSignIn) {
		// run the callback that waits for the connection to open

		Log.d(LOG_TAG, "announceSessionReady");
		mCallbackProxy.onConnect(shouldSignIn);

		// try to dispatch queued messages now
		for (String queuedMessage : mQueuedMessages) {
			send(queuedMessage);
		}
	}

	/**
	 * Returns the database that was set in the constructor and that contains all data received from the server
	 *
	 * @return the database or `null`
	 */
	public Database getDatabase() {
		return (Database) mDataStore;
	}
}
