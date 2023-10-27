FROM node:18@sha256:a6385a6bb2fdcb7c48fc871e35e32af8daaa82c518900be49b76d10c005864c2 AS frontend

WORKDIR /frontend
COPY frontend .
RUN npm install
RUN npm run build

# Base image for building lpvs lib
FROM openjdk:11@sha256:99bac5bf83633e3c7399aed725c8415e7b569b54e03e4599e580fc9cdb7c21ab AS builder

# Install dependencies
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y maven

# Copy source code into container
WORKDIR /root/
COPY . .

# Build LPVS-open-source application
RUN mvn clean install

# Base image for running lpvs container
FROM openjdk:11@sha256:99bac5bf83633e3c7399aed725c8415e7b569b54e03e4599e580fc9cdb7c21ab

# Install dependencies and remove tmp files
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y python3-pip && \
    apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install SCANOSS
RUN pip3 install scanoss

# Allow to listen port 7896
EXPOSE 7896

# Set workdir for running jar
WORKDIR /root/
COPY --from=frontend /frontend/build/ ./static/
COPY --from=builder /root/target/lpvs-*.jar ./lpvs.jar

# Run application in container
CMD ["java", "-jar", "lpvs.jar"]
