JAVAC = javac
JAVA  = java
SRC   = $(wildcard *.java)
CLASSES = $(SRC:.java=.class)

# MySQL connector JAR path (relative to src directory)
MYSQL_JAR = ../mysql-connector-j-9.1.0.jar
# CSV file path
CSV_FILE = ../eu_rail_network.csv
# SQL init file
SQL_INIT = ../init.sql
# Docker container name
DB_CONTAINER = soen342_db
# Docker compose file location
COMPOSE_FILE = ..

MAIN = driver

all: compile

compile: $(CLASSES)

%.class: %.java
	$(JAVAC) -cp ".:$(MYSQL_JAR)" $<

# Initialize database tables
init-db:
	@echo "Initializing database tables..."
	@docker exec -i $(DB_CONTAINER) mysql -u user -ppass train_system < $(SQL_INIT)
	@echo "Database initialized successfully!"

# Check if tables exist
check-db:
	@echo "Checking database tables..."
	@docker exec -i $(DB_CONTAINER) mysql -u user -ppass train_system -e "SHOW TABLES;"

# Clean database (remove all data but keep tables)
clean-db:
	@echo "Wiping all data from database tables..."
	@docker exec -i $(DB_CONTAINER) mysql -u user -ppass train_system -e "SET FOREIGN_KEY_CHECKS = 0; TRUNCATE TABLE Reservation; TRUNCATE TABLE Trip; TRUNCATE TABLE Customer; SET FOREIGN_KEY_CHECKS = 1;"
	@echo "Database cleaned! All data removed."

# Run with database check
run: compile
	@echo "Checking if database is ready..."
	@docker exec $(DB_CONTAINER) mysql -u user -ppass train_system -e "SHOW TABLES;" > /dev/null 2>&1 || (echo "Database tables not found. Run 'make init-db' first." && exit 1)
	$(JAVA) -cp ".:$(MYSQL_JAR)" $(MAIN) $(CSV_FILE)

# Run with automatic database initialization
run-auto: compile init-db
	$(JAVA) -cp ".:$(MYSQL_JAR)" $(MAIN) $(CSV_FILE)

# Docker database management
start-db:
	@echo "Starting database container..."
	@cd $(COMPOSE_FILE) && docker-compose up -d
	@echo "Database container started!"

stop-db:
	@echo "Stopping database container..."
	@cd $(COMPOSE_FILE) && docker-compose down
	@echo "Database container stopped!"

restart-db:
	@echo "Restarting database container..."
	@cd $(COMPOSE_FILE) && docker-compose restart
	@echo "Database container restarted!"

db-status:
	@echo "Database container status:"
	@docker ps --filter "name=$(DB_CONTAINER)" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

clean:
	rm -f *.class

.PHONY: all compile run run-auto init-db check-db clean-db start-db stop-db restart-db db-status clean
