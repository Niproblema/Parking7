package com.niproblema.parking7.DataObjects.Chat;

import com.niproblema.parking7.DataObjects.DataObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Chat implements Serializable, DataObject {
	// ClientSide
	public final String mInitiatorUID;
	public final String mReceiverUID;
	public final List<ChatMessage> mMessages;

	public Chat(String initiatorUID, String receiverUID, List<ChatMessage> messages) {
		this.mInitiatorUID = initiatorUID;
		this.mReceiverUID = receiverUID;
		this.mMessages = messages;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>() {{
			put("initiatorID", mInitiatorUID);
			put("receiverUID", mReceiverUID);
			put("messages", mMessages);
		}};
	}
}
