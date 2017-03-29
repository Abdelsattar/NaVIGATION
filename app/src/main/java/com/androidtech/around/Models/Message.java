package com.androidtech.around.Models;

/**
 * Created by Ahmed Donkl on 12/5/2016.
 */

public class Message {
    private String senderUid;
    private String receiverUid;
    private String message;
    private long timestamp;
    private boolean seen;

    public Message() {
    }

    public Message(String senderUid, String receiverUid, String message, long timestamp, boolean seen) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSeen() {
        return seen;
    }
}
