(ns repliclj.server
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Webserver delivers overview page and keeps the system
    alive. Triggers replication check."}
  (:require [overtone.at-at :as at]
            [compojure.route :as route]
            [com.brunobonacci.mulog :as µ]
            [repliclj.cli :as cli]
            [repliclj.page :as page]
            [repliclj.conf :as conf]
            [repliclj.log :as log]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as middleware])
  (:gen-class))

(defonce at-pool (at/mk-pool))

(defonce at-every (atom nil))

(defonce server (atom nil))

(defonce rdoc (atom (cli/get-repli-doc (cli/conn conf/conf))))


(defroutes app-routes
  
  (GET "/table" [] (page/index conf/conf (cli/replis-docs conf/conf) :table))
  (GET "/graph" [] (page/index conf/conf (cli/replis-docs conf/conf) :graph))
  
  (route/resources "/")
  (route/not-found (page/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))

(defn check [c]
  (when-let [cdoc (cli/get-repli-doc (cli/conn c))]
    (when-let [nsrv (cli/new-servers @rdoc cdoc)]
      (µ/log ::check :message "found new entries")
      (cli/prepair-dbs c nsrv)
      (cli/replis-start c nsrv)
      (reset! rdoc cdoc))))

(defn stop [c]
  (when @server (@server :timeout 100)
        (at/stop @at-every)
        (log/stop c)
        (reset! server nil)))
      
(defn start [{i :check-interval :as c}]
  (log/start c)
  (µ/log ::start :message "start repliclj server")
  (reset! at-every (at/every i #(check c)  at-pool))
  (reset! server (run-server app (:api c))))


(defn -main [& args]
  (µ/log ::-main :message "call -main")
  (start conf/conf))

(comment
  (stop-and-reset-pool! at-pool))
