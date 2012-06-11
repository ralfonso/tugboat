(ns tugboat.tasks.core
  (:import [com.eaio.uuid UUID]))

(defn generate-task-id
  "Wrapper around UUID generation"
  []
  (str (UUID.)))
