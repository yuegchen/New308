package cse308.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class RecoloringJsonEncoder implements Encoder.Text<String>{
    private static Gson gson = new Gson();
    
    @Override
    public String encode(String message) throws EncodeException {
        return gson.toJson(message);
    }
 
    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }
 
    @Override
    public void destroy() {
        // Close resources
    }
}

