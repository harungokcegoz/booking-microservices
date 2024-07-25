package props;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("type")
    private MessageType type;
    @JsonProperty("payload")
    private Object payload;


    /**
     * Creates one more empty constructor for successful serializing.
     */
    public Message() {
    }

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", payload=" + payload +
                '}';
    }
}

