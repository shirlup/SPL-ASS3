package bgu.spl171.net.api.bidi;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.ConnectionHandler;


public class ConnectionsImpl<T> implements Connections<T> {
	
	private ConcurrentHashMap<Integer, ConnectionHandler<T>> mapOfPotentialClients;
	static AtomicInteger idRegistration = new AtomicInteger();
	
	//constructor
	public ConnectionsImpl(){
		mapOfPotentialClients = new ConcurrentHashMap<Integer,ConnectionHandler<T>>();
		this.idRegistration.set(1);		
	}

	@Override
	/**
	 * this function sends a message to a certain connection handler by a given key
	 * uses connection handler send
	 */
	public boolean send(int connectionId, T msg) {
		if(mapOfPotentialClients.get(connectionId) != null){
			mapOfPotentialClients.get(connectionId).send(msg);
			
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	/**
	 * this function broadcasts a message to all users.
	 *  uses connection handler send
	 */
	public void broadcast(T msg) {
		
		for (ConnectionHandler<T> values : mapOfPotentialClients.values()){
			values.send(msg);
		}
	}

	@Override
	/**
	 * this function removes the entry <Id,connectionHandler> from map
	 */
	public void disconnect(int connectionId) {

		mapOfPotentialClients.remove(connectionId); //this action closes the socket
		
		

	}
	
	public ConcurrentHashMap<Integer, ConnectionHandler<T>> getPotentialClientsMap(){
		
		return mapOfPotentialClients;
	}
	/**
	 * registers a new connection to the connections map (of all potential clients)
	 * @param handler
	 */
	public void register(ConnectionHandler<T> handler) {
		mapOfPotentialClients.put(idRegistration.get(), handler); //insert connection handler and it's id to map 
		idRegistration.getAndIncrement(); //increase the id counter
	}
	/**
	 * returns a registered connection handler's id
	 * @param connectionHandler
	 * @return
	 */
	public int getId(ConnectionHandler<T> connectionHandler) {
		for(Map.Entry<Integer, ConnectionHandler<T>> handler : mapOfPotentialClients.entrySet())
			if(handler.getValue().equals(connectionHandler)) {
				return handler.getKey();
			}
		return 0;
	}

}
