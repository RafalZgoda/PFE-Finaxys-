In this project, you can control a certain number of the simulation parameters.  

Those parameters are located in the [`config.properties`](https://github.com/Finaxys/streaming-platform/blob/master/inject-layer/src/main/resources/config.properties) file in the `inject-layer` package.


<!-- MarkdownTOC -->

- [ATOM properties](#atom-properties)
	- [Time related properties](#time-related-properties)
	- [Agents properties](#agents-properties)
	- [OrderBook \(symbols\) properties](#orderbook-symbols-properties)
	- [Market makers properties](#market-makers-properties)
- [Kafka properties](#kafka-properties)

<!-- /MarkdownTOC -->


<a name="atom-properties"></a>
## ATOM properties

<a name="time-related-properties"></a>
### Time related properties

| Property | Signification | Default value |
|---|---|---|
| simul.days | Number of days in the simulation | 2 |
| simul.tick.opening | Number of Ticks before the beginning of the day | 1 |
| simul.tick.continuous | Number of Ticks during the day | 100 |
| simul.tick.closing | Number of Ticks after the end of the day | 1 |
| simul.time.startdate | The start date of the simulation | 09/13/2015 |
| simul.time.openhour | The market's opening hour | 9:00 |
| simul.time.closehour | The market's closing hour | 17:30 |


<a name="agents-properties"></a>
### Agents properties

| Property | Signification | Default value |
|---|---|---|
| atom.agents | Used to choose between anonymous agents or agents with names for the simulation. List of named agents is defined in the *symbols.agents.basic* property.| random |
| atom.agents.random | Number of anonymous agents to create if value of *atom.agents* property is defined to *random* | 100 |
| symbols.agents.basic | The list of agent names to use if the property "atom-agents" is set to "basic" | Paul,...,Alain |
| simul.agent.cash | Starting cash for agents | 1 |
| simul.agent.minprice | Used to build buy/sell prices when agents send orders | 1 |
| simul.agent.maxprice | Used to build buy/sell prices when agents send orders | 20 |
| simul.agent.minquantity | Used to generate quantities when agent send orders | 10 |
| simul.agent.maxquantity | Used to generate quantities when agent send orders | 50 |


<a name="orderbook-symbols-properties"></a>
### OrderBook (symbols) properties

| Property | Signification | Default value |
|---|---|---|
| atom.orderbooks | Property used to choose a certain OrderBook or use random ones (here we use DOW30 with symbols defined in symbols-orderbooks-DOW30 property) | DOW30 |
| atom.orderbooks.random | The number of random OrderBooks to create if we choose to use random order books in the simulation | 10 |
| symbols.orderbooks.DOW30 | The list of symbols for the DOW30 (an OrderBook corresponds to one symbol) | See config file |



<a name="market-makers-properties"></a>
### Market makers properties
| Property | Signification | Default value |
|---|---|---|
| atom.marketmaker | Property that manages the absence/presence of *MarketMakers* in the simulation | false |
| atom.marketmaker.quantity | Number of *MarketMakers* if *atom.marketmaker* is set to true | 10 |



<a name="kafka-properties"></a>
## Kafka properties

| Property | Signification | Default value |
|---|---|---|
| simul.output.kafka | Boolean value to whether or not enable Kafka output for the simulation | true |
| kafka.topic | The name of the Kafka topic where Atom will publish the simulations data and from which any stream processing framework will read | atomTopic |
| bootstrap.kafka.servers | A list of host/port pairs to use for establishing the initial connection to the Kafka cluster | localhost:9092 |
| kafka.quorum | ZooKeeper quorum for Kafka | localhost:2181 |