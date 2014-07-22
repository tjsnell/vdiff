vdiff
=====

Apache and other Release Package Version Diffs

Forked from https://github.com/tjsnell/vdiff

You can run within your web container or alternatively run it standalone using jetty-runner

java -jar target/dependency/jetty-runner.jar --path version-diff --port 8181 target/*.war

Then, using your browser to open

http://localhost:8181/version-diff/

or 
http://localhost:8181/version-diff/?camel
http://localhost:8181/version-diff/?cxf
http://localhost:8181/version-diff/?activemq
...

or explicitly setting the organization part
http://localhost:8181/version-diff/?apache/cxf
http://localhost:8181/version-diff/?Atmosphere/atmosphere


Any project hosted at github which specifies its depending version properties at its root
pom.xml or at parent/pom.xml can be comapred.

