(ns repliclj.server
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Webserver delivers overview page and keeps the system
    alive. Triggers replication check. Clears off outdated replications."}
  (:require [overtone.at-at :as at]
            [compojure.route :as route]
            [com.brunobonacci.mulog :as µ]
            [repliclj.cli :as cli]
            [repliclj.page :as page]
            [repliclj.conf :as conf]
            [repliclj.log :as log]
            [repliclj.utils :as u]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as middleware])
  (:gen-class))

(defonce at-pool (at/mk-pool))

(defonce at-every (atom nil))

(defonce server (atom nil))

(defonce rdoc (atom nil))

(defroutes app-routes
  (GET "/" [] (page/index conf/conf (cli/active-info conf/conf)))
  (route/resources "/")
  (route/not-found (page/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))

(defn check [c]
  (let [cdoc (cli/get-rdoc (cli/conn c))]
      (when-not (= (count cdoc) (count @rdoc)) 
        (µ/log ::check :message "found differences")
        (cli/prepair-dbs c cdoc)
        (cli/start-replis c cdoc)
        (cli/clear-replis c cdoc)
        (reset! rdoc cdoc))))

(defn stop [c]
  (when @server (@server :timeout 100)
        (at/stop @at-every)
        (log/stop c)
        (reset! server nil)))

(defn start [{i :check-interval :as c}]
  (log/start c)
  (µ/log ::start :message "start repliclj server, check")
  (check c)
  (reset! at-every (at/every i #(check c) at-pool))
  (reset! server (run-server app (:api c))))

(defn -main [& args]
  (µ/log ::-main :message "call -main")
  (start conf/conf))

(comment
  (start conf/conf)
  (stop-and-reset-pool! at-pool))
