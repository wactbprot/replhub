(ns wactbprot.repliclj.db
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Basic database interop. Plain HTTP."}
  (:require [cheshire.core :as che]
            [org.httpkit.client :as http]
            [com.brunobonacci.mulog :as µ]))

;;........................................................................
;; utils
;;........................................................................
(defn base-url [{prot :prot srv :server port :port}] (str prot "://"  srv ":" port))

(defn db-url [{db :db :as conn}] (str (base-url conn) "/" db))

(defn doc-url [{id :id :as conn}] (str (db-url conn) "/" id))

(defn usr-url [{usr :cred-usr-name :as conn}] (str (base-url conn) "/_users/org.couchdb.user:" usr))

(defn sec-url [conn] (str (db-url conn) "/_security"))

(defn act-url [conn] (str (base-url conn) "/_active_tasks"))


(defn opts
  ([conn] (opts conn :usr))
  ([conn role]
   (prn conn)
   {:timeout (:timeout conn)
    :basic-auth
    (condp = role
      :usr   [(:cred-usr-name conn) (:cred-usr-pwd conn)]
      :admin [(:cred-admin-name conn) (:cred-admin-pwd conn)])}))

(defn result [{body :body header :headers status :status url :url}]
  (let [body (try (che/decode body true)
                  (catch Exception e (µ/log ::result :error (.getMessage e))))]
    (if (< status 400) 
      (µ/log ::result :status  status :url url)
      (µ/log ::result :status  status :reason (:reason body) :url url))
    (or body header)))

;;........................................................................
;; query fuctions
;;........................................................................
(defn exists? [url opt] (contains? (result @(http/head url opt)) :etag))

(defn active-tasks [conn] (result @(http/get (act-url conn) (opts conn :admin))))

(defn get-doc [conn]
  (let [url (doc-url conn)]
    (result @(http/get url (opts conn)))))

(defn gen-db [conn]
  (let [url (doc-url conn)
        opt (opts conn :admin)]
    (when-not (exists? url opt)
      (result @(http/put (db-url conn) (opts conn :admin))))))

(defn gen-usr [{usr :cred-usr-name pwd :cred-usr-pwd :as conn}]
  (let [url  (usr-url conn)
        opt  (opts conn :admin)
        body (che/encode {:name usr :password pwd :roles [] :type "user"})]
    (when-not (exists? url opt)
      (result @(http/put url (assoc opt :body body))))))

(defn add-usr [{usr :cred-usr-name :as conn}]
  (let [url  (sec-url conn)
        opt  (opts conn :admin)
        body (che/encode {:members {:names [usr] :roles []}})]
    (result @(http/put url (assoc opt :body body)))))

