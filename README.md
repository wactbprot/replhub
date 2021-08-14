# RepliClj

Replica + Clj = RepliClj

Keep CouchDB (v3) replications alive, generate database users, visualize replication status.

## Replication documentation

* [introduction-to-replication](https://docs.couchdb.org/en/stable/replication/intro.html#introduction-to-replication) 
* `RepliClj` uses [persistent replication](https://docs.couchdb.org/en/stable/replication/intro.html#transient-and-persistent-replication)
* [start, stop](https://docs.couchdb.org/en/stable/replication/intro.html#transient-and-persistent-replication)

## ENVVARs

Some operations need admin rights: generate the `_users` and
`_replicator` datebase furthermore the generation and adding a user
that manages the replication. The following environment variables should be set:

* `export ADMIN_USR=<admins name>` (defaults to "admin")
* `export ADMIN_PWD=<admins password>`
* `export REPLICLJ_USR=< users name>` (defaults to "rcusr")
* `export REPLICLJ_PWD=< users password>` 