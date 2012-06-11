(ns tugboat.backends.redis
  (:use [tugboat.tasks.core :only (generate-task-id)])
  (:require [clojure.string :as string]
            [clj-redis.client :as redis]
            [tugboat.backends.base :as base])
  (:import [tugboat.backends.base BaseBackendAdapter]))

(declare db)

(def queue-prefix "_tugboat.queues.")

(defn kw->queue
  "Convert a keyword to a queue name"
  [kw]
  (str queue-prefix (name kw)))

(defn queue->kw    
  "Convert a queue name to a clojure keyword"
  [q]
  (keyword (last (string/split q #"\.")))) 

(defrecord BackendAdapter []
  BaseBackendAdapter
  (enqueue [this queue func args]
    (let [redis-queue (kw->queue queue)
          db (:redis-db this)
          task-id (generate-task-id)]
      (redis/rpush db redis-queue (base/encode-enqueue task-id func args))))
  (get-next [this queues]
    (let [redis-queues (map kw->queue queues)
          db (:redis-db this)
           pair (redis/blpop db redis-queues 0) ;; BLPOP is blocking
          [queue payload] pair]
        (assoc {} :queue queue :payload payload))))

(defn create-adapter
  [config]
  (let [backend-conf (:backend config)
        queues (:queues config)
        redis-db (redis/init (:url backend-conf))]
    (assoc (BackendAdapter.) :redis-db redis-db :config (:backend config) :queues queues)))
