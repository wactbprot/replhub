(ns wactbprot.replhub.db
  (:require [org.httpkit.client :as http]))

(defn get-doc [{conn :conn} id] (http/get (str conn "/" id)))
