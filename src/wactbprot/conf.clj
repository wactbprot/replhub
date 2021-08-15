(ns wactbprot.repliclj.conf
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Configuration functions. Merge environment variables."}
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn get-config
  "Slurps an `edn` configuration in file `f`. Falls back to
  `resources/config.edn`" 
  ([] (get-config (io/resource "config.edn")))
  ([f] (-> f slurp edn/read-string)))

(defn cred []
  {:cred-usr-name (or (System/getenv "REPLICLJ_USR") "rcusr")
   :cred-usr-pwd  (System/getenv "REPLICLJ_PWD")
   :cred-cal-name (or (System/getenv "CAL_USR") "cal")
   :cred-cal-pwd  (System/getenv "CAL_PWD")
   :cred-admin-name (or (System/getenv "ADMIN_USR") "admin")
   :cred-admin-pwd  (System/getenv "ADMIN_PWD")})

(def conf (merge (get-config) (cred)))

(comment
  (require :reload 'wactbprot.repliclj.conf))
