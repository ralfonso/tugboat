(ns tugboat.backends.redis
  (:require [clojure.string :as string]
            [clj-redis.client :as redis]
            [tugboat.backends.base :as base])
  (:import [tugboat.backends.base BaseBackendAdapter]))

(declare db)

(def queue-prefix "_tugboat.queues.")

(defn kw->queue
  [kw]
  (str queue-prefix (name kw)))

(defn queue->kw    
  [q]
  (keyword (last (string/split q #"\.")))) 

(defrecord BackendAdapter []
  BaseBackendAdapter
  (enqueue [this queue func args]
    (let [redis-queue (kw->queue queue)
          db (:redis-db this)]
      (redis/rpush db redis-queue (base/encode-enqueue func args))))
  (get-next [this queues]
    (let [redis-queues (map kw->queue queues)
          db (:redis-db this)
           pair (redis/blpop db redis-queues 0)
          [queue payload] pair]
        (assoc {} :queue queue :payload payload))))

(defn create-adapter
  [config]
  (let [backend-conf (:backend config)
        queues (:queues config)
        redis-db (redis/init (:url backend-conf))]
    (assoc (BackendAdapter.) :redis-db redis-db :config (:backend config) :queues queues)))
