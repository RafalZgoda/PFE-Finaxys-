package com.finaxys.utils;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * This class purpose is to build coherent "event-time" timestamps for each event that happen
 * in an ATOM simulation.
 *
 * Here is how ATOM works :
 *   - ATOM is punctuated by a tick round of talk.
 *   - At each round, all the agents are asked randomly for a decision. During two different
 *   ticks it is never the same sequence of talk.
 *   - The simulation run method have 2 parameters :
 *      1. How a day is composed
 *          - the number of ticks for opening period (which can of course be 0),
 *          - the number of ticks for the continuous period
 *          - the number of ticks for the closing period of each day)
 *      2. The number of days for this experience.
 *
 * Here is the semantics of the ATOM simulation logs
 *   - Agent log : agent;name;cash;obName;nbInvest;lastFixedPrice
 *   - Exec log : exec;nameOfTheAgentThatSendTheOrder-OrderExtId
 *   - Order log : order;obName;sender;extId;type;dir;price;quty;valid
 *   - Price log : price;obName;price;executedQuty;dir;order1;order2;bestask;bestbid
 *   - Tick log : tick;numTick;obName;bestask;bestbid;lastPrice
 *
 * We need to stamp those logs with an "event-time" timestamp in order to make sense of them.
 * What we mean by event-time is "real world time" and not the processing time.
 *
 * At the moment, only Intraday events are timestamped
 *
 *
 */
public class AtomTimeStampBuilder {

    private static Logger LOGGER = Logger.getLogger(TimeStampBuilderOld.class);

    // Useful constants used to build a timestamp
    private static final String TIME_FORMAT = "hh:mm:ss";
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final long NB_MILLI_SEC_PER_DAY = 86400000;
    private static final long NB_MILLI_SEC_PER_SECOND = 1000;

    // simulation properties from AtomInjectConfiguration
    private int nbAgents;
    private int nbOrderBooks;
    private int nbTicksIntraday;
    private int nbTicksOpeningPeriod;
    private int nbTicksClosingPeriod;

    // variables obtained from simulation properties
    private long dateBeginInMillis = 0L;
    private long marketOpenHourInMillis = 0L;
    private long marketCloseHourInMillis = 0L;

    // iterate over ticks and days
    private int currentTick = 1;
    private int currentDay = 0;

    // calculated parameters used to build the timestamp
    private long nbMillisPerTick;
    private long nbMaxOrdersPerTick;
    private long nbMillisPerOrder;

    // the result of calculation : simulate a timestamp
    private long processingTimeTimestamp;
    private long eventTimeTimestamp;


    private boolean isIntradayPeriod = false;
    private boolean isOpeningPeriod = false;
    private boolean isClosingPeriod = false;


    public AtomTimeStampBuilder(String timeZoneId, String dateBegin,
                                String marketOpenHour, String marketCloseHour,
                                int nbTicksOpeningPeriod, int nbTicksIntraday, int nbTicksClosingPeriod,
                                int nbAgents, int nbOrderBooks) {
        this.nbTicksOpeningPeriod = nbTicksOpeningPeriod;
        this.nbTicksClosingPeriod = nbTicksClosingPeriod;
        this.nbTicksIntraday = nbTicksIntraday;
        this.nbAgents = nbAgents;
        this.nbOrderBooks = nbOrderBooks;

        transformBeginDateAndOpeningClosingHoursInMillis(timeZoneId, dateBegin, marketOpenHour, marketCloseHour);

        computeParametersUsedToBuildTimestamp();
    }



