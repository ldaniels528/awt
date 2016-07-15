# Azure Workload Tracker

The Azure Workload Tracker (AWT) was designed to aide Microsoft Solution Consultants to track Azure workloads in the field.

## Building the code

<a name="Build_Requirements"></a>
#### Build Requirements

* [Scala 2.11.8+] (http://scala-lang.org/download/)
* [Scala.js 0.6.8] (http://www.scala-js.org/)
* [SBT 0.13.11+] (http://www.scala-sbt.org/download.html)
* [MEANS.js 0.2.1] (https://github.com/ldaniels528/scalajs-ndejs)

#### Building the application locally

Prior to building the code, you need to install the bower and node modules. 
*NOTE*: You only need to perform this step once.

```bash
$ cd app-nodejs
$ npm install
$ bower install
$ cd ..
```

Now, you can compile the Scala.js sources to JavaScript by executing the following command:

```bash
$ sbt "project nodejs" fastOptJS
```

#### Running the application locally

```bash
$ cd ./app-node
$ node ./dev-server.js    
```

The above will startup the application on port 1337 by default. To listen/bind to a different port. Set the "port" environment
variable.

```bash
$ export port=8000
```

Then (re)start the application.
