# BeastFX

Applications for [BEAST 2](https://github.com/CompEvol/beast2/) with (most) GUIs based on Java FX.

## Building & compiling

BEAST now uses Java 17 instead of Java 8, which might give a small performance boost for some models and allows access to Java 17 features.

It is recommended to install the JDK from Azul, since it allows painless integration of JavaFX. It can be downloaded from here: [https://www.azul.com/downloads/?package=jdk](https://www.azul.com/downloads/?package=jdk)

* scroll to the bottom
* select `Java 17 LTS`
* select your operating system
* select `JDK FX` ** make sure to include FX and not just select JDK without FX **
* download the file and install according to instructions under the `How to install` link.

In the IDE that you are using, make sure that the JDK points to Java 17.

It is convenient to make this JDK the default. For Linux, this can be done by adding to your `~/.bashrc` file the following two lines:

```
export JAVA_HOME=/path/to/zulu17.34.19-ca-fx-jdk17.0.3-linux_x64
export PATH=${JAVA_HOME}/bin:${PATH}
```

## Get code

BeastFX depends on [BEAST 2](https://github.com/CompEvol/beast2/), so make sure to clone both the BeastFX and BEAST 2 repository, e.g. like so:

```
git clone git@github.com:CompEvol/beast2.git
git clone git@github.com:CompEvol/BeastFX.git
```

The build scripts assume `beast2` and `BeastFX` have the same parent directory.

## Include libraries

If you are using an IDE, make sure to include the following libraries from `beast2/lib`

* beagle.jar		
* colt.jar		
* commons-math3-3.6.1.jar		
* antlr-runtime-4.10.1.jar	
* junit/junit-platform-console-standalone-1.8.2.jar

and from `BeastFX/locallib`

* jam.jar -- gui library
* testfx.jar -- assists in testing
* FXSkins-1.0.0.jar -- GUI theme
* jmetro-11.6.15.jar -- GUI theme


## Source folders

Sources are in `BeastFX/src`, and resources in `BeastFX/resources`.

If you are setting up in an IDE, make sure to include both as source folders, otherwise themes will not be available (i.e. dark mode will not work) when debugging.

## Building from the command line

Use [apache ant](https://ant.apache.org/) to build the BeastFX package.

`ant addon`

should build the `BEAST.app` package in `/path/to/BeastFX/build/dist/BEAST.app.addon.v2.7.X.zip` where X the current version of BeastFX and `/path/to/BeastFX` the path to where `BeastFX` resides on your computer.

Releases containing all applications for your operating system can be built using one of:

* `ant linux`
* `ant windows`
* `ant mac`

This requires the jre for you operating system to be available, which can be downloaded from the [Zulu website](https://www.azul.com/downloads/). 
Make sure that the `openjreMac`, `openjreWnd` or `openjreLnx` property at the top of the `build.xml` file in the BeastFX package points to where the JRE resides.
