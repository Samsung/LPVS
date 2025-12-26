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
    apt-get install -y \
        ca-certificates \
        software-properties-common \
        wget \
        curl \
        gnupg \
        lsb-release && \
    apt-get clean

# Add deadsnakes PPA for Python 3.13
RUN wget -O /etc/apt/trusted.gpg.d/python.gpg https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x3B81FE248EB361B8 && \
    echo "deb http://ppa.launchpad.net/deadsnakes/ppa/ubuntu jammy main" > /etc/apt/sources.list.d/deadsnakes.list && \
    apt-get update

# Install Python 3.13 and pip
RUN apt-get install -y python3.13 python3.13-pip python3.13-venv python3.13-distutils && \
    apt-get clean

# Clean up apt caches and tmp files
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

WORKDIR /root/

# Install SCANOSS
COPY --from=builder /root/requirements.txt ./
RUN python3.13 -m pip install --require-hashes -r requirements.txt

# Allow to listen port 7896
EXPOSE 7896

# Set workdir for running jar
COPY --from=builder /root/target/lpvs-*.jar ./lpvs.jar

# Run application in container
CMD ["java", "-jar", "lpvs.jar"]
