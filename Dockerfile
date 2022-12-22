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


FROM openjdk:8

ARG DEBIAN_FRONTEND=noninteractive

# Install Apache Ant
RUN apt-get update && apt-get install -y ant

# Install and configure VNC server
RUN apt-get update && apt-get install -y tightvncserver twm
RUN mkdir /root/.vnc
RUN echo password | vncpasswd -f > /root/.vnc/passwd
RUN chmod 600 /root/.vnc/passwd

#FROM ubuntu:18.04 as system
#
WORKDIR /root/BeastFX

# Install stuff required later
RUN apt-get update && apt-get install -y git curl

ENV USER root


# set up VNC
#RUN    apt-get install -y x11vnc xvfb \
#    && mkdir ~/.vnc \
#    && x11vnc -storepasswd 1234 ~/.vnc/passwd 


# Add JDK
RUN curl -fsSL -o /tmp/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64.tar.gz https://cdn.azul.com/zulu/bin/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64.tar.gz \
  && cd /tmp/ \
  && tar fxz /tmp/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64.tar.gz 

ENV JAVA_HOME=/tmp/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64
ENV PATH=/tmp/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64/bin:$PATH


# Install Apache Ant
RUN apt-get update && apt-get install -y ant

# Install and configure VNC server
#RUN apt-get update && apt-get install -y tightvncserver twm
#RUN mkdir /root/.vnc
#RUN echo password | vncpasswd -f > /root/.vnc/passwd
#RUN chmod 600 /root/.vnc/passwd
#ENV DISPLAY host.docker.internal:0.0


# get beast2.7
RUN cd /root && git clone --depth=1 https://github.com/CompEvol/beast2.git 
# && mv beast2 /beast2

# Install BEAGLE
#RUN apt-               get update && apt-get install -y build-essential autoconf automake libtool pkg-config
# use latest release v3.2.1, issue #786
#RUN cd /root && git clone --branch v3.2.1 --depth=1 https://github.com/beagle-dev/beagle-lib.git
#RUN cd /root/beagle-lib && ./autogen.sh && ./configure --prefix=/usr/local && make install
#RUN ldconfig

ADD . ./

RUN echo "#!/bin/bash\n"\
     "x11vnc -forever -usepw -create &\n"\
     "/bin/bash" > /entrypoint.sh 
ENTRYPOINT ["/entrypoint.sh"]


RUN echo "#!/bin/bash\n" \
        "export USER=root\n" \
        "export DISPLAY=:1\n" \
        "vncserver :1 -geometry 1920x1080\n" \
        "ant -f build-testing.xml \$1\n" > entrypoint.sh
#RUN echo "#!/bin/bash\n" \
#        "export USER=root\n" \
#        "export DISPLAY=:1\n" \        
#        "export LD_LIBRARY_PATY=/usr/lib/x86_64-linux-gnu/jni/\n" \
#        "x11vnc -forever -usepw -create &\n"\
#        "ant -lib locallib -f build-testing.xml \$1\n" > entrypoint.sh

RUN chmod a+x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

CMD ["test-all"]
