# KuerbissKraftPlugin
Game-Management-Plugin für das Minecraft Projekt Kürbisskraft im Hungergames Stil

Dateien:
```
plugins
│  KuerbissKraft-1.0-SNAPSHOT-all.jar
│
└───KuerbissKraft
    │  Advancements.json
    │  Blacklist.txt
    │  Entities.json
    │  Players.json
```

### Advancements.json
Enthält ein JSON-Objekt in welchem der Titel zu den NamespaceKeys der Advancements steht. 
Wird benutzt für Meldungen wie: "Ein Spieler hat den Erfolg [Oh Shiny] erziehlt". 

### Blacklist.txt
Enthält eine Reihe von Ausdrücken die im Chat gesperrt werden sollen, diese stehen im RegEx-Syntax und sind Case Insensitive. 
```
du\s+bist\s+blöd
minecraft\s+ist\s+doof
```

### Entities.json
Hier wird festgehalten welche Truhe welchem Team gehört. 

### Players.json
Muss vor Begin gefüllt sein!
Hier stehen alle Infos über einen Spieler in einem JSON-Array. 
Pro Team sind genau zwei Spieler vorgesehen. 
```
[
  {
    "id": "der_kuerbiss",
    "teamName": "groot",
    "strikes": 0,
    "lastLogout": -1,
    "dayPlayTime": 0,
    "textures": "mizunos16craft",
    "alive": true
  }
]
```
