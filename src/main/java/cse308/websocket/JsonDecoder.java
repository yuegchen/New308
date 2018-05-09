package cse308.websocket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class JsonDecoder implements Decoder.Text<String>{
    private static Gson gson = new Gson();
    
    @Override
    public String decode(String message) throws DecodeException {
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

	@Override
	public boolean willDecode(String arg0) {
		return (arg0 != null);
	}
}
