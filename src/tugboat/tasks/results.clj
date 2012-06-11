(ns tugboat.tasks.results
  (:use [clojure.data.json :only (read-json json-str)]
        [clojure.tools.logging :only (debugf infof errorf)])
  (:require [tugboat.config :as config]))

(defn set-result
  [adapter task-id result]
  (if (and adapter (:result (:backends @config/conf)))
    (.set-result adapter task-id result)))
