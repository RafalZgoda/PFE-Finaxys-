


* [1 - Timeline des logs au cours d'une simulation](#1---timeline-des-logs-au-cours-dune-simulation)
* [2 - Anatomie des différents logs](#2---anatomie-des-différents-logs)
    * [2.1 - Order](#21---order)
    * [2.2 - Tick](#22---tick)
    * [2.3 - Day](#23---day)
    * [2.4 - Exec](#24---exec)
    * [2.5 - Agent](#25---agent)
    * [2.6 - Price](#26---price)

<a name="1---timeline-des-logs-au-cours-dune-simulation"></a>
## 1 - Timeline des logs au cours d'une simulation

D'après l'analyse du fonctionnement interne d'ATOM (cf. [[Atom Simulation Anatomy]]), voici un schéma permettant de résumer la timeline des logs d'ATOM au cours de la simulation avec une journée de type ouverture-journée-fermeture.

* Opening (période *fixe*)
    * Log des *Prices* si des prix peuvent être fixés
* Journée (période *continue*)
    * Log des *Ordres*
        * Si au cours du log d'un nouvel ordre deux ordres complémentaires match alors
            * Log du nouveau *Price*
            * Log *Agent* deux fois (pour chaque Agent acheteur/vendeur)
            * Si le nouvel ordre et/ou l'ancien sont complètement exécutés alors
                * Log *Exec* pour chaque ordre complètement exécuté
    * Fin du *Tick* et log de ce dernier avant de passer au *Tick* suivant et recommencer le log des ordres
        * Il y a un log de Tick pour chaque OrderBook de la simulation
* Closure (période *fixe*)
    * Log des *Prices* si des prix peuvent être fixés
* Fin de la journée et log du *Day* puis passer à la journée suivante s'il en reste
    * Il y a un log de Day pour chaque OrderBook de la simulation


<a name="2---anatomie-des-différents-logs"></a>
## 2 - Anatomie des différents logs

<a name="21---order"></a>
### 2.1 - Order

**"Order; obName; sender; extId; type; dir; price; quty; valid"**

* **Order** : Type de ligne courante, ici "Order"
* **obName** : Le nom de l'OrderBook, c'est à dire du symbole
* **sender** : Agent responsable de l'émission de l'ordre
* **extId** : Identifiant de l'ordre, utilisé par exemple pour supprimer ou mettre à jour un ordre
* **type** : nature de l'ordre(L=LimitOrder, C=CancelOrder, I=IcebergOrder, M=MarketOrder, U=UpdateOrder)
* **dir** : direction de l'ordre (A=ASK (selling) and B=BUY (buying))
* **quty** : quantité que l'agent choisit d'acheter/vendre
* **valid** : validité de l'ordre

<a name="22---tick"></a>
### 2.2 - Tick

**"Tick; numTick; obName; bestask; bestbid; lastPrice"**

* **Tick** : Type de ligne courante, ici "Tick"
* **numTick** : le numéro du Tick actuel dans la période
* **obName** : le nom de l'order book
* **bestask** : meilleur prix d'achat pour l'order book donné
* **bestbid** : meilleur prix de vente pour l'order book donné
* **lastPrice** : dernier prix fixé pour l'order book donné


<a name="23---day"></a>
### 2.3 - Day

**"Day; NumDay; obName; FirstfixedPrice; LowestPrice; HighestPrice; lastFixedPrice; nbPricesFixed"**

* **Day** : Type de ligne courante, ici "Day"
* **NumDay** : le numéro du jour (0 pour le premier jour)
* **obName** : le nom de l'order book
* **FirstfixedPrice** : premier prix fixé
* **LowestPrice** : prix le plus bas de la journée
* **HighestPrice** : prix le plus haut de la journée
* **lastFixedPrice** : dernier prix fixé
* **nbPricesFixed** : nombre de prix fixés


<a name="24---exec"></a>
### 2.4 - Exec

**"Exec; agentSenderName-orderExtId"**

* **Exec** : Type de ligne courante, ici "Exec"
* **agentSenderName**-orderExtId : nom de l'agent qui a envoyé l'ordre et id de l'ordre


<a name="25---agent"></a>
### 2.5 - Agent

**"Agent; name; cash; obName; nbInvest; lastFixedPrice"**

* **Agent** : Type de ligne courante, ici "Agent"
* **name** : nom de l'Agent
* **cash** : le cash de l'agent
* **obName** : l'OrderBook qui vient d'être modifié (log Agent = match de deux ordres ask-bid)
* **nbInvest** : quantité de l'order book donné possédée par l'Agent
* **lastFixedPrice** : dernier prix fixé pour l'order book

<a name="26---price"></a>
### 2.6 - Price

**"Price; obName; price; executedQuty; dir; order1; order2; bestask; bestbid"**

* **Price** : Type de ligne courante, ici "Price"
* **obName** : l'OrderBook qui vient d'être modifié (log Price = match de deux ordres ask-bid)
* **price** : le prix généré par le match de deux ordres
* **executedQuty** : la quantité échangée
* **dir** : direction générée par le prix
* **order1** : identifiant de l'ordre générant le prix
* **order2** : identifiant de l'ordre qui match avec le premier ordre
* **bestask** : le meilleur prix actuel d'achat
* **bestbid** : le meilleur prix actuel de vente