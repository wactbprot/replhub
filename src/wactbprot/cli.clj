(ns wactbprot.replhub.cli
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Command line interface."}
  (:require [wactbprot.replhub.db :as db]
            [wactbprot.replhub.conf :as conf]
            [wactbprot.replhub.log :as log]
            [com.brunobonacci.mulog :as µ]))

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

(def c conf/conf)

(comment
  
  (get-doc (assoc c :id (:repl-doc c)))
  (gen-usr (assoc c :db "rh" :cred-usr-name "rh"))
  (gen-db (assoc c :db "rh"))
  (add-usr (assoc c :db "rh" :cred-usr-name "rh")))
