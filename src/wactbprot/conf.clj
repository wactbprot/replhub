(ns wactbprot.repliclj.conf
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Configuration functions. Merge environment variables."}
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn get-config
  "Reads a `edn` configuration in file `f`." 
  ([] (get-config (io/resource "config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn cred []
  {:cred-usr-name (System/getenv "CAL_USR")
   :cred-usr-pwd  (System/getenv "CAL_PWD")
   :cred-admin-name (System/getenv "ADMIN_USR")
   :cred-admin-pwd  (System/getenv "ADMIN_PWD")})

(def conf (merge (get-config) (cred)))

(comment
  (require :reload 'wactbprot.repliclj.conf))
