*1/ How to make a reference to ATOM*

Philippe Mathieu and Olivier Brandouy
A Generic Architecture for Realistic Simulations of Complex Financial
Dynamics
in Advances in Practical Applications of Agents and Multiagent Systems,
8th International conference on Practical Applications of Agents and
Multi-Agents Systems (PAAMS'2010), pages 185-197, Salamanca (Spain),
26-28th April, 2010

Philippe Mathieu and Olivier Brandouy
Efficient Monitoring of Financial Orders with Agent-Based Technologies
in Proceedings of the 9th International conference on Practical
Applications of Agents and Multi-Agents Systems (PAAMS'2011), pages
277-286, Salamanca (Spain) -- 6th-8th April 2011

Philippe Mathieu and Olivier Brandouy
Introducing ATOM
in Proceedings of the 10th International conference on Practical
Applications of Agents and Multi-Agents Systems (PAAMS'2012), Salamanca
(Spain) -- 28th-30th March 2012

*2 Architecture */

ATOM is a Java API with which you can build any kind of experiments on
order-driven markets like NYSE-Euronext.
ATOM contains a Market on which you can add any number of double auction
orderbooks, one for each asset (option) you want to trade. It also
manages some artificial agents that you may have to build and add to
your experiments. We give in this package few basic agents like the
classic Zero Intelligent Trader (ZIT). The given agents are able to
trade simultaneously on all the orderbooks added in the Market.
in ATOM an agent is just an empty shell. You have to plug
your own strategy. You just have to implement a specific method called
by the market when it is your time to talk, and you just have to return
an order or null if you decide to do noting at this time. This method is
called decide(String obName, int day, int tick).

During an experiment, all the events can be logged. This aspect
facilitates all the statistics you could need, curves you could have to
plot or stylized facts you could have to verify. This log file can also
be integrally replayed with the ATOM flow replayer, not only able to
send once again the orders, but also able to re-create all the agents
asking all of them to re-send their own orders in the same scheduling.
In ATOM we are using mainly two agent-based engines :
- fair, one-flow ; which gives us the possibility to run experiments
extremely fast
- unfair, multi-flows ; which gives us the possibility to have human in
the loop
Last but not least, ATOM is able to work both in Intraday or Extraday
configuration, with two fixing prices methods (fix and continuous). When
you run several days, each day is a real day, built on the Intraday
configuration.


*3/ Is it user friendly ?*

ATOM comes with a GUI for generating orders and prices, and a  GUI for
the replay engine. It's sufficient for many people but not for
everybody. But ATOM is mainly an Application Programming Interface
(API). The number of possible experiences you may need is so large that
only a basic user interface is available. Since ATOM is mainly an API,
you can write Java code to design your own experience.
You need basic coding skills in OO programming in order
to define your own trading strategies.

*4/ Install*

You just need to have atom.jar on your disk and a "lib" directory
at the same place. In "lib" you need swing-layout.jar

mydir
    |- atom.jar
    |- lib
         |- swing-layout-1.0.4.jar

*5/ Run*

You should have Java (version 1.5 at least) installed on your
system (freely available here:
http://www.oracle.com/technetwork/java/javase/downloads).

You have two main commands given with ATOM : Generate and Replay
Double click on atom.jar ... you obtain a graphical user interface
which offers to generate a file or replay a file.
Generate contains many parameters and is able to launch a script to
analyse or plot immediately the data obtained (use R, Gnuplot for ex)

It is also possible to use it in a shell script or command line :

   java -cp atom.jar v13.gui.MenuScreen .... gives the same GUI
   java -cp atom.jar v13.Replay         .... gives the Replay GUI
   java -cp atom.jar v13.Generate       .... gives the Generate GUI

   java -cp atom.jar v13.Replay orderFileExample1 ... flow replayer
   java -cp atom.jar v13.Generate 10 1 1000 ... flow generator for
one day


*6/ How Build my own experiment*

*6.1/ Intraday, No strategy

If you just want to send orders and see what append to the agent or the
market, you can easily build your own experiment. You can use Continuous
or FIX price mechanism, you can use a long fixing method or a short one,
you can have a log or not and any Dumb agent you need.

import v13.*;
import v13.agents.*;

public class TestATOMv13
{
    public static void main(String args[])
    {
    Simulation sim = new MonothreadedSimulation();
          sim.market.setFixingPeriod (MarketPlace.CONTINUOUS); // or FIX
        sim.market.logType=MarketPlace.LONG; // or SHORT
        sim.setLogger(new Logger(System.out)); // or FilteredLogger
        // sim.setLogger(new Logger("/tmp/test.atom"));
    // default null, thus nothing

    String obName="lvmh";
    sim.addNewOrderBook(obName);
    Agent a = new DumbAgent("paul");

    Order orders[] ={
    new IcebergOrder(obName,"g",LimitOrder.ASK, 3, 10, (long) 200),
    new MarketOrder(obName,"h",LimitOrder.BID, 8),
    new LimitOrder(obName,"i",LimitOrder.BID, 2, (long) 210)
    };

        for (int i = 0; i < orders.length; i++)
            sim.market.send(a,orders[i]);

    sim.market.printState();
    sim.market.close();
    }
}


*6.2/ Strategies

If you need to see agents behaviors in action, the easiest way is to use
predefined agents. There are several predefined agenst in ATOM like the
lassic Zero Intelligence Trader ZIT. You can then use it and build build
an experiment either in Intraday or in Extraday during a specific amount
of rounds.

import v13.*;
import v13.agents.ZIT;
public class Test
{
    public static void main(String args[])
    {
        Simulation s = new MonothreadedSimulation();
       
        // sim.market.setFixingPeriod(MarketPlace.FIX);
        // sim.market.setFixingPeriod(MarketPlace.CONTINUOUS);
        // default is CONTINUOUS
       
            // sim.MarketPlace.logType=MarketPlace.SHORT;
            // sim.MarketPlace.logType=MarketPlace.LONG;
        // default is LONG
       
                sim.setLogger(new Logger(System.out));
                // sim.setLogger(new Logger("/tmp/test.atom"));
        // default null, thus nothing

        sim.addNewOrderBook("lvmh");
        sim.addNewAgent(new ZIT("paul",10000));
        sim.addNewAgent(new ZIT("john",10000));
       
        sim.run(Day.createEuroNEXT(100,1000,100), 5);
        // 5 days. 100 ticks for opening period, 1000 ticks for
        // continuous period and 100 closing period each day.

        sim.market.printState();
        sim.market.close();
        // don't forget to close ! specially when it is FIX
    }
}


*7/Informant Agents
ATOM let you build experiments with an InformatAgent that will decide to send a specific information to all the agents (like an earthquake, good or bad news about assets , etc ..) and all the other agents who will build their behaviour on these information (or lack of information)

To do that you have to use a specific Informant Agent who send news. An "InformantAgent" have to implement the method "broadcastNews" which will call the "news" method of each trading agent with the information (any kind of object) he decide to notify.

"news is a method called by "InformantAgents" on traders to inform them about any kind of external information.
Trading Agents have then to implement "news" which code what they want
do do with this information, and of course "decide" which is called by
the market for each asset.



*8/Abstract

Three basic works with ATOM

First one : Generate and analyze data
---------

> java -cp v13.jar v13.Generate 10 1 1000
Syntax: <nbAgents> <nbOrderbooks> <nbTurns>

You then have a log file that you can analyse
You can see the orders sent, the prices, the agents's states etc ...

Use Grep for example to filter lines
> java -cp v13.jar v13.Generate 10 1 1000 > myfile
> grep Price toto
> grep Agent toto | grep ZIT1

Second one : replay files
----------
> java -cp v13.jar v13.Replay orderFileExample1

See this file. It is really easy to build your own.
but you can also replay exactly a file with several agents
as one obtained with Generate

> java -cp v13.jar v13.Generate 10 1 1000 > myfile1
> java -cp v13.jar v13.Replay myfile 1 > myfile2

Third one : Write your own code
---------
See Tutorial.java


*9/ Frequently Asked Questions*

- Why ATOM is powerful ?
You can study micro-structure, agent behaviours, orders influence on
prices, agent's wealth, or any kind of research questions about these
markets. You can then build any experiment with your own trading
strategies. Last but not least, ATOM is really efficient.

- What are ATOM main functionalities ?
Double auction orderbook close to Nyse-Euronext
Multi orderbooks
Multi-Agents system
Order Driven architecture or Price driven architecture
New trading strategies can easily be integrated
Intraday or Extraday functionalities
All kind of NYSE orders (Limit, Market, Iceberg, Cancel, Update, ...)
Synchronous or threaded execution
Powerful execution trace about orders, agents, prices, ticks, days....
Multiagents Flow Replayer

- How exactly does it run ?
ATOM is rythmed by a tick round of talk. At each round, all the agents
are asked randomly for a decision. During two different ticks it is
never the same sequence of talk. Of course, when asked, an agent can
decide to do nothing, but it is an equitable principle of trade. You can
dedide to have less verbose agents, extra-day agents or whatever you want :
when you write an agent you decide when, how, and what it will do.
The run method have 2 parameters : how a day is composed (the number of
ticks for opening period (which can of course be 0), the number of ticks
for the continuous period and the number of ticks for the closing period
of each day), and then the number of days for this experience.
        sim.run(Day.createEuroNEXT(100,1000,100) , 5);
        // 5 days. 100 ticks for opening period, 1000 ticks for
        // continuous period and 100 closing period each day.


- What are the predefined agents ?
At this time,
ZIT , Zero Intelligent Trader who sends random LimitOrders
ZIT_MO , ZIT Multi Orders, sends any kind of order
IntelligentAGent , Manages its cash and pendings
Multiassets arbitraging strategies
HumanAgent , Interface agent for human players

And many technical traders (Periodic, MovingAverage etc ...)
But you can easily integrate your own trading strategies.


- What are the semantic of the log lines :

order;obName;sender;extId;type;dir;price;quty;valid
price;obName;price;executedQuty;dir;order1;order2;bestask;bestbid
agent;name;cash;obName;nbInvest;lastFixedPrice
day;NumDay;obName;FirstfixedPrice;LowestPrice;HighestPrice;lastFixedPrice;nbPricesFixed
tick;numTick;obName;bestask;bestbid;lastPrice

You have also some lines beginning with "info;". They are about
execution information like ticks, days, executed or destroyed orders

Thus to plot an intraday price curve use 7th price column
To plot an extraday price curse use the 4th day column

- How to run a vertuous loop between Replay and Generate ?
ATOM is able to Replay exactly what it has generated, even the agents,
the orderbooks and the commands.
    java -cp atom.jar v13.Generate 10 3 1000 > myfile1.txt
    java -cp atom.jar v13.Replay myfile1.txt > myfile2.txt
    wc myfile*
    diff myfile1.txt myfile2.txt
You can do it also with the GUI if you want.

- How to filter information ?
The best way is to use grep or awk unix tools (unix,linux,macOS) with a
regular expression.

    grep '^Day;' marketsim.atom
    grep '^Agent;ZIT1' marketsim.atom
    awk -F';' '/^Agent;ZIT1/{print $3+($5*$6);}' marketsim.atom   

Of course you can also filter the output of the flowReplayer by the same way

    java -cp atom.jar v13.Replay orderFileExample3 | grep '^Price'
    java -cp atom.jar v13.Replay orderFileExample3 | grep '^Day'


- How the agent knows which day we are ?
it is a decide parameter : decide(obName,day,tick)

- How the agent knows which tick we are ?
it is a decide parameter : decide(obName,day,tick)

- How the agent knows the currently best ask offer ?
market.orderBooks.get(obName).ask.first().price

- How the agent knows the currently best bid offer ?
market.orderBooks.get(obName).bid.first().price

- How the agent knows the last fixed price ?
market.orderBooks.get(obName).lastFixedPrice.price

- How an agent can obtain the last fixed prices ?
lastFixedPrice always contains the last fixed price.
The list LasPrices contains n last prices fixed, including the last one.
lastFixedPrice.price = lastPrices.get(0).price The most recent one is
at the head of this list
To display all the previous prices :
for (PriceRecord p : sim.market.orderBooks.get(obName).lastPrices) {
            System.out.println(p.price+" "+p.quantity);
}

- How to filter the log, specially for extraday ?
By default, nothing is logged. If you use the basic Logger, all is logged
in the flow given in parameter.
    sim.setLogger(new Logger(System.out));
or
    sim.setLogger(new Logger("/tmp/test.atom"));
or
    sim.setLogger(null);

But there is also a special FilteredLogger which allows to select what
to log.
        FilteredLogger flog = new FilteredLogger(System.out);
        flog.orders = true;
        flog.prices = true;
        flog.agents = false;
        flog.infos = false;
        flog.commands = false;
        sim.setLogger(flog);


- How to write an Agent
Easy ! just extend Agent, write the decision procedure, and add it to
the simulation.

class MyAgent extends Agent {
    MyAgent(String name, long cash){
        super(name, cash);
    }

    public Order decide(String obName, int day, int tick){
        OrderBook ob = market.orderBooks.get(obName);
        // use for example
        // ob.numberOfPricesFixed
    // ob.lastFixedPrice.price
    // ob.lastPrices.get(i).price;
        // ob.ask.first().price
        // ob.bid.first().quantity
        return null; // or any order
    }
}

- How initialize the price of an OrderBook ?

Two possibilities : You can add manually a PriceRecord

OrderBook ob = new OrderBook(name);
...
ob.setNewPrice(new PriceRecord(name,210,1,'A',null,null));
...
System.out.println(ob.numberOfPricesFixed+" "+ob.lastFixedPrice.price);


But in ATOM we consider that all have to be done with agents. The most
correct way is to use an agent sending symetric orders to fix a price :

Agent a = new DumbAgent("paul");
m.send(a,new LimitOrder(obName,"0",LimitOrder.BID, 1, (long) 210));
m.send(a,new LimitOrder(obName,"0",LimitOrder.ASK, 1, (long) 210));
...
System.out.println(ob.numberOfPricesFixed+" "+ob.lastFixedPrice.price);



- How to build a multiThreaded simulation ?

Simulation sim = new MultithreadedSimulation();

In such simulation you need real time. Thus you can adjust your
simulation using two variables.
sim.tempo who indicates the time used for a round of talk in millisec
and for each agent a.speed who indicates the rythm of the agent in round
of talks.
Take care of that : tempo is in millisec, speed is in round of talks.

For example, if you need several speeds for all your agents, compute
first the gcd of all the speeds. This will give you the tempo.
Then adjust the speed of each agent dividing the speed wanted with the
tempo.

Here is an example for a simulation running during 1mn, with 4 agents at
different speeds, the agent i running each i sec.

public class TestThreads
{
    public static void main(String args[])
    {
        Simulation sim = new MultithreadedSimulation();
        sim.setLogger(new Logger(System.out));

        sim.tempo = 1000; // gcd = 1 sec for one round

        sim.addNewOrderBook("lvmh");
        for (int i = 1; i <= 4; i++)
        {
            Agent z = new ZIT("zit_" + i, 10000);
            z.speed =  i; // talk each "speed" round
            sim.addNewAgent(z);
        }

        sim.run(Day.createEuroNext(0, 60, 0) , 1); // 60 rounds of 1 sec

        sim.market.close();

    }
}

- How to plot Bollinger's bands in Extraday
First, execute your experience during several days by using
sim.run(Day.createEuroNext(0, 60, 0) , 50); // 50 days

Then filter the result to keep days information
java -cp atom.jar MyExp | grep '^Day' > data

Then plot bollinger's bands with gnuplot
set datafile separator ";"
plot "data" using :4:5:6:7 with financebars
