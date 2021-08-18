(ns repliclj.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [repliclj.db :as db]
            [repliclj.conf :as conf]
            [repliclj.crypto :as crypto]
            [repliclj.log :as log]
            [clojure.pprint :as pp]
            [com.brunobonacci.mulog :as µ])
  (:use [clojure.repl])
  (:gen-class))

;;........................................................................
;; log
;;........................................................................
(defn log-start [c] (log/start c))

(defn log-stop [c] (log/stop c))

;;........................................................................
;; doc
;;........................................................................
(defn get-repli-doc [{id :repl-doc :as c}] (db/get-doc (conn (assoc c :id id))))

(defn del-doc [c {id :id {rev :rev} :value}] (db/del-doc (assoc c :id id :rev rev)))

;;........................................................................
;; replication
;;........................................................................
(defn act-repl [c] (filterv #(= (:type %) "replication") (db/active-tasks c)))

(defn repli-table [c]
  (let [v [:source :target :continuous :user]]
    (pp/print-table (mapv #(select-keys % v) (act-repl c)))))

(defn replis-stop [c]
  (let [v (db/get-repli-docs c)
        c (assoc c :db "_replicator")]
    (mapv #(del-doc c %) v)))

;;........................................................................
;; crypt
;;........................................................................
(defn decrypt-hash-a [{hash :hash-a secret :cred-admin-secret :as c}]
  (assoc c :cred-admin-pwd (crypto/decrypt hash secret)))

;;........................................................................
;; preparation
;;........................................................................
(defn conn
  ([c] (conn c nil))
  ([c m] (decrypt-hash-a (if m (merge c m) c))))
  
(defn ensure-users-db [c] (db/gen-db (assoc c :db "_users")))

(defn ensure-repli-db [c] (db/gen-db (assoc c :db "_replicator")))

(defn ensure-db+usr [c] (db/gen-db c) (db/add-usr c))
  
(defn ensure-vl-db [c] (ensure-db+usr (assoc c :db "vl_db")))

(defn ensure-work-db [c] (ensure-db+usr (assoc c :db "vl_db_work")))

(defn ensure-bu-db [c] (ensure-db+usr (assoc c :db "vl_db_bu")))

(defn inner-dbs [c]
  (ensure-users-db c)
  (db/gen-usr c)
  (ensure-repli-db c)
  (ensure-vl-db c))

(defn outer-dbs [c]
  (inner-dbs c)
  (ensure-work-db c)
  (ensure-bu-db c))

(defn prepair-all [c]
  (let [rdoc    (:Replications (get-repli-doc c))
        in-srv  (:Inner rdoc)
        out-srv (:Outer rdoc)]
    (mapv #(inner-dbs (conn c %)) in-srv)
    (mapv #(outer-dbs (conn c %)) out-srv)))


(comment
  (def c conf/conf)
  (def m  {:server "e75458"
           :port "5984"
           :alias "Optische Druckmessung (devhub)"
           :hash-a "FjwyQzvCIVPqoowj85s+YA=="})
  
  (def e (conn c m))
  
  (db/get-doc (assoc c :id (:repl-doc c)))
  (db/gen-db (assoc c :db "_users"))
  (db/gen-db (assoc c :db "_replicator"))

  ;; start replication on localhost
  (db/start-repli (assoc (conn conf/conf) :db "vl_db") (assoc (conn conf/conf) :db "vl_db_work"))
  )
