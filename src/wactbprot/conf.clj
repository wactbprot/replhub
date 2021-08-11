(ns wactbprot.replhub.conf
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([] (get-config (io/resource "config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn base-url [{prot :prot srv :srv port :port}]
  (let [usr  (System/getenv "CAL_USR")
        pwd  (System/getenv "CAL_PWD")]
    (str prot "://" (when (and usr pwd) (str usr ":" pwd "@")) srv ":" port)))

(defn conn [{db :db :as c}] (str (base-url c) "/" db))

(defn log-context [{app-name :app-name :as c}]
  {:facility (get-in c [:mulog :context :facility])
   :app-name app-name})

(def conf 
  (let [c (get-config)]
    (assoc c
           :global-log-context (log-context c)
           :base-url (base-url c)
           :conn (conn c))))

(comment
  (require :reload 'wactbprot.replhub.conf))
