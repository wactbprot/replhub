# RepliClj

<img src="model.png" alt="model" id="logo">

Generates databases and a user at CouchDB (v3) instances. Initialises
but also stops replications following the document
[000_REPLICATIONS](http://a75438.berlin.ptb.de:5984/_utils/#database/vl_db/000_REPLICATIONS).
Provides a website with the [replication status](http://a75438:8011/).

## Environment vars

Most operations need admin rights e.g. the generation of the `_users`,
`_replicator` etc. datebases and the generation and adding the
`cal` user. The following environment variables should be set:

* `export ADMIN_USR=<admins name>` (defaults to "admin")
* `export ADMIN_SECRET=<admins secret>` **this is not the password**
* `export CAL_USR=< users name>` (defaults to "cal")
* `export CAL_PWD=< users password>`

## generate uberjar (tools.build)

In order to generate an stand alone uberjar **RepliClj** uses [tools.build](https://clojure.org/guides/tools_build). Run:

```shell
clj -T:build clean
clj -T:build prep
clj -T:build uber
```

Start the server by invoking:

```shell
java -jar target/repliclj-x.y.z-standalone.jar
```

## systemd

```shell
cd /path/to/repliclj
sudo mkdir /usr/local/share/repliclj
sudo cp repliclj.jar /usr/local/share/repliclj
sudo cp repliclj.service  /etc/systemd/system/
sudo systemctl enable repliclj.service
sudo systemctl start repliclj.service
sudo systemctl status repliclj.service
``` 

## notes

### model graph

```shell
neato model.dot -T png > model.png
```


## replication documentation

* [introduction to replication](https://docs.couchdb.org/en/stable/replication/intro.html#introduction-to-replication) 
* `RepliClj` uses [persistent replication](https://docs.couchdb.org/en/stable/replication/intro.html#transient-and-persistent-replication)
* [replication start, stop](https://docs.couchdb.org/en/stable/replication/intro.html#transient-and-persistent-replication)
* [replication states descriptions](https://docs.couchdb.org/en/main/replication/replicator.html#states-descriptions)
