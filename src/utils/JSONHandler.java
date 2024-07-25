package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import props.Message;
import reservations.Reservation;

public class JSONHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serialize(Message message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    public static Message deserialize(String messageString) throws JsonProcessingException {
        return objectMapper.readValue(messageString, Message.class);
    }

}
