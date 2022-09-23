# Basic image
FROM openjdk:11

# Install dependencies and remove tmp files
RUN apt-get update && \
apt-get upgrade -y && \
apt-get install -y python3-pip maven && \
apt-get clean && \
rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Setup env variables
ENV PRJ_DIR="LPVS"

# Create project dir
RUN mkdir $PRJ_DIR

# Set workdir
WORKDIR /$PRJ_DIR

# Copy source code into container
COPY . .

# Install SCANOSS
RUN pip3 install scanoss

# Build LPVS-open-source application 
RUN mvn clean install

# Allow to listen port 7896
EXPOSE 7896

# Set workdir for running jar
WORKDIR /$PRJ_DIR/target

# Run application in container
CMD ["java", "-jar", "lpvs-1.0.1.jar"]
