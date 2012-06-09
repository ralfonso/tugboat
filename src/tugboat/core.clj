(ns tugboat.core
  (:use [clojure.data.json :only (read-json json-str)])
  (:require [tugboat.config :as config]
            [tugboat.backends.core :as backend]))

(defn run-worker
  [backend-adapter queues]
  (loop []
    (let [task (.get-next backend-adapter queues)]
      (println task)
      (if-let [responded-queue (:queue task)]
        (if-let [payload (:payload task)]
          (let [parsed-payload (read-json payload)
                callable (:callable parsed-payload)
                args (:args parsed-payload)]
            (println (apply (resolve (symbol callable)) args)))
          (println "no payload"))
         (println "no queue")))
    (recur)))

(defn init
  [configuration]
  (config/configure configuration))

(defn do-work
  []
  (doseq [task-ns (:task-namespaces @config/conf)]
    (require (symbol task-ns)))

  (let [backend-adapter (backend/create @config/conf)
        queues (:queues @config/conf)
        worker-count (:workers @config/conf)
        workers (doall
                  (repeatedly worker-count #(future (doall (run-worker backend-adapter queues)))))]
    (doseq [worker workers] @worker)))

(defn -main
  []
  (init {:backend {:type :redis :url "redis://localhost:6379"}
         :queues [:test :test2]
         :task-namespaces ["tugboat.tasks.builtins"]
         :workers 10})
  (do-work))
