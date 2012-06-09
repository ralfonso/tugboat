(ns tugboat.backends.base
  (:use [clojure.data.json :only (read-json json-str)]))

(defn encode-enqueue
  [func args]
  (json-str {:callable func :args args}))

(defprotocol BaseBackendAdapter
  (enqueue [this queue func payload])
  (get-next [this queue]))
