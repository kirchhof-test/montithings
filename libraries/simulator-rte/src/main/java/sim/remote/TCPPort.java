/**
 * 
 */
package sim.remote;

import java.util.LinkedList;
import java.util.List;

import sim.generic.TickedMessage;
import sim.port.Port;

/**
 * Port that transmits over tcp. <br>
 * <br>
 * Copyright (c) 2011 RWTH Aachen. All rights reserved.
 * 
 * @author (last commit) $LastChangedBy: ahaber $
 * @version $LastChangedDate: 2014-08-31 16:58:17 +0200 (So, 31 Aug 2014) $<br>
 * $LastChangedRevision: 2972 $
 * @param <T>
 */
public class TCPPort<T> extends Port<T> implements IInTCPPort<T>, IOutTcpPort<T> {
    
    private boolean isTcpBased = false;
    
    private final List<OutPortTCPServer<T>> senderServer;
    
    private final List<InPortTCPServer<T>> inputServer;
    
    /**
     * Constructor for sim.remote.TCPPort.
     */
    public TCPPort() {
        senderServer = new LinkedList<OutPortTCPServer<T>>();
        inputServer = new LinkedList<InPortTCPServer<T>>();
    }
    

    
    /* (non-Javadoc)
     * 
     * @see sim.port.Port#accept(sim.generic.TickedMessage) */
    @SuppressWarnings("unchecked")
    @Override
    public void accept(TickedMessage<? extends T> message) {
        if (isTcpBased) {
            TickedMessage<T> tickedMessage = (TickedMessage<T>) message;
            send(tickedMessage);
        }
        else {
            doAccept(message);
        }
    }
    
    private synchronized void doAccept(TickedMessage<? extends T> message) {
        super.accept(message);
    }
    
    /* (non-Javadoc)
     * 
     * @see sim.generic.IOutgoingPort#sendMessage(sim.generic.ITimedMessage) */
    @Override
    public void send(TickedMessage<T> message) {
        if (isTcpBased) {
            for (OutPortTCPServer<T> sever : senderServer) {
                sever.sendMessage(message);
            }
        }
        else {
            super.send(message);
        }
    }
    
    
    /**
     * @see sim.remote.IOutTcpPort#addReceiver(java.lang.String, int)
     */
    @Override
    public void addReceiver(String address, int tcpPort) {
        isTcpBased = true;
        RemoteReceiverConfig cfg = new RemoteReceiverConfig(address, tcpPort);
        OutPortTCPServer<T> server = new OutPortTCPServer<T>(cfg);
        new Thread(server, this.toString()).start();
        senderServer.add(server);
    }
    
    /**
     * @see sim.remote.IInTCPPort#startListenOn(int)
     */
    @Override
    public void startListenOn(int tcpPort) {
        InPortTCPServer<T> s = new InPortTCPServer<T>(this, tcpPort);
        new Thread(s, this.toString()).start();
        inputServer.add(s);
    }
    
    public void stop() {
        for (OutPortTCPServer<T> o : senderServer) {
            o.stop();
        }
        for (InPortTCPServer<T> i : inputServer) {
            i.stop();
        }
    }
    
}
