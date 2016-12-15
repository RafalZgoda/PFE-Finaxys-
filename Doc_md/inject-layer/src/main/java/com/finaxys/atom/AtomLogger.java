package com.finaxys.atom;

import com.finaxys.utils.AtomInjectConfiguration;
import com.finaxys.utils.AtomTimeStampBuilder;
import v13.Order;
import v13.OrderBook;
import v13.PriceRecord;
import v13.Day;
import v13.agents.Agent;
import v13.Logger;


public class AtomLogger extends Logger {

    private static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(AtomLogger.class);

    private AtomDataInjector injectors[];
    private AtomTimeStampBuilder tsb;
    private AtomInjectConfiguration conf;

    public AtomLogger(AtomInjectConfiguration conf, AtomDataInjector... injectors) {
        super();
        this.conf = conf;
        this.injectors = injectors;
        init();
    }

    public void init() {
        LOGGER.info("Initializing AtomLogger");
        tsb = new AtomTimeStampBuilder(
                conf.getTsbTimeZone(), conf.getTsbDateBegin(),
                conf.getTsbOpenHour(), conf.getTsbCloseHour(),
                conf.getTickOpening(), conf.getTsbNbTickMax(), conf.getTickClosing(),
                conf.getNbAgents(), conf.getNbOrderBooks()
        );

        tsb.setTimestampForPreOpening();
        for (AtomDataInjector injector : injectors) {
            injector.createOutput();
        }
    }


    @Override
    public void agent(Agent a, Order o, PriceRecord pr) {
        // Semantic of an Agent log : agent;name;cash;obName;nbInvest;lastFixedPrice
        super.agent(a, o, pr);
        long ts = 0L;
        for (AtomDataInjector injector : injectors) {
            injector.sendAgent(ts, a, o, pr);
        }
    }


    @Override
    public void exec(Order o) {
        // Semantic of an Exec log : Exec;nameOfTheAgentThatSendTheOrder-OrderExtId
        super.exec(o);
        long ts = 0L;
        for (AtomDataInjector injector : injectors) {
            injector.sendExec(ts, o);
        }
    }

    @Override
    public void order(Order o) {
        // Semantic of an Order log : order;obName;sender;extId;type;dir;price;quty;valid
        super.order(o);
        long ts = 0L;
        for (AtomDataInjector injector : injectors) {
            injector.sendOrder(ts, o);
        }
    }

    @Override
    public void price(PriceRecord pr, long bestAskPrice, long bestBidPrice) {
        // Semantic of a Price log : price;obName;price;executedQuty;dir;order1;order2;bestask;bestbid
        // Prices are logged during all periods (opening, intraday and closing)
        super.price(pr, bestAskPrice, bestBidPrice);
        long processingTimeTimestamp = tsb.getProcessingTimeTimestamp();
        long eventTimeTimestamp = tsb.getEventTimeTimestamp();
        for (AtomDataInjector injector : injectors) {
            injector.sendPriceRecord(processingTimeTimestamp, eventTimeTimestamp , pr, bestAskPrice, bestBidPrice);
        }
    }


    @Override
    public void tick(Day day, java.util.Collection<OrderBook> orderbooks) {
        // Semantic of a Tick log : tick;numTick;obName;bestask;bestbid;lastPrice
        // There is generally one tick on opening and closing period and multiple ones in intraday
        // But there can be multiple ticks in opening/closing period as well
        super.tick(day, orderbooks);
        tsb.setCurrentTick(day.currentTick());
        tsb.setProcessingTimeTimestamp(tsb.computeTimestampForCurrentTick());
        long ts = 0L;
        for (AtomDataInjector injector : injectors) {
            injector.sendTick(ts, day, orderbooks);
        }
    }

    @Override
    public void day(int numOfDay, java.util.Collection<OrderBook> orderbooks) {
        // Semantic of a Day log : day;NumDay;obName;FirstfixedPrice;LowestPrice;HighestPrice;lastFixedPrice;nbPricesFixed
        super.day(numOfDay, orderbooks);
        tsb.setCurrentDay(numOfDay);
        long ts = 0L;
        for (AtomDataInjector injector : injectors) {
            injector.sendDay(ts, numOfDay, orderbooks);
        }
    }

    public void close() throws Exception {
        for (AtomDataInjector injector : injectors) {
            injector.closeOutput();
        }
    }

}