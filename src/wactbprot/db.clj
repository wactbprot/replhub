(ns wactbprot.replhub.db
  (:require [org.httpkit.client :as http]
            [com.brunobonacci.mulog :as µ]))

(defn url [{prot :prot srv :srv port :port}] (str prot "://"  srv ":" port))

(defn db-url [{db :db :as conn}] (str (url conn) "/" db))

(defn doc-url [{id :db :as conn}]  (str (db-url conn) "/" id))

(defn opts
  ([conn] (opts conn :usr))
  ([conn role]
   {:basic-auth
    (condp = role
      :usr   [(:cred-usr-name conn) (:cred-usr-pwd conn)]
      :admin [(:cred-admin-name conn) (:cred-admin-pwd conn)])}))

   
(defn get-doc [conn] (http/get (doc-url conn) (opts conn)))

(defn gen-usr [{usr :cred-usr-name pwd :cred-usr-pwd}]
  (let [a {:name  usr
            :password pwd
            :roles []
            :type "user"}
        b {:members {:names [usr]
                      :roles []}}]))

(defn gen-db [conn] (http/put (db-url conn) (opts conn :admin)))
