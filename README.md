##Protokolli kirjeldus
Server töötab REST põhimõtetel. Resursse on järgnevate linkide taga:

###/login (PSOT)
Sisse logimine. JSON sõnumiga saab täpsustada millise kasutajanimega sisse logida tahetakse.

Näide: `{"username":"kasutajanimi"}`

Vastuse variandid

| HTTP Kood | Sõnum | Selgitus |
| --------- | ----- | -------- |
| OK (200)  |       | Õnnestus |
| BAD REQUEST (400) | | `"username"` välja ei leitud.|
| CONFLICT (409) | | Antud kasutajanimega on keegi teine sisselogitud.|

###/game (PUT)
Kui sisselogimine õnnestub, saab uut mängu alustada. Parameetriteks:
* username - kasutajanimi
* bet - millise summaga kasutaja tahab panustada. See väli ei ole kohustuslik.

Näide: `{"username": "kasutajanimi", "bet": 200}`

Vastuse variandid

| HTTP Kood | Sõnum | Selgitus |
| --------- | ----- | -------- |
| OK (200)  | `{"gameId":0,"playerHand":"[1, 30]","dealerHand":5}` | Õnnestus. Server saatis mängija käsi ja diileri käest üks kaart. Kaardi väärtus on arv vahemikus 1-52.|
| BAD REQUEST (400) | `{"message": "Username not found"}` | `"username"` välja ei leitud.|
| FAILED DEPENDENCY (424) | `{"message": "Username not found in database"}` | Kasutaja pole sisse loginud.|
| FAILED DEPENDENCY (424) | `{"message": "Inadequate funds to start the game"}` | Kasutajal pole piisavalt raha kontol et mängu alustada.|

### /game/play (POST)
Siia saab postitada käike (Hit või Stand)
Näide: {"username": "kasutajanimi", "gameAction": "hit"}
Et mängu lõpetada, tuleks siia saata tegevus "end". Siis server lõpetab
mängu ja annab võitjale auhinnaraha.
Näide: {"username": "kasutajanimi","gameAction": "end"}

Vastuse variandid

| HTTP Kood | Sõnum | Selgitus |
| --------- | ----- | -------- |
| OK (200)  | `{"playerHand":"[1, 3, 20, 30]","dealerHand":"[5, 38, 48]"}` | Õnnestus. Server saatis mängija käsi ja diileri käest üks kaart, kui käsuks oli "hit" ja tema käe väärtus on väiksem kui 21; vastasel juhul diileri terve käsi. Kaardi väärtus on arv vahemikus 1-52.|
| BAD REQUEST (400) | `{"message": "Username not found"}` | `"username"` välja ei leitud.|
| BAD REQUEST (400) | `{"message": "gameAction not found"}` | `"gameAction"` välja ei leitud.|
| BAD REQUEST (400) | `{"message": "Cannot hit: Player already Stood."}` | Kui mängija üritab "hit" saata kui on juba "stand" saatnud. |
| BAD REQUEST (400) | `{"message": "Cannot hit: Player bust."}` | Kui mängija üritab "hit" saata kui tema käe väärtus on juba suurem kui 21. |
| FAILED DEPENDENCY (424) | `{"message": "No such username"}` | Kasutaja pole sisse loginud.|
| FAILED DEPENDENCY (424) | `{"message": "No game running"}` | Kasutaja pole mängu alustanud.|
| INTERNAL SERVER ERROR (500) |  | Server ei saanud aru sõnumist. |

### /funds (POST)
Selle lingi kaudu saab mängija lisada raha kontole või seda ära võtta.
Selleks on kaks parameetrit: "fund" ja "withdraw". Esimese puhul
lisandub see väärtus mängija kontole. Teise puhul lahutatakse.

Näide: `{"username": "kasutajanimi","withdraw": 300}`

Vastuse variandid

| HTTP Kood | Sõnum | Selgitus |
| --------- | ----- | -------- |
| OK (200)  |  | Õnnestus. |
| BAD REQUEST (400) | `{"message": "Username not found"}` | `"username"` välja ei leitud.|
| BAD REQUEST (400) | `{"message": "Transaction not found"}` | Ei `"fund"` ega `"withdraw"` välja ei leitud.|
| FORBIDDEN (403) | `{"message": "Inadequate funds."}` | Kasutajal pole piisavalt raha kontol et välja võtta. |