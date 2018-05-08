package cse308.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value="/redistricting", encoders=RecoloringJsonEncoder.class, decoders=JsonDecoder.class)
public class RedistrictingEndPoint {
	private Session session;
	
	@OnOpen
	public void open(Session session){
		this.session = session;
	}
	
	@OnMessage
	public String echoTest(String msg){
		return "what's up" + msg;
	}
	
	@OnClose
	public String close(Session session){
		System.out.println("close");
		return "close";
	}
	
	@OnError
	public void	handleError(Session session){
		
	}
}
