# Klassresan Intellij Plugin

Keep track of where you are in the code base

Detects debugging sessions and posts the current stack frames to a http endpoint with a guess on the current method name. 

# Building

build

    ./gradlew buildPlugin

outputs

    build/distributions/klassresan-x.y.z.zip

this zip file can be installed manually in intellij

to debug and run a custom ide with the plugin loaded, run

    ./gradlew runIde



# Client

To visualise a server is needed, see [../intellij-client](../intellij-client)
It receives http post requests, serves a webserver with websockets and a static html page for visualisation.
