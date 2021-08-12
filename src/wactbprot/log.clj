(ns wactbprot.replhub.log
  (:require [wactbprot.replhub.conf :as conf]
            [com.brunobonacci.mulog :as µ]))

(defonce logger (atom nil))

(defn stop
  ([] (stop conf/conf))
  ([conf]
   (µ/log ::stop)
   (@logger)
   (reset! logger nil)))

(defn start
  ([] (start conf/conf))
  ([{mulog :mulog log-context :log-context app-name :app-name}]
   (µ/set-global-context! (assoc log-context
                                 :app-name app-name))
   (reset! logger (µ/start-publisher! mulog))
   conf))
