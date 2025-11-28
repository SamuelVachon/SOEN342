# Use Java 17 JDK base image
FROM eclipse-temurin:17-jdk

# Create working directory inside the container
WORKDIR /app

# Install wget to download MySQL JDBC driver
RUN apt-get update && apt-get install -y wget

# Download and extract MySQL Connector/J (JDBC driver)
RUN wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-9.0.0.tar.gz && \
    tar -xzf mysql-connector-j-9.0.0.tar.gz && \
    cp mysql-connector-j-9.0.0/mysql-connector-j-9.0.0.jar /app/

# Copy your CSV file into the container
COPY eu_rail_network.csv ./

# Copy your Java source files
COPY src/ ./src/

# Compile Java files into /app/out
RUN mkdir -p out && javac -cp "mysql-connector-j-9.0.0/mysql-connector-j-9.0.0.jar:." src/*.java -d out
EXPOSE 8080

# Run your main class
CMD ["java", "-cp", "out:mysql-connector-j-9.0.0/mysql-connector-j-9.0.0.jar", "driver"]
