SOURCES = src
TEST = test
TEST_DIR = test/game/
CLASSES = classes
DOCS = docs

LOG_FILE = util/enregistreur/partie.txt

# compil principale (Trouve et compile TOUS les .java du dossier src automatiquement)
all: 
	mkdir -p $(CLASSES)
	javac -sourcepath $(SOURCES) -d $(CLASSES) $$(find $(SOURCES) -name "*.java")

#doc
doc:
	javadoc -sourcepath $(SOURCES) -subpackages game -d $(DOCS)

#tests (Trouve et compile TOUS les tests automatiquement)
tests: all
	javac -classpath junit-console.jar:$(CLASSES) $$(find $(TEST_DIR) -name "*.java")

testAll: tests
	java -jar junit-console.jar -classpath test:$(CLASSES) -scan-classpath

###########
# executions
###########

# reflector et websocket
run-reflector:
	cd $(SOURCES)/reflector && ./reflector --port 3001

# clients console Python
run-client-alice:
	cd $(SOURCES) && python3 -m core.client ws://localhost:3001 Alice

run-client-bob:
	cd $(SOURCES) && python3 -m core.client ws://localhost:3001 Bob

# arbitre reseau
run-arbitre: all
	cd $(SOURCES) && python3 -m core.arbitre_reseau ws://localhost:3001

# outils annexes
run-enregistreur:
	cd $(SOURCES) && python3 -m util.enregistreur.Enregistreur ws://localhost:3001 $(LOG_FILE)

run-rediffuseur:
	java -classpath $(CLASSES) util.rediffuseur.Rediffuseur ws://localhost:3001 $(SOURCES)/$(LOG_FILE)
    
run-main: all
	java -classpath $(CLASSES) game.Main

# ==========================================
# INTERFACES GRAPHIQUES (NOUVEAUTES)
# ==========================================

run-gui: all
	java -classpath $(CLASSES) game.CarcassonneLauncher

run-gui-network-alice: all
	java -classpath $(CLASSES) game.CarcassonneNetworkGUI ws://localhost:3001 Alice

run-gui-network-bob: all
	java -classpath $(CLASSES) game.CarcassonneNetworkGUI ws://localhost:3001 Bob

# ==========================================

#clean
clean:
	find $(TEST) -type f -name "*.class" -delete
	find $(SOURCES) -type f -name "*.class" -delete
	rm -rf $(CLASSES)