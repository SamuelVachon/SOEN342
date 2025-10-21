JAVAC = javac
JAVA  = java
SRC   = $(wildcard *.java)
CLASSES = $(SRC:.java=.class)

MAIN = driver

all: compile

compile: $(CLASSES)

%.class: %.java
	$(JAVAC) $<

run: compile
	$(JAVA) $(MAIN)

clean:
	rm -f *.class
