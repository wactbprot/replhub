(ns wactbprot.replhub.cli
  (:require [wactbprot.replhub.db :as db]
            [wactbprot.replhub.conf :as conf]
            [wactbprot.replhub.log :as log]
            [com.brunobonacci.mulog :as µ] )
  (:gen-class))

(defn log-start [c] (log/start c))

(defn log-stop [c] (log/stop c))

(defn get-repl-doc [{id :repl-doc :as c}] (db/get-doc (assoc c :id id)))

(defn gen-usr [c] (db/gen-usr c))  

(defn add-usr [c] (db/add-usr c))  

(defn gen-db [c] (db/gen-db c))  

(defn -main [& args] (println conf/conf))

(comment
  (get-repl-doc conf/conf)
  (gen-usr (assoc conf/conf :db "rh" :cred-usr-name "rh"))
  (gen-db (assoc conf/conf :db "rh"))
  (add-usr (assoc conf/conf :db "rh" :cred-usr-name "rh")))
