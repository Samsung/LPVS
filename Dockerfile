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

# OpenJDK 17 stage
FROM openjdk:17-slim@sha256:aaa3b3cb27e3e520b8f116863d0580c438ed55ecfa0bc126b41f68c3f62f9774

# Install build dependencies for Python compilation
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
        ca-certificates \
        wget \
        build-essential \
        libssl-dev \
        zlib1g-dev \
        libncurses5-dev \
        libncursesw5-dev \
        libreadline-dev \
        libsqlite3-dev \
        libgdbm-dev \
        libdb5.3-dev \
        libbz2-dev \
        libexpat1-dev \
        libffi-dev \
        liblzma-dev \
        uuid-dev && \
    apt-get clean

# Download and compile Python 3.13 from source with optimizations
RUN --mount=type=cache,target=/var/cache/apt \
    --mount=type=cache,target=/root/.cache/pip \
    wget https://www.python.org/ftp/python/3.13.1/Python-3.13.1.tgz && \
    tar xzf Python-3.13.1.tgz && \
    cd Python-3.13.1 && \
    ./configure \
        --enable-optimizations \
        --enable-shared \
        --with-ensurepip=install \
        --prefix=/usr/local \
        --disable-test-modules \
        --disable-tk \
        --disable-curses \
        LDFLAGS="-Wl,-rpath,/usr/local/lib" && \
    make -j$(nproc) PROFILE_TASK="" && \
    make altinstall && \
    cd .. && \
    rm -rf Python-3.13.1 Python-3.13.1.tgz

# Create symlinks for easier access
RUN ln -sf /usr/local/bin/python3.13 /usr/bin/python3.13 && \
    ln -sf /usr/local/bin/pip3.13 /usr/bin/pip3.13 && \
    ln -sf /usr/local/bin/python3.13 /usr/bin/python3 && \
    ln -sf /usr/local/bin/pip3.13 /usr/bin/pip3

# Update shared library cache
RUN ldconfig

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
