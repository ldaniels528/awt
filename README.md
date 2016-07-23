# Azure Workload Tracker

The Azure Workload Tracker (AWT) was designed to aide Microsoft Solution Consultants to track Azure workloads in the field.

## Building the code

<a name="Build_Requirements"></a>
#### Build Requirements

* [SBT 0.13.x] (http://www.scala-sbt.org/download.html)

#### Building the application locally

Prior to building the code, you need to install the bower and node modules. 
*NOTE*: You only need to perform this step once.

```bash
$ npm install
$ bower install
```

Now, you can compile the Scala.js sources to JavaScript by executing the following command:

```bash
$  sbt fastOptJSPlus
```

#### Running the application locally

```bash
$ node ./server.js
```

The above will startup the application on port 1337 by default. To listen/bind to a different port. Set the "port" environment
variable.

```bash
$ export port=8000
```

Then (re)start the application.
