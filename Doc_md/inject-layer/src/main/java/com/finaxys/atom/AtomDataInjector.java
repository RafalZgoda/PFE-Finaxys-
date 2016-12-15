package com.finaxys.atom;

import com.finaxys.utils.InjectLayerException;
import v13.Day;
import v13.Order;
import v13.OrderBook;
import v13.PriceRecord;
import v13.agents.Agent;

import java.util.Collection;

public interface AtomDataInjector {

    void closeOutput() throws InjectLayerException;

    void createOutput() throws InjectLayerException;

    void sendAgent(long processingTimeTimestamp, long eventTimeTimestamp, Agent a, Order o, PriceRecord pr)
            throws InjectLayerException;

    void sendPriceRecord(long processingTimeTimestamp, long eventTimeTimestamp, PriceRecord pr, long bestAskPrice,
                                long bestBidPrice) throws InjectLayerException;

    void sendOrder(long processingTimeTimestamp, long eventTimeTimestamp, Order o) throws InjectLayerException;

    void sendTick(long processingTimeTimestamp, long eventTimeTimestamp, Day day, Collection<OrderBook> orderbooks)
            throws InjectLayerException;

    void sendDay(long processingTimeTimestamp, long eventTimeTimestamp, int nbDays, Collection<OrderBook> orderbooks)
            throws InjectLayerException;

    void sendExec(long processingTimeTimestamp, long eventTimeTimestamp, Order o) throws InjectLayerException;

}
