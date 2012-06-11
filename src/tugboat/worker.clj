(ns tugboat.worker
  (:use [clojure.data.json :only (read-json json-str)]))

(defn run-worker
  "Runs within a worker thread. Needs to loop forever. Pulls an item from the
  work queue and executes."
  ;; TODO needs better error handling
  ;; TOOD needs retrying
  ;; TODO needs a way to gracefully shut down
  ([backend-adapter queues]
    (run-worker backend-adapter queues false 5000))

  ([backend-adapter queues blocking timeout]
  (loop []
    (let [task (.get-next backend-adapter queues)]
      (println task)
      (if-let [responded-queue (:queue task)]
        (if-let [payload (:payload task)]
          (let [parsed-payload (read-json payload)
                task-id (:task-id parsed-payload)
                callable (:callable parsed-payload)
                args (:args parsed-payload)]
            (try
              (apply (resolve (symbol callable)) args)
              (catch Exception e (println (format "Exception from task %s caught: %s" task-id e)))))
          (println "no payload"))
         (println "no queue")))
    (if blocking
      (Thread/sleep timeout))
    (recur))))
