(ns wactbprot.replhub.cli
  (:require [wactbprot.replhub.db :as db]
            [wactbprot.replhub.conf :as conf]
            [wactbprot.replhub.log :as log]
            [com.brunobonacci.mulog :as µ] )
  (:gen-class))

(comment
  (get-repl-doc conf/conf))

(defn log-start [conf] (log/start conf))

(defn log-stop [conf] (log/stop conf))

(defn get-repl-doc [{id :repl-doc :as conf}] (db/get-doc (assoc conf :id id)))

(defn gen-db-usr [conf] (db/gen-usr conf))  


(defn gen-db [conf] (db/gen-db conf))  

(comment
  (gen-db (assoc conf/conf :db "rh")))

(defn -main
  "Say Hello!"
  [& args]
  (println conf/conf))

