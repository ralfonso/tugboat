(ns tugboat.backends.base
  (:use [clojure.data.json :only (read-json json-str)]))

(defn encode-enqueue
  [task-id func args]
  (json-str {:task-id task-id :callable func :args args}))

(defn encode-result
  [result]
  (json-str result))

(defn decode-result
  [result]
  (read-json result))

(defprotocol BaseBackendAdapter
  (enqueue [this queue func payload])
  (get-next [this queue]))

(defprotocol BaseResultAdapter
  (set-result [this task-id result])
  (get-result [this task-id]))
