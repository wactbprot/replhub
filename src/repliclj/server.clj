(ns repliclj.server
  (:require [compojure.route :as route]
            [com.brunobonacci.mulog :as µ]
            [repliclj.page :as page]
            [repliclj.conf :as conf]
            [repliclj.log :as log]
            [compojure.core :refer :all]
            [compojure.handler        :as handler]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :as middleware])
  (:gen-class))


(defonce server (atom nil))

(defroutes app-routes
  (GET "/" [:as req] (page/index conf/conf req))
  
  (route/resources "/")
  (route/not-found (page/not-found)))

(def app
  (-> (handler/site app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))

(defn stop [c]
  (when @server (@server :timeout 100)
        (log/stop c)
        (reset! server nil)))

(defn start [c]
  (log/start c)
  (µ/log ::start :message "start repliclj server")
  (reset! server (run-server app (:api c))))


(defn -main [& args]
  (µ/log ::-main :message "call -main")
  (start conf/conf))
