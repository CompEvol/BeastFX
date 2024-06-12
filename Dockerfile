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
#
# This will give you a shell in the container. From this
# shell, run
#   vncserver $DISPLAY -geometry 1920x1080; ant -f build-testing.xml
#
# The previous command exposes the VNC session, so while the
# BEAUti test suite is running you can run a VNC viewer and
# connect it to localhost (password: password) to observe
# the graphical output of these tests.
#
# NB on OS X M1, you may need `--platform linux/arm64/v8`, that is
#   docker build --platform linux/arm64/v8 -t beastfx_testing .
#   docker run --platform linux/arm64/v8 -t beastfx_testing
#   docker run --platform linux/arm64/v8 --entrypoint /bin/bash -it -p 5900:5900 beastfx_testing


FROM openjdk:8

RUN apt-get update
# Install stuff required later
RUN apt-get install -y curl git

ARG DEBIAN_FRONTEND=noninteractive

ENV USER root

WORKDIR /root/BeastFX


# Add JDK
RUN if [ `uname -m` = 'x86_64' ]; then \
	curl -fsSL -o /tmp/jdk17.0.5-linux.tar.gz https://cdn.azul.com/zulu/bin/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64.tar.gz; \
    else \
	curl -fsSL -o /tmp/jdk17.0.5-linux.tar.gz https://download.bell-sw.com/java/17.0.5+8/bellsoft-jdk17.0.5+8-linux-aarch64-full.tar.gz; \
    fi 

RUN cd /tmp/ \
  && tar fxz /tmp/jdk17.0.5-linux.tar.gz \
  && if [ `uname -m` = 'x86_64' ]; then \
        mv /tmp/zulu17.38.21-ca-fx-jdk17.0.5-linux_x64 /tmp/jdk17.0.5-linux; \
     else \ 
        mv /tmp/jdk-17.0.5-full /tmp/jdk17.0.5-linux;\ 
     fi
  		

# Install Apache Ant
RUN curl -k -fsSL -o /tmp/apache-ant-bin.tar.gz https://dlcdn.apache.org//ant/binaries/apache-ant-1.10.14-bin.tar.gz \
  && cd /tmp/ \
  && tar fxz /tmp/apache-ant-bin.tar.gz \
  && mv /tmp/apache-ant-1.10.14 /tmp/apache-ant

ENV JAVA_HOME=/tmp/jdk17.0.5-linux
ENV ANT_HOME=/tmp/apache-ant-1.10.12
ENV PATH=/tmp/jdk17.0.5-linux/bin:/tmp/apache-ant/bin:$PATH


# get beast2.7
RUN cd /root && git clone --depth=1 https://github.com/CompEvol/beast2.git 

# Install BEAGLE
#RUN apt-               get update && apt-get install -y build-essential autoconf automake libtool pkg-config
# use latest release v3.2.1, issue #786
#RUN cd /root && git clone --branch v3.2.1 --depth=1 https://github.com/beagle-dev/beagle-lib.git
#RUN cd /root/beagle-lib && ./autogen.sh && ./configure --prefix=/usr/local && make install
#RUN ldconfig


# Install and configure VNC server
#RUN apt install -y xfce4 xfce4-goodies xfonts-base dbus-x11 tightvncserver
RUN apt install -y xfce4 tightvncserver
RUN mkdir /root/.vnc
RUN echo password | vncpasswd -f > /root/.vnc/passwd
RUN chmod 600 /root/.vnc/passwd

ADD . ./

ENV DISPLAY :2

RUN echo "#!/bin/bash\n" \
        "export USER=root\n" \
        "export DISPLAY=:2\n" \
        "vncserver :2 -geometry 1920x1080\n" \
        "ant -f build-testing.xml \$1\n" > entrypoint.sh

RUN chmod a+x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

CMD ["test-all"]
