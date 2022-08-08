# Dockerfile to build container for unit testing.
#
# To build the image, run the following from this directory:
#   docker build -t beastfx_testing .
#
# To run the tests, use
#   docker run beastfx_testing
#
# To run the tests interactively, use
#   docker run --entrypoint /bin/bash -it -p 5900:5900 beastfx_testing
# This will give you a shell in the container. From this
# shell, run
#   vncserver $DISPLAY -geometry 1920x1080; ant -f build-testing.xml
#
# The previous command exposes the VNC session, so while the
# BEAUti test suite is running you can run a VNC viewer and
# connect it to localhost (password: password) to observe
# the graphical output of these tests.



#ARG JDK_VERSION=17
#FROM maven:3.8.1-openjdk-$JDK_VERSION-slim

#FROM openjdk:17 <= does not have apk package manager
#FROM openjdk:17-alpine
#
#WORKDIR /BeastFX
#
## Install Apache Ant
#RUN apk update && apk add apache-ant
#
#RUN apk add openjfx



#FROM openjdk:11

FROM openjdk:8

WORKDIR /BeastFX

# Install Apache Ant
RUN apt-get update && apt-get install -y ant

# Install and configure VNC server
RUN apt-get update && apt-get install -y tightvncserver twm
RUN mkdir /root/.vnc
RUN echo password | vncpasswd -f > /root/.vnc/passwd
RUN chmod 600 /root/.vnc/passwd

# get javafx
ENV DISPLAY host.docker.internal:0.0
RUN apt-get install -y openjfx=8u141-b14-3~deb9u1
RUN apt-get install -y openjfx-source libopenjfx-java libopenjfx-jni
RUN apt-get install -y libswt-gtk-3-java

# get beast2.7
RUN cd /root && git clone --depth=1 https://github.com/CompEvol/beast2.git && mv beast2 /beast2

# Install BEAGLE
#RUN apt-               get update && apt-get install -y build-essential autoconf automake libtool pkg-config
# use latest release v3.2.1, issue #786
#RUN cd /root && git clone --branch v3.2.1 --depth=1 https://github.com/beagle-dev/beagle-lib.git
#RUN cd /root/beagle-lib && ./autogen.sh && ./configure --prefix=/usr/local && make install
#RUN ldconfig

ADD . ./

RUN echo "#!/bin/bash\n" \
        "export USER=root\n" \
        "export DISPLAY=:1\n" \        
        "export LD_LIBRARY_PATY=/usr/lib/x86_64-linux-gnu/jni/\n" \
        "vncserver :1 -geometry 1920x1080\n" \
        "ant -lib locallib -f build-testing.xml \$1\n" > entrypoint.sh
RUN chmod a+x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

CMD ["test-all"]
