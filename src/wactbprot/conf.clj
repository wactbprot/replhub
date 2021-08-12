(ns wactbprot.replhub.conf
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as io]
            [clojure.string  :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([] (get-config (io/resource "config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn cred-usr []
  {:cred-usr-name (System/getenv "CAL_USR")
   :cred-usr-pwd  (System/getenv "CAL_PWD")})

(defn cred-admin []
  {:cred-admin-name (System/getenv "ADMIN_USR")
   :cred-admin-pwd  (System/getenv "ADMIN_PWD")})

(defn log-context [{app-name :app-name :as c}]
  {:facility (get-in c [:mulog :context :facility])
   :app-name app-name})

(def conf 
  (let [c (get-config)]
    (merge (assoc c
                  :global-log-context (log-context c)
                  :base-url (base-url c))
           (cred-usr)
           (cred-admin))))

(comment
  (require :reload 'wactbprot.replhub.conf))
