# Setup BEAST-FX in IntelliJ

This is the developer guide to show you how to setup BEAST 2.7.x core packages in IntelliJ.

## Core 

https://github.com/CompEvol/beast2

https://github.com/CompEvol/BeastFX

## Azul JDK 17 

BEAST 2.7.x is based on Java 17 (LTS) and GUI is developed from JavaFX.
The following link (filters are applied) to lead you to download Azul Zulu builds of OpenJDK with JavaFX:

https://www.azul.com/downloads/?version=java-17-lts&package=jdk-fx

Scroll the page to the bottom to select the correct one according to your operating system and CPU.

## IntelliJ

Download/upgrade to the last version of IntelliJ:

https://www.jetbrains.com/idea/download/

## Open "beast2" project

__Open__ your `beast2` project. If it does not exist, you can follow the tutorial
to [create it from existing sources](https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html#create-from-sources).
Do not worry about libraries, you can fix them later.

## Project Structure

After the project is opened (be patient with IntelliJ loading time), 
open [Project Structure](https://www.jetbrains.com/help/idea/project-settings-and-structure.html) to configure the project.

Here is the final setting you need to achieve:

<a href="./IntelliJProject.png"><img src="IntelliJProject.png" align="left" ></a>

If you did not have the Zulu 17, then you need to add it, otherwise you can skip the next section.

## Add Zulu JDK

Go to `SDKs` on the left, and add Zulu 17 from the downloaded Azul Zulu builds of OpenJDK with JavaFX.
If you are not familiar with this process, you can read the tutorials [SDKs](https://www.jetbrains.com/help/idea/sdk.html). 

In the end, you should have Zulu 17 in your SDKs list:

<a href="./zulu-17.png"><img src="zulu-17.png" align="left" width="750"></a>

## Setup modules

### Global libraries

Personally I recommend you to create [global libraries](https://www.jetbrains.com/help/idea/library.html),
so that they will be available for other projects.

1. beast2 library

<a href="./b2-lib.png"><img src="b2-lib.png" align="left" width="400"></a>

2. beast2 junit test

<a href="./b2-junit.png"><img src="b2-junit.png" align="left" width="400"></a>

3. BeastFx library

<a href="./b2fx-lib.png"><img src="b2fx-lib.png" align="left" width="400"></a>

### beast2 module

Add or modify the `beast2` module to make it same as the configuration below:

<a href="./b2Src.png"><img src="b2Src.png" align="left"></a>

Then setup the dependencies:

<a href="./b2Dep.png"><img src="b2Dep.png" align="left"></a>


### BeastFx module

Add or modify the `BeastFx` module to make it same as the configuration below:

<a href="./b2fxSrc.png"><img src="b2fxSrc.png" align="left"></a>

Then setup the dependencies:

<a href="./b2fxDep.png"><img src="b2fxDep.png" align="left"></a>

Because the test code is in a different folder structure, 
you need to add the package prefix to make them compile in IntelliJ.
Click the "pen" icon, and add "test" to the prefix.

<a href="./PkgPref.png"><img src="PkgPref.png" align="left" width="300"></a>

## Run application in IntelliJ

Create the [run configuration](https://www.jetbrains.com/help/idea/creating-and-running-your-first-java-application.html#create_jar_run_config) for BEAUti:

<a href="./BEAUti.png"><img src="BEAUti.png" align="left" ></a>