    private void transformBeginDateAndOpeningClosingHoursInMillis(
            String timeZoneId, String dateBegin,
            String marketOpenHour, String marketCloseHour) {

        try {
            /* use of SimpleDateFormat objects to transform strings into
               Date objects to then get the number of millis from the date */
            SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
            dateFormatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));
            SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);
            timeFormatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));

            /* get the number of millis corresponding to the market begin date
               and the opening/closure hours */
            dateBeginInMillis = dateFormatter.parse(dateBegin).getTime();
            marketOpenHourInMillis = timeFormatter.parse(marketOpenHour).getTime();
            marketCloseHourInMillis = timeFormatter.parse(marketCloseHour).getTime();

            // assert that the conversion in millis is completed
            boolean convertionInMillisCompleted = dateBeginInMillis != 0L
                    && marketOpenHourInMillis != 0L
                    && marketCloseHourInMillis != 0L;
            assert convertionInMillisCompleted;


        } catch (ParseException e) {
            throw new InjectLayerException(
                    "Cannot convert market begin date and opening/closure hours to milliseconds ", e);
        }
    }


    private void computeParametersUsedToBuildTimestamp() {
        /* Compute the number of milliseconds for a single tick
         *   We subtract two seconds (one for the beginning and one for the end of the day)
         *   so that the events occurring in the intraday period do not have the opening time
         *   not the closing time timestamp*/
        long nbMillisInAMarketDay = (marketCloseHourInMillis - marketOpenHourInMillis) - (NB_MILLI_SEC_PER_SECOND * 2);
        nbMillisPerTick = nbMillisInAMarketDay / nbTicksIntraday;
        LOGGER.info("Number of milliseconds per Tick = " + nbMillisPerTick);

        // compute the maximum number of orders that can be issued between two ticks
        nbMaxOrdersPerTick = nbAgents * nbOrderBooks; // used to be multiplied by two but this isn't necessary
        LOGGER.info("Max number of orders by tick (nbMaxOrdersPerTick) = " + nbMaxOrdersPerTick);

        // compute the average time needed to send one single order
        nbMillisPerOrder = (nbMillisPerTick / nbMaxOrdersPerTick);
        LOGGER.info("Average time needed to send one single order (nbMillisPerOrder) = " + nbMillisPerOrder);
    }


    public long computeTimestampForCurrentTick() {
        long timeStampCurrentTick;

        if (currentTick == nbTicksIntraday) {
            timeStampCurrentTick =
                    dateBeginInMillis
                    + (currentDay - 1) * NB_MILLI_SEC_PER_DAY
                    + marketOpenHourInMillis
                    + (currentTick - 1) * nbMillisPerTick;
        }
        else {
            timeStampCurrentTick =
                    dateBeginInMillis
                    + currentDay * NB_MILLI_SEC_PER_DAY
                    + marketOpenHourInMillis
                    + (currentTick - 1) * nbMillisPerTick;
        }
        return timeStampCurrentTick;
    }



    public void setTimestampForPreOpening() {
        this.isIntradayPeriod = false;
        this.eventTimeTimestamp =  dateBeginInMillis
                + currentDay * NB_MILLI_SEC_PER_DAY
                + marketOpenHourInMillis;
        this.processingTimeTimestamp = this.eventTimeTimestamp;
    }

    public void setTimestampForOpening() {
        this.isIntradayPeriod = true;
        this.eventTimeTimestamp = dateBeginInMillis
                + currentDay * NB_MILLI_SEC_PER_DAY
                + marketOpenHourInMillis
                + NB_MILLI_SEC_PER_SECOND;
        this.processingTimeTimestamp = this.eventTimeTimestamp;
    }

    public void setTimestampForClosing() {
        this.isIntradayPeriod = false;
        this.eventTimeTimestamp = dateBeginInMillis
                + currentDay * NB_MILLI_SEC_PER_DAY
                + marketCloseHourInMillis;
        this.processingTimeTimestamp = this.eventTimeTimestamp;
    }

    public void setTimestampForNexTick() {
        // Only increment during intraday
        // If there is multiple ticks in the opening or closing period,
        // then all prices logs will have the same timestamp, which is the
        // opening/closing timestamp
        if (isIntradayPeriod) {
            this.eventTimeTimestamp += nbMillisPerTick;
            this.processingTimeTimestamp = this.eventTimeTimestamp;
        }
    }

    public void incrementProcessingTimeForNextOrder() {
        this.processingTimeTimestamp += this.nbMillisPerOrder;
    }







    public int getNbAgents() {
        return nbAgents;
    }

    public int getNbOrderBooks() {
        return nbOrderBooks;
    }

    public int getNbTicksIntraday() {
        return nbTicksIntraday;
    }

    public void setNbAgents(int nbAgents) {
        this.nbAgents = nbAgents;
    }

    public void setNbOrderBooks(int nbOrderBooks) {
        this.nbOrderBooks = nbOrderBooks;
    }

    public void setNbTicksIntraday(int nbTicksIntraday) {
        this.nbTicksIntraday = nbTicksIntraday;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public long getEventTimeTimestamp() {return eventTimeTimestamp;}

    public long getProcessingTimeTimestamp() {
        return this.processingTimeTimestamp;
    }

    public void setProcessingTimeTimestamp(long processingTimeTimestamp) {
        this.processingTimeTimestamp = processingTimeTimestamp;
    }

    public long getNbMillisPerOrder() {
        return nbMillisPerOrder;
    }

    public long getDateBeginInMillis() {
        return dateBeginInMillis;
    }

    public long getMarketOpenHourInMillis() {
        return marketOpenHourInMillis;
    }

    public long getMarketCloseHourInMillis() {
        return marketCloseHourInMillis;
    }

    public long getNbMaxOrdersPerTick() {
        return nbMaxOrdersPerTick;
    }

    public long getNbMillisPerTick() {
        return nbMillisPerTick;
    }

    public void setNbMillisPerTick(long nbMillisPerTick) {
        this.nbMillisPerTick = nbMillisPerTick;
    }


}