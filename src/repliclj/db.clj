(ns repliclj.db
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Basic database interop. Plain HTTP."}
  (:require [cheshire.core :as che]
            [clojure.string :as string]
            [repliclj.utils :as u]
            [org.httpkit.client :as http]
            [com.brunobonacci.mulog :as µ]))

;;........................................................................
;; utils
;;........................................................................
(defn base-url [{prot :prot srv :server port :port}] (str prot "://"  srv ":" port))

(defn db-url [{db :db :as conn}] (str (base-url conn) "/" db))

(defn dbs-url [{db :db :as conn}] (str (base-url conn) "/_all_dbs"))

(defn dbs-info-url [{db :db :as conn}] (str (base-url conn) "/_dbs_info"))

(defn doc-url [{id :id rev :rev :as conn}]
  (str (db-url conn) "/" id (when rev (str "?rev=" rev))))

(defn repli-docs-url [conn] (str (base-url conn) "/_replicator/_all_docs"))

(defn usr-url [{usr :cred-usr-name :as conn}] (str (base-url conn) "/_users/org.couchdb.user:" usr))

(defn sec-url [conn] (str (db-url conn) "/_security"))

(defn act-url [conn] (str (base-url conn) "/_scheduler/docs/_replicator/"))

(defn cred-db-url [{prot :prot srv :server port :port name :cred-admin-name pwd :cred-admin-pwd db :db}]
  (str prot "://" name ":" pwd "@" srv ":" port "/" db))

(defn design-doc? [m]
  (let [id (or (:_id m) (:id m))]
    (string/starts-with? id "_design")))

(defn opts [{name :cred-admin-name pwd :cred-admin-pwd t :timeout} url]
  {:headers {"Content-Type" "application/json"
             "Referer" url}
   :timeout t
   :basic-auth [name pwd]})

(defn result [{body :body header :headers status :status url :url}]
  (let [body (try (che/parse-string-strict body true )
                  (catch Exception e (µ/log ::result :error (.getMessage e))))]
    (if (< status 400)
      (do (µ/log ::result :status  status :url url) body)
      (µ/log ::result :status  status :url url :error (:error body) :reason (:reason body)))))

;;........................................................................
;; query fuctions
;;........................................................................
(defn get-rev [conn]
  (let [url (base-url conn)
        opt (opts conn url)]
;; go on here
    @(http/head url opt)))

(defn online? [conn]
  (let [url (base-url conn)
        opt (opts conn url)]
  (if (not (contains? @(http/head url (assoc opt :timeout 100)) :error))
    (do (µ/log ::online? :url url :online true) true)
    (do (µ/log ::online? :url url :online false) false))))

(defn exists? [url opt]
  (if (< (get @(http/head url opt) :status 400) 400)
    (do (µ/log ::exists? :url url :exists true) true)
    (do (µ/log ::exists? :url url :exists false) false)))

(defn active-docs [conn]
  (let [url (act-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (:docs (result @(http/get url opt))))))

(defn get-doc [conn]
  (let [url (doc-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (when (exists? url opt)
      (result @(http/get url opt))))))

(defn del-doc [conn]
  (let [url (doc-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (when (exists? url opt)
        (let [res (result @(http/get url opt))]
          (result @(http/delete (doc-url (assoc conn :rev (:_rev res))) opt)))))))

(defn post-doc [conn doc]
  (let [url (doc-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (when-not (exists? url opt)
        (result @(http/put url (assoc opt :body (che/encode doc))))))))

(defn get-repli-docs [conn]
  (let [url (repli-docs-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (when (exists? url opt)
        (when-let [rows (:rows (result @(http/get url opt)))]
          (filterv #(not (design-doc? %)) rows))))))

(defn gen-db [conn]
  (let [url (db-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (when-not (exists? url opt)
        (result @(http/put url opt))))))

(defn get-dbs-info [conn]
  (let [url (dbs-url conn)
        opt (opts conn url)]
    (when (online? conn)
      (let [dbs (result @(http/get url opt))]
        (when (seq dbs)
          (let [url (dbs-info-url conn)]
            (result @(http/post url (assoc opt :body (che/encode {:keys dbs}))))))))))

(defn gen-usr [{usr :cred-usr-name pwd :cred-usr-pwd :as conn}]
  (let [url (usr-url conn)
        opt (opts conn url)
        doc {:name usr :password pwd :roles [] :type "user"}]
    (when (online? conn)
      (when-not (exists? url opt)
        (result @(http/put url (assoc opt :body (che/encode doc))))))))

(defn add-usr [{usr :cred-usr-name :as conn}]
  (let [url (sec-url conn)
        opt (opts conn url)
        doc {:members {:names [usr] :roles []}}]
    (when (online? conn)
      (result @(http/put url (assoc opt :body (che/encode doc)))))))

(defn start-repli
  ([source target] (start-repli source target true))
  ([source target cont?]
   (post-doc (assoc source :db "_replicator" :id (u/gen-repli-id source target))
            {:_id (u/gen-repli-id source target)
             :source (cred-db-url source)
             :target (cred-db-url target)
             :continuous cont?})))
