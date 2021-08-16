(ns wactbprot.repliclj.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [wactbprot.repliclj.db :as db]
            [wactbprot.repliclj.conf :as conf]
            [wactbprot.repliclj.crypto :as crypto]
            [wactbprot.repliclj.log :as log]
            [clojure.pprint :as pp]
            [com.brunobonacci.mulog :as µ])
  (:use   [clojure.repl])
  (:gen-class))

;;........................................................................
;; log
;;........................................................................
(defn log-start [c] (log/start c))

(defn log-stop [c] (log/stop c))

;;........................................................................
;; usr
;;........................................................................ 
(defn db-usr [c] (db/gen-usr c) (db/add-usr c))

;;........................................................................
;; replication
;;........................................................................
(defn act-repl [c] (filterv #(= (:type %) "replication") (db/active-tasks c)))

(defn repl-table [c]
  (let [v [:source :target :continuous :user]]
    (pp/print-table (mapv #(select-keys % v) (act-repl c)))))

;;........................................................................
;; crypt
;;........................................................................
(defn decrypt-admin-pwd [{hash :aes-128 secret :cred-admin-secret :as c}]
  (assoc c :cred-admin-pwd (crypto/decrypt hash secret)))

;;........................................................................
;; preparation
;;........................................................................
(defn ensure-users-db [c] (db/gen-db (assoc c :db "_users")))

(defn ensure-repli-db [c] (db/gen-db (assoc c :db "_replicator")))

(defn ensure-vl-db [c]
  (db/gen-db (assoc c :db "vl_db"))
  (db-usr (assoc c :db "vl_db"
                 :cred-usr-name (:cred-cal-name c)
                 :cred-usr-pwd (:cred-cal-pwd c))))

(defn ensure-work-db [c]
  (db/gen-db (assoc c :db "vl_db_work"))
  (db-usr (assoc c :db "vl_db_work"
                 :cred-usr-name (:cred-cal-name c)
                 :cred-usr-pwd (:cred-cal-pwd c))))

(defn ensure-bu-db [c]
  (db/gen-db (assoc c :db "vl_db_bu"))
  (db-usr (assoc c :db "vl_db_bu"
                 :cred-usr-name (:cred-cal-name c)
                 :cred-usr-pwd (:cred-cal-pwd c))))

(defn lvl-0 [c]
  (ensure-users-db c)
  (ensure-repli-db c)
  (ensure-vl-db c))

(defn lvl-1 [c]
  (ensure-users-db c)
  (ensure-repli-db c)
  (ensure-vl-db c)
  (ensure-work-db c)
  (ensure-bu-db c))

(def c conf/conf)

(comment
  (def d {:server "e75458",
          :port "5984",
          :alias "optische Druckmessung (devhub)",
          :level 1,
          :aes-128 "FjwyQzvCIVPqoowj85s+YA=="}
  (db/get-doc (assoc c :id (:repl-doc c)))
  (db/gen-db (assoc c :db "_users"))
  (db/gen-db (assoc c :db "_replicator"))
  (gen-usr (assoc c :db "rh" :cred-usr-name "rh"))
  (add-usr (assoc c :db "rh" :cred-usr-name "rh")))
