package com.niproblema.parking7.DataObjects.Chat;

import com.niproblema.parking7.DataObjects.DataObject;

import java.io.Serializable;
import java.util.HashMap;

public class ChatMessage implements Serializable, DataObject {
	// ClientSide
	public final String mSenderUID;
	public final String mText;

	//ServerSide
	public boolean mReceived;
	public long mTimeStamp;

	public ChatMessage(String senderUID, String text) {
		this.mSenderUID = senderUID;
		this.mText = text;
	}

	@Override
	public HashMap<String, Object> getSubmittableObject() {
		return new HashMap<String, Object>(){{
			put("senderUID", mSenderUID);
			put("text", mText);
		}};
	}
}
