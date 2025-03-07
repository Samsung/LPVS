FROM node:23@sha256:c29271c7f2b4788fe9b90a7506d790dc8f2ff46132e1b70e71bf0c0679c8451c AS frontend

WORKDIR /frontend
COPY frontend .
RUN npm ci
RUN npm run build

# Base image for building lpvs lib
FROM openjdk:17-slim@sha256:aaa3b3cb27e3e520b8f116863d0580c438ed55ecfa0bc126b41f68c3f62f9774 AS builder

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
FROM openjdk:17-slim@sha256:aaa3b3cb27e3e520b8f116863d0580c438ed55ecfa0bc126b41f68c3f62f9774

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
