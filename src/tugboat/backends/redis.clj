(ns tugboat.backends.redis
  (:use [tugboat.tasks.core :only (generate-task-id)])
  (:require [clojure.string :as string]
            [clj-redis.client :as redis]
            [tugboat.backends.base :as base])
  (:import [tugboat.backends.base BaseBackendAdapter BaseResultAdapter]))

;; TODO I think this needs to handle disconnections at the Jedis level

(declare db)

(def queue-prefix "_tugboat.queues.")
(def result-prefix "_tugboat.result.")

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
      (redis/rpush db redis-queue (base/encode-enqueue task-id func args))
      task-id))

  (get-next [this queues]
    (let [redis-queues (map kw->queue queues)
          db (:redis-db this)
           pair (redis/blpop db redis-queues 0) ;; BLPOP is blocking
          [queue payload] pair]
        (assoc {} :queue queue :payload payload)))

  BaseResultAdapter
  (set-result [this task-id result]
    (let [db (:redis-db this)
          redis-key (str result-prefix task-id)
          result-timeout (:result-timeout (:config this))]
      (if result-timeout
        (redis/setex db redis-key result-timeout (base/encode-result result)))
        (redis/set db redis-key (base/encode-result result))))

  (get-result [this task-id]
    (let [db (:redis-db this)
          redis-key (str result-prefix task-id)]
      (if-let [redis-result (redis/get db redis-key)]
        (base/decode-result redis-result)))))

(defn create-adapter
  [config backend-role]
  (let [backend-conf (backend-role (:backends config))
        queues (:queues config)
        redis-db (redis/init (:url backend-conf))]
    (assoc (BackendAdapter.) :redis-db redis-db :config config :backend-config backend-conf :queues queues)))
