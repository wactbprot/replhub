(ns repliclj.db
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Basic database interop. Plain HTTP."}
  (:require [cheshire.core :as che]
            [clojure.string :as string]
            [org.httpkit.client :as http]
            [com.brunobonacci.mulog :as µ]))

;;........................................................................
;; utils
;;........................................................................
(defn base-url [{prot :prot srv :server port :port}] (str prot "://"  srv ":" port))

(defn db-url [{db :db :as conn}] (str (base-url conn) "/" db))

(defn doc-url [{id :id rev :rev :as conn}]
  (str (db-url conn) "/" id (when rev (str "?rev=" rev))))

(defn repli-docs-url [conn] (str (base-url conn) "/_replicator/_all_docs")) 

(defn usr-url [{usr :cred-usr-name :as conn}] (str (base-url conn) "/_users/org.couchdb.user:" usr))

(defn sec-url [conn] (str (db-url conn) "/_security"))

(defn act-url [conn] (str (base-url conn) "/_active_tasks"))

(defn design-doc? [m]
  (let [id (or (:_id m) (:id m))]
    (string/starts-with? id "_design")))

(defn opts [{name :cred-admin-name pwd :cred-admin-pwd t :timeout}]
  {:timeout t
   :basic-auth [name pwd]})

(defn result [{body :body header :headers status :status url :url}]
  (let [body (try (che/decode body true)
                  (catch Exception e (µ/log ::result :error (.getMessage e))))]
    (if (< status 400) 
      (do (µ/log ::result :status  status :url url) body)
      (µ/log ::result :status  status :url url :error (:error body) :reason (:reason body)))))

;;........................................................................
;; query fuctions
;;........................................................................
(defn online? [url opt]
  (if (not (contains? @(http/head url opt) :error))
    (do (µ/log ::online? :url url :online true) true)
    (do (µ/log ::online? :url url :online false) false)))
    
      
(defn exists? [url opt]
  (if (< (get @(http/head url opt) :status 400) 400)
    (do (µ/log ::exists? :url url :exists true) true)
    (do (µ/log ::exists? :url url :exists false) false)))

(defn active-tasks [conn] (result @(http/get (act-url conn) (opts conn))))

(defn get-doc [conn]
  (let [url (doc-url conn) 
        opt (opts conn)]
    (when (online? url conn)
      (when (exists? url opt)
      (result @(http/get url opt))))))

(defn del-doc [conn]
  (let [url (doc-url conn) 
        opt (opts conn)]
    (when (online? url conn)
      (when (exists? url opt)
        (result @(http/delete url opt))))))

(defn get-repli-docs [conn]
  (let [url (repli-docs-url conn) 
        opt (opts conn)]
    (when (online? url conn)
      (when (exists? url opt)
        (when-let [rows (:rows (result @(http/get url opt)))]
          (filterv #(not (design-doc? %)) rows))))))

(defn gen-db [conn]
  (let [url (db-url conn)
        opt (opts conn)]
    (when (online? url conn)
      (when-not (exists? url opt)
        (result @(http/put url opt))))))

(defn gen-usr [{usr :cred-usr-name pwd :cred-usr-pwd :as conn}]
  (let [url  (usr-url conn)
        opt  (opts conn)
        body (che/encode {:name usr :password pwd :roles [] :type "user"})]
    (when (online? url conn)
      (when-not (exists? url opt)
        (result @(http/put url (assoc opt :body body)))))))

(defn add-usr [{usr :cred-usr-name :as conn}]
  (let [url  (sec-url conn)
        opt  (opts conn)
        body (che/encode {:members {:names [usr] :roles []}})]
    (when (online? url conn)
      (when-not (exists? url opt)
        (result @(http/put url (assoc opt :body body)))))))

