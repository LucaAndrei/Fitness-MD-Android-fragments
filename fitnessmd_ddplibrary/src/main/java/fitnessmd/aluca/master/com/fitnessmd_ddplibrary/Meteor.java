package fitnessmd.aluca.master.com.fitnessmd_ddplibrary;

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
import android.util.Log;

import com.firebase.tubesock.WebSocket;
import com.firebase.tubesock.WebSocketEventHandler;
import com.firebase.tubesock.WebSocketException;
import com.firebase.tubesock.WebSocketMessage;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Provides a single access point to the `Meteor` class that can be used across `Activity` instances */
public class Meteor {

	private static final String LOG_TAG = "Fitness_Meteor";
	/** The maximum number of attempts to re-connect to the server over WebSocket */
	private static final int RECONNECT_ATTEMPTS_MAX = 5;
	/** Instance of Jackson library's ObjectMapper that converts between JSON and Java objects (POJOs) */
	private static final ObjectMapper mObjectMapper = new ObjectMapper();
	/** The WebSocket connection that will be used for the data transfer */
	private WebSocket mWebSocket;
	/** The callback that handles messages and events received from the WebSocket connection */
	private final WebSocketEventHandler mWebSocketEventHandler;
	/** Map that tracks all pending Listener instances */
	//private final Map<String, Listener> mListeners;
	/** Messages that couldn't be dispatched yet and thus had to be queued */
	//private final Queue<String> mQueuedMessages;
	private final Context mContext;
	/** Whether logging should be enabled or not */
	private static boolean mLoggingEnabled;
	private String mServerUri;
	private String mDdpVersion = "1";
	/** The number of unsuccessful attempts to re-connect in sequence */
	private int mReconnectAttempts;
	/** The callbacks that will handle events and receive messages from this client */
	//protected final CallbackProxy mCallbackProxy = new CallbackProxy();
	private String mSessionID;
	private boolean mConnected;
	private String mLoggedInUserId;
	//private final DataStore mDataStore;





	public Meteor(final Context context, final String serverUri) {

		if (context == null) {
			throw new IllegalArgumentException("The context reference may not be null");
		}

		// save the context reference
		mContext = context.getApplicationContext();
		// create a new handler that processes the messages and events received from the WebSocket connection
		mWebSocketEventHandler = new WebSocketEventHandler() {

			@Override
			public void onOpen() {
				Log.d(LOG_TAG, "  onOpen");

				mConnected = true;
				mReconnectAttempts = 0;
				initConnection(mSessionID);
			}

			@Override
			public void onClose() {
				Log.d(LOG_TAG, "  onClose");

				final boolean lostConnection = mConnected;
				mConnected = false;
				if (lostConnection) {
					mReconnectAttempts++;
					if (mReconnectAttempts <= RECONNECT_ATTEMPTS_MAX) {
						// try to re-connect automatically
						reconnect();
					}
					else {
						disconnect();
					}
				}
			}

			@Override
			public void onMessage(final WebSocketMessage message) {
				Log.d(LOG_TAG, "  onTextMessage");

				if (message.isText()) {
					Log.d(LOG_TAG, "    payload == " + message.getText());
					handleMessage(message.getText());
				}
				else {
					Log.d(LOG_TAG, "    binary");
					Log.d(LOG_TAG, "      ignored");
				}
			}

			@Override
			public void onError(final WebSocketException e) {
				Log.d(LOG_TAG, "  onError");
				e.printStackTrace();
			}

			@Override
			public void onLogMessage(final String msg) {
				Log.d(LOG_TAG, "  onLogMessage : " + msg);
			}

		};
		// save the server URI
		mServerUri = serverUri;
		// count the number of failed attempts to re-connect
		mReconnectAttempts = 0;
	}

	/** Attempts to establish the connection to the server */
	public void connect() {
		openConnection(false);
	}

	/** Manually attempt to re-connect if necessary */
	public void reconnect() {
		openConnection(true);
	}

	/**
	 * Opens a connection to the server over websocket
	 *
	 * @param isReconnect whether this is a re-connect attempt or not
	 */
	private void openConnection(final boolean isReconnect) {
		Log.d(LOG_TAG, "  openConnection : " + isReconnect);
		if (isReconnect) {
			if (mConnected) {
				initConnection(mSessionID);
				return;
			}
		}

		// create a new WebSocket connection for the data transfer
		mWebSocket = new WebSocket(URI.create(mServerUri));

		// attach the handler to the connection
		mWebSocket.setEventHandler(mWebSocketEventHandler);

		try {
			mWebSocket.connect();
		}
		catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Establish the connection to the server as requested by the DDP protocol (after the websocket has been opened)
	 *
	 * @param existingSessionID an existing session ID or `null`
	 */
	private void initConnection(final String existingSessionID) {
		Log.d(LOG_TAG, "  initConnection : " + existingSessionID);
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("msg", "connect");
		data.put("version", mDdpVersion);
		if (existingSessionID != null) {
			data.put("session", existingSessionID);
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
		Log.d(LOG_TAG, "  send");
		Log.d(LOG_TAG, "    message == " + message);

		if (message == null) {
			throw new IllegalArgumentException("You cannot send `null` messages");
		}

		if (mConnected) {
			Log.d(LOG_TAG, "    dispatching");

			if (mWebSocket != null) {
				mWebSocket.send(message);
			}
			else {
				throw new IllegalStateException("You must have called the 'connect' method before you can send data");
			}
		}
		else {
			Log.d(LOG_TAG, "    queueing");
		}
	}

	/** Disconnect the client from the server */
	public void disconnect() {

	}

	private String toJson(Object obj) {
		try {
			return mObjectMapper.writeValueAsString(obj);
		}
		catch (Exception e) {
			Log.d(LOG_TAG, "    toJson : ");
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
			Log.d(LOG_TAG, "  fromJson : ");
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
		final JsonNode data;
		try {
			data = mObjectMapper.readTree(payload);
		}
		catch (JsonProcessingException e) {
			Log.d(LOG_TAG, "  JsonProcessingException : ");
			e.printStackTrace();

			return;
		}
		catch (IOException e) {
			Log.d(LOG_TAG, "  IOException : ");
			e.printStackTrace();

			return;
		}

		if (data != null) {

		}
	}
}
