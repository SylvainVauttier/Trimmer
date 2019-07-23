# Trimmer

Le programme est écrit pour travailler à partir d'un répertoire racine dans lequel il cherche un sous-répertoire sdsl
contenant les décripteurs de déploiement. 

Dans le répertoire racine, il utilise un fichier puml d'entrée et un fichier puml de sortie.

Ces 3 éléments peuvent être définis par des paramètres de lancement (non pleinement testé) ou par les propriétés statiques cheatcode, cheat2, cheat3
de la classe ModelTrimmer (valeurs par défaut).

Pour produire la visualisation des modèles, il est recommandé d'augmenter la taille maximale dans PlantUML :

java -DPLANTUML_LIMIT_SIZE=8192 -jar plantuml.jar
