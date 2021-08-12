(ns wactbprot.replhub.db
  (:require [cheshire.core :as che]
            [org.httpkit.client :as http]
            [com.brunobonacci.mulog :as µ]))

(defn url [{prot :prot srv :srv port :port}] (str prot "://"  srv ":" port))

(defn db-url [{db :db :as conn}] (str (url conn) "/" db))

(defn doc-url [{id :id :as conn}] (str (db-url conn) "/" id))

(defn usr-url [{usr :cred-usr-name :as conn}] (str (url conn) "/_users/org.couchdb.user:" usr))

(defn sec-url [conn] (str (db-url conn) "/_security"))

(defn opts
  ([conn] (opts conn :usr))
  ([conn role]
   {:timeout (:timeout conn)
    :basic-auth
    (condp = role
      :usr   [(:cred-usr-name conn) (:cred-usr-pwd conn)]
      :admin [(:cred-admin-name conn) (:cred-admin-pwd conn)])}))

(defn result [{body :body header :headers status :status url :url}]
  (let [body (try
               (che/decode body true)
               (catch Exception e
                 (µ/log ::result :error (.getMessage e))))]
    (if (< status 400) 
      (µ/log ::result :status  status :ok true :url url)
      (µ/log ::result :status  status :error (:error body) :reason (:reason body) :url url))
    (or body header)))


(defn get-doc [conn]
  (let [url (doc-url conn)]
    (µ/log ::get-doc :url url :state :start)
    (result @(http/get url (opts conn)))))

(defn gen-db [conn]
  (let [url (doc-url conn)]
    (µ/log ::gen-db :url url :state :start)
    (result @(http/put (db-url conn) (opts conn :admin)))))

(defn gen-usr [{usr :cred-usr-name pwd :cred-usr-pwd :as conn}]
  (let [url  (usr-url conn)
        opt  (opts conn :admin)
        body (che/encode {:name usr :password pwd :roles [] :type "user"})
        head (result @(http/head url opt))]
    (µ/log ::gen-usr :url url :state :prepair)
    (if-not (contains? head :etag)
      (result @(http/put url (assoc opt :body body)))
      {:ok true :warn "already exists"})))

(defn add-usr [{usr :cred-usr-name :as conn}]
  (let [url  (sec-url conn)
        opt  (opts conn :admin)
        body (che/encode {:members {:names [usr] :roles []}})]
    (µ/log ::add-usr :url url :state :start)
    (result @(http/put url (assoc opt :body body)))))

