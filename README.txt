===============================
How to test and build jcr-shell
===============================

1. Introduction

  Very brief introduction to how to build, test and run jcr-shell!

2. Requirements --

  (1) Java 1.6
  (2) Maven 3.0.x

3. Build

  (1) Build with testing

    $ mvn clean install

  (2) Build with skipping tests

    $ mvn clean install -DskipTests

4. How to run the console during development

  (1) Move to `console' folder and run the following:

    $ mvn exec:java

5. Create distribution files

  (1) Move to `console' folder and run the following:

    $ mvn appassembler:assemble

  (2) Move to `console/target' folder and compress the the assembled application directory:

    $ cd target
    $ tar cvfz jcr-shell.tgz jcr-shell/
    $ zip -r jcr-shell.zip jcr-shell/
