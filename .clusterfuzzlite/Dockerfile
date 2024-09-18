FROM gcr.io/oss-fuzz-base/base-builder-jvm@sha256:e57ec446d50da82d34047f0da16fd667ef40b5e8752447bfae37d4b129fa3ace

RUN curl -L https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip -o maven.zip && \
    unzip maven.zip -d $SRC/maven && \
    rm -rf maven.zip

ENV MVN $SRC/maven/apache-maven-3.9.6/bin/mvn

RUN rm -rf $SRC/lpvs

RUN git clone --depth 1 https://github.com/samsung/lpvs

COPY . $SRC

#COPY build.sh $SRC/
WORKDIR $SRC

# WORKDIR clusterfuzzlite-example
COPY .clusterfuzzlite/build.sh $SRC
