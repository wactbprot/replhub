(ns repliclj.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [repliclj.db :as db]
            [repliclj.conf :as conf]
            [repliclj.crypto :as crypto]
            [repliclj.log :as log]
            [clojure.pprint :as pp]
            [clojure.data :as data]
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
  (assoc c :cred-admin-pwd (when (and hash secret) (crypto/decrypt hash secret))))

;;........................................................................
;; preparation
;;........................................................................
(defn conn
  ([c] (conn c nil))
  ([c m] (decrypt-hash-a (if m (merge c m) c))))

(defn new-servers [o n]
  (let [d (data/diff o n)]
    (filterv seq (second d))))
;;........................................................................
;; doc
;;........................................................................
(defn get-repli-doc [{id :repl-doc :as c}]
  (:Replications (db/get-doc (conn (assoc c :id id)))))

(defn del-doc [c {id :id {rev :rev} :value}]
  (db/del-doc (assoc c :id id :rev rev)))

;;........................................................................
;; replication
;;........................................................................
(defn repli-table [c]
  (let [v [:doc_id :state  :error_count :start_time :last_updated ]]
    (pp/print-table (mapv #(select-keys % v) (db/repli-docs c)))))

(defn replis-docs [c] 
  (let [rdoc (get-repli-doc c)]
    (mapv (fn [m] {:server (:server m)
                   :docs (db/repli-docs (conn c m))
                   :alias (:alias m)
                   :db-info (db/get-dbs-info (conn c m))}) rdoc)))

;;........................................................................
;; replication stop
;;........................................................................
(defn repli-stop 
  "Stops all replications at `c`."
  [c]
  (let [v (db/get-repli-docs c)
        c (assoc c :db "_replicator")]
    (mapv #(del-doc c %) v)))

(defn replis-stop
  "Stops all replications on the entire system."
  [c]
  (mapv repli-stop (get-repli-doc c)))

;;........................................................................
;; replication start
;;........................................................................
(defn start-repli 
  "Starts a replications from `source`to `target`."
  [c source target]
  (db/start-repli (conn c source) (conn c target)))

(defn inner-replis [c rdoc]
    (mapv #(start-repli c (assoc % :db "vl_db") (assoc % :db "vl_db_work"))
          rdoc))

(defn outer-replis [c rdoc]
    (mapv #(let [source (nth rdoc %)]
             (mapv (fn [target]
                     (when (not= source target)
                       (start-repli c (assoc source :db "vl_db") (assoc target :db "vl_db"))))
                   rdoc))
          (range (count rdoc))))

(defn replis-start 
  "Starts the inner and outer replications of the entire system."
  [c rdoc]
  (inner-replis c rdoc)
  (outer-replis c rdoc))

;;........................................................................
;; database and usr
;;........................................................................
(defn ensure-db+usr [c] (db/gen-db c) (db/add-usr c))

(defn ensure-users-db [c] (db/gen-db (assoc c :db "_users")))

(defn ensure-repli-db [c] (db/gen-db (assoc c :db "_replicator")))
  
(defn ensure-vl-db [c] (ensure-db+usr (assoc c :db "vl_db")))

(defn ensure-work-db [c] (ensure-db+usr (assoc c :db "vl_db_work")))

(defn prepair-db [c]
  (ensure-users-db c)
  (db/gen-usr c)
  (ensure-repli-db c)
  (ensure-vl-db c)
  (ensure-work-db c))

(defn prepair-dbs [c rdoc] (mapv #(prepair-db (conn c %)) rdoc))

;;........................................................................
;; playground
;;........................................................................
(comment
  ;; use conn to resolve pwd
  (def c (conn conf/conf))
  
  (def m  {:server "e75467"
           :port "5984"
           :alias "Optische Druckmessung (devhub)"
           :hash-a "FjwyQzvCIVPqoowj85s+YA=="})
  
  (def e (conn c m))
  
  (db/get-doc (assoc c :id (:repl-doc c)))
  (db/gen-db (assoc c :db "_users"))
  (db/gen-db (assoc c :db "_replicator"))

  ;; start replication on localhost
  (db/start-repli (assoc c :db "vl_db") (assoc c :db "vl_db_work")))
