(ns tugboat.core
  (:require [tugboat.config :as config]
            [tugboat.backends.core :as backend]
            [tugboat.worker :as worker])
  (:import (java.io FileNotFoundException)))

(defn load-task-namespaces
  "load the namespaces that contain our app's tasks"
  [namespaces]
  (doseq [task-ns namespaces]
    (try
      (require (symbol task-ns))
      (catch FileNotFoundException e (throw (Exception. (format "Could not find task namespace %s" task-ns)))))))

(defn init
  [configuration]
  (config/configure configuration)
  (load-task-namespaces (:task-namespaces @config/conf)))

(defn do-work
  "initalize the workers and run their threads"
  ;; TODO need a way to gracefully shut down main thread and workers
  []
  (let [backend-adapter (backend/create @config/conf)
        queues (:queues @config/conf)
        worker-count (:workers @config/conf)
        workers (doall
                  (repeatedly worker-count #(future (doall (worker/run-worker backend-adapter queues)))))]
    (doseq [worker workers] @worker)))

(defn -main
  []
  (init {:backend {:type :redis :url "redis://localhost:6379"}
         :queues [:test :test2]
         :task-namespaces ["tugboat.tasks.builtins"]
         :workers 10})
  (do-work))
