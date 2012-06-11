# tugboat

library for consuming from a queue and doing work

## Usage

Tugboat works as both a server (to consume from the queue) and a library (to populate the queue).

To set up the server:

```clj
;; in MYPROJECT/tasks.clj, define your task
(ns MYPROJECT.tasks)
(defn sum
  [x y]
  (+ x y))
  
;; in MYPROJECT/core (or wherever you want main)
(require '[tugboat.core :as tugboat])

;; initialize Tugboat.  This expects a map with config data.
(tugboat/init 
         ;; define the queue and result backends
         {:backends {:queue {:type :redis :url "redis://localhost:6379"}
                     :result {:type :redis :url "redis://localhost:6379"}}
         ;; define the queues from which this server will consume tasks
         :queues [:queue1 :queue2]
         ;; how long to tasks remain, in seconds.  (redis only)
         :result-timeout 600
         ;; define the namespaces that contain our tasks
         :task-namespaces ["MYPROJECT.tasks"]
         ;; the number of worker threads to use
         :workers 10})

;; fire off the workers
(tugboat/do-work)
```

For the library, you still have to configure the backends.  You can enqueue work by using a fully-qualified namespace.

```clj

(require '[tugboat.core :as tugboat])
(require '[tugboat.client :as tugboat-client])

;; call tugboat/init as above
;; enqueue a task. syntax is (enqueue queue-keyword task-name arguments).  Returns a task UUID.
(tugboat-client/enqueue :queue1 "MYPROJECT.tasks/sum" [5 10])
;; => "ab750610-b41d-11e1-9ab7-c42c033414ee"

;; get the result/status
(tugboat-client/get-result "ab750610-b41d-11e1-9ab7-c42c033414ee")
;; => {:status :pending :task-id "ab750610-b41d-11e1-9ab7-c42c033414ee"}
;; or, after completion:
;; => {:status :success :task-id "ab750610-b41d-11e1-9ab7-c42c033414ee" :value 15 :elapsed-time .000108}

## License

Copyright © 2012 Ryan Roemmich

Distributed under the MIT License
