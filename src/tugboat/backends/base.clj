(ns tugboat.backends.base
  (:use [clojure.data.json :only (read-json json-str)]))

(defn encode-enqueue
  [task-id func args]
  (json-str {:task-id task-id :callable func :args args}))

(defprotocol BaseBackendAdapter
  (enqueue [this queue func payload])
  (get-next [this queue]))
