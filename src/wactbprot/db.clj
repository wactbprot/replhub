(ns wactbprot.replhub.db
  (:require [org.httpkit.client :as http]))

(defn opts [{usr :cred-usr-name pwd :cred-usr-pwd}] { :basic-auth [usr pwd]})

(defn get-doc [{url :base-url :as conf} id] (http/get (str url "/" id) (opts conf)))

(defn gen-usr [conf {usr :cred-usr-name pwd :cred-usr-pwd}]
  (let [ma {:name  usr
            :password pwd
            :roles []
            :type "user"}
        mb {:members {:names [usr]
                      :roles []}}]))
