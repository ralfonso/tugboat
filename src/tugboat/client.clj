(ns tugboat.client
  (:use [clojure.data.json :only (read-json json-str)])
  (:require [tugboat.config :as config]
            [tugboat.backends.core :as backend]))

(def backend-adapter (delay (do (backend/create @config/conf))))

(defn enqueue
  [queue func args]
  (let [adapter @backend-adapter]
    (.enqueue adapter queue func args)))
