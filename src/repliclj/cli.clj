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

  
(defn ensure-users-db [c] (db/gen-db (assoc c :db "_users")))

(defn ensure-repli-db [c] (db/gen-db (assoc c :db "_replicator")))

(defn ensure-db+usr [c] (db/gen-db c) (db/add-usr c))
  
(defn ensure-vl-db [c] (ensure-db+usr (assoc c :db "vl_db")))

(defn ensure-work-db [c] (ensure-db+usr (assoc c :db "vl_db_work")))

(defn prepair-db [c]
  (ensure-users-db c)
  (db/gen-usr c)
  (ensure-repli-db c)
  (ensure-vl-db c)
  (ensure-work-db c))

(defn prepair-dbs [c]
  (let [rdoc (:Replications (get-repli-doc c))]
    (mapv #(prepair-db (conn c %)) rdoc)))

(defn inner-repli [c]
  (let [rdoc (:Replications (get-repli-doc c))]
    (mapv #(db/start-repli (assoc (conn c %) :db "vl_db")
                           (assoc (conn c %) :db "vl_db_work")) rdoc)))

(defn outer-repli [c]
  (let [rdoc (:Replications (get-repli-doc c))]
    (mapv #(let [m (nth rdoc %)]
             (mapv #(when (not= m %)
                      (db/start-repli (assoc (conn c m) :db "vl_db")
                                      (assoc (conn c %) :db "vl_db")))
                   rdoc)))
    (range (count rdoc))))

(comment
  (def c (conn conf/conf))
  (def m  {:server "e75458"
           :port "5984"
           :alias "Optische Druckmessung (devhub)"
           :hash-a "FjwyQzvCIVPqoowj85s+YA=="})
  
  (def e (conn c m))
  
  (db/get-doc (assoc c :id (:repl-doc c)))
  (db/gen-db (assoc c :db "_users"))
  (db/gen-db (assoc c :db "_replicator"))

  ;; start replication on localhost
  (db/start-repli (assoc c :db "vl_db") (assoc c :db "vl_db_work")))
