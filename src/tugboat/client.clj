(ns tugboat.client
  (:use [clojure.data.json :only (read-json json-str)])
  (:require [tugboat.config :as config]
            [tugboat.backends.core :as backend]))

(def backend-adapter 
  (delay 
    (do 
      (if (empty? @config/conf)
        (throw (Exception. "You must configure Tugboat before queueing tasks")))
      (backend/create @config/conf))))

(defn enqueue
  "Allow clients to push a task to the queue"
  ([queue func] 
    (enqueue queue func []))

  ([queue func args]
    (let [adapter @backend-adapter]
      (.enqueue adapter queue func args))))
