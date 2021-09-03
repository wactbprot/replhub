(ns repliclj.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [repliclj.db :as db]
            [repliclj.conf :as conf]
            [repliclj.crypto :as crypto]
            [repliclj.log :as log]
            [repliclj.utils :as u]
            [clojure.data :as data]
            [clojure.string :as string]
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
;; connection
;;........................................................................
(defn conn
  ([c] (conn c nil))
  ([c m] (decrypt-hash-a (if m (merge c m) c))))

;;........................................................................
;; rdoc
;;........................................................................
(defn get-rdoc [{id :repl-doc :as c}]
  (:Replications (db/get-doc (conn (assoc c :id id)))))

;;........................................................................
;; replication info
;;........................................................................
(defn active-info [c]
  (let [rdoc (get-rdoc c)]
    (mapv (fn [m] {:server (:server m)
                   :docs (db/active-docs (conn c m))
                   :alias (:alias m)
                   :db-info (db/get-dbs-info (conn c m))}) rdoc)))

;;........................................................................
;; replication stop
;;........................................................................
(defn repli-stop
  "Stops all replications at `c`."
  [c m]
  (let [v (db/get-repli-docs (conn c m))
        c (assoc c :db "_replicator")]
    (mapv (fn [{id :id {rev :rev} :value}]
            (db/del-doc (assoc (conn c m):id id :rev rev))) v)))

(defn replis-stop
  "Stops all replications on the entire system."
  [c]
  (mapv #(repli-stop c %) (get-rdoc c)))

;;........................................................................
;; replication clear
;;........................................................................
(defn filter-by-target [host-name idoc] (filterv #(string/ends-with? (:id %) host-name) idoc))

(defn clear-repli [c m]
  (let [rdoc          (get-rdoc c)
        idoc          (db/get-repli-docs (conn c m))
        should-hosts  (set (mapv (comp u/host->host-name :server) rdoc))
        is-hosts      (set (filterv seq (mapv (comp u/id->target-host-name :id) idoc)))
        clear-hosts   (second (data/diff should-hosts is-hosts))
        docs-to-clear (flatten (mapv  #(filter-by-target % idoc) clear-hosts))]
    (mapv #(db/del-doc (assoc (conn c m) :id (:id %) :db "_replicator")) docs-to-clear)))

(defn clear-replis
  "Clears all replications which are not in rdoc."
  [c rdoc]
  (mapv #(clear-repli c %) rdoc))

;;........................................................................
;; replication start
;;........................................................................
(defn start-repli
  "Starts a replications from `source`to `target`."
  [c source target]
  (db/start-repli (conn c source) (conn c target)))

(defn inner-replis [{work-db :work-db outer-db :outer-db :as c} rdoc]
    (mapv #(start-repli c (assoc % :db outer-db) (assoc % :db work-db))
          rdoc))

(defn outer-replis [{work-db :work-db outer-db :outer-db :as c} rdoc]
    (mapv #(let [source (nth rdoc %)]
             (mapv (fn [target]
                     (when (not= source target)
                       (start-repli c (assoc source :db outer-db) (assoc target :db outer-db))))
                   rdoc))
          (range (count rdoc))))

(defn start-replis
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

(defn ensure-vl-db [{outer-db :outer-db :as c}] (ensure-db+usr (assoc c :db outer-db)))

(defn ensure-work-db [{work-db :work-db :as c}] (ensure-db+usr (assoc c :db work-db)))

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
