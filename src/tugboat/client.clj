(ns tugboat.client
  (:use [clojure.data.json :only (read-json json-str)])
  (:require [tugboat.config :as config]
            [tugboat.backends.core :as backend]))

(defn- get-adapter
  [adapter-type]
  (if (empty? @config/conf)
    (throw (Exception. "You must configure Tugboat before queueing tasks")))
  (backend/create @config/conf adapter-type))

(def backend-adapter 
  (delay (get-adapter :queue)))

(def result-adapter 
  (delay (get-adapter :result)))

(defn enqueue
  "Allow clients to push a task to the queue"
  ([queue func] 
    (enqueue queue func []))

  ([queue func args]
    (let [backend-adapter @backend-adapter
          result-adapter @result-adapter
          task-id (.enqueue backend-adapter queue func args)]
      (.set-result result-adapter task-id {:status :pending :task-id task-id}))))
