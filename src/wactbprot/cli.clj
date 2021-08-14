(ns wactbprot.repliclj.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [wactbprot.repliclj.db :as db]
            [wactbprot.repliclj.conf :as conf]
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
;; doc
;;........................................................................
(defn get-doc [c] (db/get-doc c))

;;........................................................................
;; usr
;;........................................................................
(defn gen-usr [c] (db/gen-usr c))  

(defn add-usr [c] (db/add-usr c))  

(defn db-usr [c] (db/gen-usr c) (db/add-usr c))
  
(defn gen-db [c] (db/gen-db c))  

;;........................................................................
;; replication
;;........................................................................
(defn act-repl [c] (filterv #(= (:type %) "replication") (db/active-tasks c)))

(defn repl-table [c]
  (let [v [:source :target :continuous :user]]
    (pp/print-table (mapv #(select-keys % v) (act-repl c)))))

(def c conf/conf)

(comment
  
  (get-doc (assoc c :id (:repl-doc c)))
  (gen-db (assoc c :db "_users"))
  (gen-db (assoc c :db "_replicator"))
  (gen-usr (assoc c :db "rh" :cred-usr-name "rh"))
  (add-usr (assoc c :db "rh" :cred-usr-name "rh")))
