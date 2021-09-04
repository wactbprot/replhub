(ns repliclj.utils
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Simple replication state overview page delivered by server.clj."}
  (:require [clojure.string :as string]))


(defn date [] (.format (new java.text.SimpleDateFormat "yyyy-MM-dd HH:mm") (java.util.Date.)))


(defn url
  "Return a vector containing the url parts.

  ```clojure
  [http://admin:*****@e75467.berlin.ptb.de:5984/vl_db
  http
  admin
  *****
  e75467
  berlin.ptb.de
  5984
  vl_db]
  ```"
  [s]
  (let [r  #"^(http[s]?)\://(\w*\:?[\*]*@?)?([a-z0-9]*\.?[\w\.]*):([0-9]*)\/([\w\_]*)"
        v (filterv seq (re-find r s))
        n (count v)]
    {:db   (get v (- n 1))
     :port (get v (- n 2))
     :host (get v (- n 3))
     :prot (get v 1)}))

(defn url->db [s] (:db (url s)))

(defn url->host [s] (:host (url s)))

(defn url->prot [s] (:prot (url s)))

(defn url->port [s] (:port (url s)))

(defn host->host-name [s] (first (string/split s #"\.")))

(defn url->host-name [s] (host->host-name (:host (url s))))

(defn nice-date [s] (string/replace s #"[TZ]" "&nbsp;&nbsp;&nbsp;"))

(defn gen-repli-id [{ad :db as :server} {bd :db bs :server}]
  (str ad "@" (host->host-name as)  "--" bd "@" (host->host-name bs)))

(defn repli-doc-link [m]
  (str (url->prot (:source m)) "://"
       (url->host (:source m)) ":"
       (url->port (:source m)) "/_utils/#database/" (:database m)"/" (:doc_id m)))

(defn id->target-host-name [s]
  (let [v (string/split s #"@")]
    (when (and (string/includes? s "--") (= 3 (count v))) (last v))))
