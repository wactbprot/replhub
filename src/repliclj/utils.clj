(ns repliclj.utils
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Simple replication state overview page delivered by server.clj."}
  (:require [clojure.string :as string]))


(defn date [] (.format (new java.text.SimpleDateFormat "yyyy-MM-dd HH:mm") (java.util.Date.)))


(defn url
  "Return a vector containing the url parts.
  ```clojure
  [http://admin:*****@i75419.berlin.ptb.de:5984/vl_db/ggg/
  http://admin:*****@i75419.berlin.ptb.de:5984
  http:
  admin
  *****
  i75419.berlin.ptb.de:5984
 i75419.berlin.ptb.de
  5984
  /vl_db/ggg/
  nil
  nil]
  ```"
  [s]
  ;; https://stackoverflow.com/questions/27745/getting-parts-of-a-url-regex
  (let [r  #"^(?:(?:(([^:\/#\?]+:)?(?:(?:\/\/)(?:(?:(?:([^:@\/#\?]+)(?:\:([^:@\/#\?]*))?)@)?(([^:\/#\?\]\[]+|\[[^\/\]@#?]+\])(?:\:([0-9]+))?))?)?)?((?:\/?(?:[^\/\?#]+\/+)*)(?:[^\?#]*)))?(\?[^#]+)?)(#.*)?"]
    (re-find (re-matcher r s))))

(defn url->db [s] (second (string/split (get (url s) 8) #"\/")))

(defn url->host [s] (get (url s) 6))

(defn host->host-name [s] (first (string/split s #"\.")))

(defn nice-date [s] (string/replace s #"[TZ]" "&nbsp;&nbsp;&nbsp;"))

(defn gen-repli-id [{ad :db as :server} {bd :db bs :server}]
  (str ad "@" (host->host-name as)  "--" bd "@" (host->host-name bs)))
