(ns repliclj.log
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
      :doc "Switch log on and off. Uses [µlog](https://github.com/BrunoBonacci/mulog). 
            Writes direct to elastic search."}
  (:require [repliclj.conf :as conf]
            [com.brunobonacci.mulog :as µ]))

(defonce logger (atom nil))

(defn stop
  ([] (stop conf/conf))
  ([c]
   (µ/log ::stop)
   (@logger)
   (reset! logger nil)))

(defn start
  ([] (start conf/conf))
  ([{mulog :mulog log-context :log-context app-name :app-name}]
   (µ/set-global-context! (assoc log-context
                                 :app-name app-name))
   (reset! logger (µ/start-publisher! mulog))))
