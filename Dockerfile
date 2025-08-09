FROM node:24@sha256:dd5c5e4d0a67471a683116483409d1e46605a79521b000c668cff29df06efd51 AS frontend

WORKDIR /frontend
COPY frontend .
RUN npm ci
RUN npm run build

# Base image for building lpvs lib
FROM openjdk:18-slim@sha256:8e17383576d7e71988ee5927473a32e8461381c7a29eefa9a0c24b3a28926272 AS builder

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
FROM openjdk:18-slim@sha256:8e17383576d7e71988ee5927473a32e8461381c7a29eefa9a0c24b3a28926272

# Install dependencies and remove tmp files
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y python3-pip && \
    apt-get clean
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

WORKDIR /root/

# Install SCANOSS
COPY --from=builder /root/requirements.txt ./
RUN pip3 install --require-hashes -r requirements.txt

# Allow to listen port 7896
EXPOSE 7896

# Set workdir for running jar
COPY --from=frontend /frontend/build/ ./static/
COPY --from=builder /root/target/lpvs-*.jar ./lpvs.jar

# Run application in container
CMD ["java", "-jar", "lpvs.jar"]
