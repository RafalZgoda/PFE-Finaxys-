

<!-- MarkdownTOC -->

- [1 - Explication générale de la simulation](#1---explication-générale-de-la-simulation)
    - [1.1 - Que se passe-t-il durant un Tick ?](#11---que-se-passe-t-il-durant-un-tick-)
    - [1.2 - Comment ATOM passe-t-il de période en période ?](#12---comment-atom-passe-t-il-de-période-en-période-)
    - [1.3 - Comment ATOM passe-t-il de jour en jour ?](#13---comment-atom-passe-t-il-de-jour-en-jour-)
- [2 - Logs pour *Ordre*, *Tick* et *Day*](#2---logs-pour-ordre-tick-et-day)
    - [2.1 - Où interviennent t'ils ?](#21---où-interviennent-tils-)
    - [2.2 - Log d'un *Ordre* en détails](#22---log-dun-ordre-en-détails)
    - [2.3 - Log d'un Tick en détails](#23---log-dun-tick-en-détails)
    - [2.4 - Log d'un Day en détails](#24---log-dun-day-en-détails)
- [3 - Log pour les *Exec*, *Agent* et *Price*](#3---log-pour-les-exec-agent-et-price)
    - [3.1 - Où interviennent t'ils ?](#31---où-interviennent-tils-)
    - [3.2 - Analyse de la fonction *OrderBook.fixingEachContinuous\(\)*](#32---analyse-de-la-fonction-orderbookfixingeachcontinuous)
    - [3.3 - Contenu des logs *Exec*, *Agent* et *Price*](#33---contenu-des-logs-exec-agent-et-price)

<!-- /MarkdownTOC -->




<a name="1---explication-générale-de-la-simulation"></a>
## 1 - Explication générale de la simulation

La simulation est lancée via la méthode *Simulation.run()* qui prend deux paramètres en entrée :

* *Day typeOfDay* qui définit le type de jour (nombre de périodes et nombre de ticks par période)
* *int numberOfDays* qui définit le nombre de jours que dure la simulation




ATOM va simuler chaque journée et itérer à travers chaque période de la journée (et itérer à travers chaque Tick de chaque période).  

Typiquement, il y a 3 périodes qui correspondent au pre-opening, à la journée de marché en elle même et à la fermeture (fixing).



<a name="11---que-se-passe-t-il-durant-un-tick-"></a>
### 1.1 - Que se passe-t-il durant un Tick ?

On a vu que la simulation itère à travers chaque journée, puis chaque période d'une journée donnée et enfin chaque Tick d'une période donnée.

Voici les actions effectuées pour chaque *Tick* : 

1. Appel à tous les agents non trader afin qu'ils broadcast leurs informations
2. Appel à tous les agents traders et pour chaque agent
    * Pour chaque *OrderBook* (symbole) du marché, demander à l'agent s'il veut décider d'envoyer un *Ordre* et le cas échant, envoyer *l'Ordre* puis **log de l'Ordre**
    * C'est lors du log d'un *Ordre* que sera lancée ou pas la procédure permettant d'exécuter l'ordre s'il y a une contrepartie qui match.  
    Cette procédure est lancée seulement durant la journée et pas pendant la pré-ouverture ou la fermeture.
3. **Log du tick** et passer au tick suivant



<a name="12---comment-atom-passe-t-il-de-période-en-période-"></a>
### 1.2 - Comment ATOM passe-t-il de période en période ?

Chaque période possède un attribut *fixingMechanism* qui permet de savoir si c'est une période de pré-ouverture/fermeture (*fixingMechanism* vaut 1) ou bien si c'est une période continue (*fixingMechanism* vaut alors 0).

Lors de chaque changement de période, ATOM met à jour le *fixingMechanism* de la *MarketPlace* en utilisant la valeur du *fixingMechanism* de la nouvelle période.  

C'est lors de cette mise à jour de la période qu'est lancée **ou non** la procédure *OrderBook.fixingEndPreopening()* sur chaque *OrderBook* de la *MarketPlace*.  
Cela permet de fixer les prix d'ouverture et de fermeture de chaque *OrderBook*.  

Cette procédure (*OrderBook.fixingEndPreopening()*) n'est lancée que : 

* Lorsqu'on passe de la période de pré-ouverture à l'ouverture réelle de la journée
* Et après la période de fermeture, quand on ferme réellement le marché avant de passer à la journée suivante.



<a name="13---comment-atom-passe-t-il-de-jour-en-jour-"></a>
### 1.3 - Comment ATOM passe-t-il de jour en jour ?

Une fois l'ensemble des périodes de la journée traitées et le marché fermé : 

* ATOM log la journée
	* Utilisation de la méthode *Logger.day(int currentDay, Collection<OrderBook> allOrderBooks)*
* Une fois le log effectué, ATOM itère à travers l'ensemble des *OrderBook*
	* Pour chaque *OrderBook*, ATOM extrait le dernier prix fixé (si un prix a été fixé) et l'ajoute à la liste des prix de l'*OrderBook* (attribut *OrderBook.extLastPrices*)
* Si la journée traitée était la dernière, alors on supprime toutes les données en mémoire et la simulation s'arrête



---

---



<a name="2---logs-pour-ordre-tick-et-day"></a>
## 2 - Logs pour *Ordre*, *Tick* et *Day*

<a name="21---où-interviennent-tils-"></a>
### 2.1 - Où interviennent t'ils ?

Les logs des ordres interviennent à chaque *Tick* et pour chaque Agent qui passe un *Ordre*.  
Un *Tick* est loggé dès lors qu'il est terminé, et il en va de même pour les logs de *Day*.



<a name="22---log-dun-ordre-en-détails"></a>
### 2.2 - Log d'un *Ordre* en détails

Pour chaque *Agent*, on passe en revue tous les *OrderBook* et pour chaque *OrderBook*, on interpelle 3 fois l'*Agent* via trois méthodes : 

* *void Agent.beforeDecide(String obName, Day day)*
    * Ne fait rien et n'est pas surchargée par les classes filles de *Agent*
    * Surement à surcharger pour coder nous même des agents intelligents
* *Order Agent.decide(String obName, Day day)*
    * C'est cette fonction qui se charge de créer ou au contraire de ne pas créer d'*Ordre* pour l'*OrderBook* en question
* *void Agent.afterDecide(String obName, Day day)*
    * Action spécifiques sur l'état de l'Agent après qu'il ait envoyé un ordre (nombre d'ordres envoyés, frozenCash ou frozenInvest, ...)


**Si l'agent a créé un *Ordre* alors il va falloir le logger.  
Voici la liste des méthodes appelées successivement pour logger un ordre :**

1. Depuis la méthode *Simulation.queryAllAgents()*, appel à *MarketPlace.send(Agent, Order)*
2. Depuis *MarketPlace.send(Agent, Order)*
    * On récupère l'*OrderBook* grâce à son nom stocké dans le paramètre *Order.obName* et on fait appel à *OrderBook.send(Order)*
3. Depuis *OrderBook.send(Order)* 
    1. *OrderBook.stampAndLog(Order)* qui s'occuper d'apposer un processing-time timestamp à l'ordre et de le logger
        * Incrémente le nombre d'ordres reçus par l'OrderBook
        * Affecte *Order.timestamp* avec la valeur de *System.currentTimeMillis()*
        * Affecte *Order.id* avec la valeur du nombre d'ordres reçus par l'*OrderBook*
        * Et enfin **log de l'Ordre** via la méthode *Logger.order(Order)*
    2. *Order.execute(OrderBook this)* où le paramètre *OrderBook* passé est *this*
	    * Selon le type d'*Ordre* (*LimitOrder*, *CancelOrder*, *UpdateOrder*) et la direction (bid ou ask) alors la fonction execute se chargera de mettre à jour la liste des ordres d'achat ou de vente de l'*OrderBook*
    3. *OrderBook.fixingEach()*
	    * Selon le type de *fixing* de la *MarketPlace* (fixe ou continu) on appellera *OrderBook.fixingEachContinuous()* ou bien *OrderBook.fixingEachPreopening()*
		    * A savoir (cf. "1.2 - Comment ATOM passe-t-il de période en période ?") que l'attribut *MarketPlace.fixing* est mis à jour à chaque changement de période en fonction de système de fixing de la période
	    * Si *fixing* vaut 0 (c'est à dire faux) alors on est en mode continu et on appelle la fonction *synchronized void OrderBook.fixingEachContinuous()* que l'on verra plus en détails dans la section des logs Exec, Agent et Price. En résumé, c'est cette méthode qui se chargera de passer en revue tous les ordres non exécutés de l'*OrderBook* et, dans le cas où le nouvel ordre créé match un de ces anciens ordres, lancer l'execution (log Agent, Exec et Price)
	    * Sinon on est en mode fixe et on appelle la fonction *void OrderBook.fixingEachPreopening()* qui ne fait rien, le travail pour l'extraday étant fait par la méthode *synchronized void OrderBook.fixingEndPreopening()*
    4. Sauvegarder le dernier ordre que l'*OrderBook* a reçu et vient de logger via l'instruction *this.lastOrder = lo*

    
Un log d'*Order* est composé de 

* *Order; obName; sender; extId; type; dir; price; quty; valid*
    * Order : Type de ligne courante, ici "Order"
    * obName : Le nom de l'OrderBook, c'est à dire du symbole
    * sender : Agent responsable de l'émission de l'ordre
    * extId : Identifiant de l'ordre, utilisé par exemple pour supprimer ou mettre à jour un ordre
    * type : nature de l'ordre(L=LimitOrder, C=CancelOrder, I=IcebergOrder, M=MarketOrder, U=UpdateOrder)
    * dir : direction de l'ordre (A=ASK (selling) and B=BUY (buying))
    * quty : quantité que l'agent choisit d'acheter/vendre
    * valid : validité de l'ordre



<a name="23---log-dun-tick-en-détails"></a>
### 2.3 - Log d'un Tick en détails

Une fois qu'on a fait appel à tous les *Agent* et que tous les *Ordre* ont été loggés, on log le *Tick* courrant via *Logger.tick(Day, Collection<OrderBook>)*.  
Dans la méthode *Logger.tick(Day, Collection<OrderBook>)*, on loggera le même tick autant de fois qu'il existe d'order books.

Un log de *Tick* est composé de

* *Tick; numTick; obName; bestask; bestbid; lastPrice*
    * Tick : Type de ligne courante, ici "Tick"
    * numTick : le numéro du Tick actuel dans la période
    * obName : le nom de l'order book
    * bestask : meilleur prix d'achat pour l'order book donné
    * bestbid : meilleur prix de vente pour l'order book donné
    * lastPrice : dernier prix fixé pour l'order book donné





<a name="24---log-dun-day-en-détails"></a>
### 2.4 - Log d'un Day en détails

Une fois que toutes les périodes de la journée sont passées et qu'on a fermé le marché via *MarketPlace.close()* on log le *Day*.  
De façon similaire, il y aura autant de log de *Day* que d'*OrderBook* dans la simulation

Un log de *Day* pour un OrderBook donné est composé de :

* *Day; NumDay; obName; FirstfixedPrice; LowestPrice; HighestPrice; lastFixedPrice; nbPricesFixed*
    * Day : Type de ligne courante, ici "Day"
    * NumDay : le numéro du jour (0 pour le premier jour)
    * obName : le nom de l'order book
    * FirstfixedPrice : premier prix fixé
    * LowestPrice : prix le plus bas de la journée
    * HighestPrice : prix le plus haut de la journée
    * lastFixedPrice : dernier prix fixé
    * nbPricesFixed : nombre de prix fixés





---

---



<a name="3---log-pour-les-exec-agent-et-price"></a>
## 3 - Log pour les *Exec*, *Agent* et *Price*

<a name="31---où-interviennent-tils-"></a>
### 3.1 - Où interviennent t'ils ?


Il existe **2 méthodes** dans la classe *OrderBook* qui ont pour but de parcourir tous les ordres d'un orderbook afin d'exécuter ceux d'entre eux qui match. Lorsque deux ordres complémentaire sont trouvés, leur exécution entraîne la fixation d'un nouveau prix.

**Ces 2 méthodes sont *fixingEach()* et *fixingEnd()*.**  

La première, ***fixingEach()***, est appelée (sur l'*OrderBook* correspondant) dès qu'un ordre est passé par un *Agent*. Son rôle consiste à choisir quelle méthode appeler ensuite en fonction de la période actuelle.  

* Si on est en période fixe (pré-ouverture ou fermeture), alors elle appellera ***fixingEachPreopening()*** qui en réalité ne fait rien.  
* Si par contre on est en période de marché (continue), alors elle appelle la méthode ***fixingEachContinuous()***. C'est cette méthode qui se chargera de passer en revue tous les ordres de l'OrderBook et faire en sorte de matcher les ordres entre eux si cela est possible. Le match de deux ordres donne alors lieu au **log du nouveau prix** (log de type *Price*), au **log de chacun des deux *Agent* émetteurs** et au **log de chacun des deux *Exec* pour peu que les ordres soient complètement exécutés** (il ne reste rien à vendre dans l'ordre de type *bid* et rien à acheter dans l'ordre de type *ask*).

La seconde, ***fixingEnd()***, fonctionne comme la première : en fonction de la période, elle appellera ***fixingEndContinuous()*** ou bien ***fixingEndPreopening()***.

* La méthode ***fixingEnd()*** est appelée sur chaque *OrderBook* à deux conditions différentes pour chaque journée
	* Au moment de la transition entre une période de type fixing et une période de type continue.  
Donc en ce qui concerne une journée classique (trois périodes ouverture-journée-fermeture), la méthode sera appelée entre la pré-ouverture et l'ouverture concrète du marché.
	* Au moment de fermeture, quand on appelle la fonction *MarketPlace.close()*
* La méthode ***fixingEndContinuous()*** ne fait rien.
* Par contre, la méthode ***fixingEndPreopening()*** va se charger, pour un *OrderBook* donné, de passer en revue les ordres *ask* et *bid* restants et de fixer un prix pour l'*OrderBook* si un prix peut être fixé (en plus de mettre à jour les informations propres relatives aux *Ordre* et *Agents* concernés, mais sans qu'il y ait de log).  
**Le seul log ayant lieu est celui du du *Price* si un prix est fixé.**  
Pour plus d'information, se référer au document joint *"fixingEndPreopeningMethod.java"* qui est une version commentée et clarifiée de la méthode ***fixingEndPreopening()*** originale d'ATOM (clarifiée signifie renommage des variables "var1, var2, ..." en des noms plus significatifs).



**En résumé**

* La création des logs pour *Exec* et *Agent* interviennent exclusivement pendant la journée dès qu'une exécution est faite (dans la méthode *synchronized void fixingEachContinuous()* de la classe *OrderBook*)
* La création des logs *Price* intervient
	* En journée dès lorsqu'un prix est fixé (c'est à dire qu'une exécution est faite)
		* Il peut y avoir plusieurs logs de *Price* pour un même *OrderBook* donné
	* Lors d'un passage d'une phase de "fixing" à une phase continue (typiquement ouverture du marché)
		* Un seul log (ou aucun) de *Price* pour un *OrderBook* donné
	* Lors de la fermeture du marché
		* Un seul log (ou aucun) de *Price* pour un *OrderBook* donné






<a name="32---analyse-de-la-fonction-orderbookfixingeachcontinuous"></a>
### 3.2 - Analyse de la fonction *OrderBook.fixingEachContinuous()*

**Tout le traitement de la méthode se fait dans une boucle *for* dont voici les instructions de contrôle :**

```java
for(
     bbid = this.bid.isEmpty()?null:(LimitOrder)this.bid.first(); 
     !this.ask.isEmpty() 
          && !this.bid.isEmpty() 
          && bask.price <= bbid.price; 
    bbid = this.bid.isEmpty()?null:(LimitOrder)this.bid.first()
)
```

* *this.ask* est instance de *TreeSet<LimitOrder>* et représente l'ensemble des ordres d'achat
* *this.bid* est instance de *TreeSet<LimitOrder>* et représente l'ensemble des ordres de vente
* *bbid* est instance de la classe *LimitOrder* et est la variable dans laquelle sont stockés les ordres de vente successifs de *this.bid*
* *bask* est instance de la classe *LimitOrder* et est la variable dans laquelle sont stockés les ordres d'achat successifs de *this.ask* (à la fin de chaque itération de la boucle for, *bask* est affecté avec la valeur suivante de *this.ask*)

**Traduite en français, les instruction de contrôle de la boucle *for* donnent :**

* Considérer successivement chaque ordre de vente et chaque ordre d'achat tant que les listes d'ordres de vente et d'achat ne sont pas vides et que le prix d'achat proposé est inférieur ou égal au prix de vente proposé.
* A la fin de chaque itération
    * On passe à l'ordre de vente suivant : instruction *bbid = this.bid.isEmpty()?null:(LimitOrder)this.bid.first()* située dans la partie *contrôle* de la boucle *for*
    * On passe à l'ordre d'achat suivant : instruction *bask = this.ask.isEmpty()?null:(LimitOrder)this.ask.first();* situé à la fin de la boucle *for*


**Que ce passe-t-il dans la boucle *for* ?**

* On affecte deux *LimitOrder* appelés *newer* et *older* en fonction de l'id de l'ordre d'achat (*bask*) et de l'id de l'ordre de vente (*bbid*) : 
    * Si l'id de l'ordre d'achat est inférieur ou égal à celui de l'ordre de vente alors *newer = ordre de vente* et *older = ordre d'achat*. Sinon *newer = ordre d'achat* et *older = ordre de vente*.
    * L'id d'un *Ordre* représente le nombre d'ordres reçus par l'*OrderBook* propriétaire de l'*Ordre* au moment où l'*Ordre* est envoyé.
    * En résumé, *newer* représente l'ordre le plus récent, celui qui déclenche l'execution, et *older* représente l'ordre qui était en pending et attendait un ordre complémentaire
* On met à jour les informations des agents car leurs ordres d'achat/vente vont être exécutés :
    * Mise à jour de leur cash ainsi que leur liste d'investissements
* On cherche la plus petite quantité proposée par l'ordre d'achat et de vente car c'est cette quantité qui sera échangée :
    * Si le vendeur vent 100 actions et l'acheteur en veut 50, alors 50 actions seront échangées
    * Si au contraire le vendeur vent 100 actions et l'acheteur en veut 200, alors 100 actions seront échangées
* On va ensuite logger
	* Le nouveau *Price*
	* L'*Exec* du vendeur si l'*Ordre* est complètement exécuté (toute la quantité de l'*Ordre* a été vendue)
	* L'*Exec* de l'acheteur si l'*Ordre* est complètement exécuté (toute la quantité de l'*Ordre* a été achetée)





<a name="33---contenu-des-logs-exec-agent-et-price"></a>
### 3.3 - Contenu des logs *Exec*, *Agent* et *Price*

* Exec
	* *Exec; agentSenderName-orderExtId*
        * Exec : Type de ligne courante, ici "Exec"
        * agentSenderName-orderExtId : nom de l'agent qui a envoyé l'ordre et id de l'ordre

* Agent
	* *Agent; name; cash; obName; nbInvest; lastFixedPrice*
        * Agent : Type de ligne courante, ici "Agent"
        * name : nom de l'Agent
        * cash : le cash de l'agent
        * obName : l'OrderBook qui vient d'être modifié (log Agent = match de deux ordres ask-bid)
        * nbInvest : quantité de l'order book donné possédée par l'Agent
        * lastFixedPrice : dernier prix fixé pour l'order book
* Price
	* *Price; obName; price; executedQuty; dir; order1; order2; bestask; bestbid*
        * Price : Type de ligne courante, ici "Price"
        * obName : l'OrderBook qui vient d'être modifié (log Price = match de deux ordres ask-bid)
        * price : le prix généré par le match de deux ordres
        * executedQuty : la quantité échangée
        * dir : direction générée par le prix
        * order1 : identifiant de l'ordre générant le prix
        * order2 : identifiant de l'ordre qui match avec le premier ordre
        * bestask : le meilleur prix actuel d'achat
        * bestbid : le meilleur prix actuel de vente