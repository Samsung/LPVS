FROM gcr.io/oss-fuzz-base/base-builder-jvm@sha256:0a35a200381bf7ebda008131f8b1b7f19fd9c1bba1cfbaa9e4068c124761ecfd

RUN curl -L https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip -o maven.zip && \
    unzip maven.zip -d $SRC/maven && \
    rm -rf maven.zip

ENV MVN $SRC/maven/apache-maven-3.6.3/bin/mvn

RUN rm -rf $SRC/lpvs

RUN git clone --depth 1 https://github.com/samsung/lpvs

COPY . $SRC

#COPY build.sh $SRC/
WORKDIR $SRC

# WORKDIR clusterfuzzlite-example
COPY .clusterfuzzlite/build.sh $SRC
