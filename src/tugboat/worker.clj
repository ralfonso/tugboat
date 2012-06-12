(ns tugboat.worker
  (:use [clojure.data.json :only (read-json json-str)]
        [clojure.tools.logging :only (debugf infof errorf)]
        [tugboat.tasks.results :only (set-result)]))

(defn run-task
  [task-id callable args]
  (let [resolved-func (resolve (symbol callable))]
    (debugf "Running task: %s[%s]" callable task-id)
    (try
      (if (nil? resolved-func)
        (throw (Exception. (str "Task not found: " callable))))
      (let [rv (apply resolved-func args)]
        {:task-id task-id :value rv :status :success})
    (catch Exception e 
      (do 
        (infof "Exception from task %s caught: (%s: %s)" task-id e (.getMessage e))
        {:task-id task-id :status :failure})))))

(defn run-worker
  "Runs within a worker thread. Needs to loop forever. Pulls an item from the
  work queue and executes."
  ;; TODO needs better error handling
  ;; TOOD needs retrying
  ;; TODO needs a way to gracefully shut down
  ([queue-adapter result-adapter queues]
    (run-worker queue-adapter result-adapter queues false 5000))

  ([queue-adapter result-adapter queues blocking timeout]
  (infof "Worker thread started")
  (loop []
    (let [task (.get-next queue-adapter queues)]
      (debugf "Received task: %s" task)
      (if-let [responded-queue (:queue task)]
        (if-let [payload (:payload task)]
          (let [parsed-payload (read-json payload)
                task-id (:task-id parsed-payload)
                callable (:callable parsed-payload)
                args (:args parsed-payload)]
            ;; change the status of the result to :started
            (.set-result result-adapter task-id {:status :started :task-id task-id})
            (let [start-time (System/nanoTime)
                  raw-result (run-task task-id callable args)
                  end-time (System/nanoTime)
                  elapsed-time (/ (- end-time start-time) 1e9)
                  result (assoc raw-result :elapsed-time elapsed-time)]
              (debugf "Task %s[%s] %s: %fs" callable task-id (name (:status result)) elapsed-time)
              ;; set the final result
              (.set-result result-adapter task-id result)))
          (infof "Invalid task, no payload found: %s" task))
         (infof "Queue not found: %s" task)))
    (when blocking
      (Thread/sleep timeout))
    (recur))))
