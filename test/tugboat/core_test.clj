(ns tugboat.core-test
  (:use clojure.test
        tugboat.core
        [tugboat.tasks.core :only (generate-task-id)])
  (:require [tugboat.backends.core :as backend-core]
            [tugboat.config :as config]
            [tugboat.client :as client]
            [tugboat.backends.base :as backend-base])
  (:import [tugboat.backends.base BaseBackendAdapter]))

(def test-item-queue (atom clojure.lang.PersistentQueue/EMPTY))
(defn clear-queue [f]
  (reset! test-item-queue clojure.lang.PersistentQueue/EMPTY)
  (f))
(def test-config {:backend {:type :test} 
                  :test-namespaces ["tugboat.task-tasks"]
                  :queues [:test1 :test2 :test3]})

(defrecord TestAdapter []
  BaseBackendAdapter
  (enqueue [this queue func args]
    (let [task-id (generate-task-id)]
      (dosync
        (swap! test-item-queue conj (backend-base/encode-enqueue task-id func args)))))
  (get-next [this queues]
    (dosync
      (let [item (first @test-item-queue)]
        (swap! test-item-queue pop)
        item))))

(defn create-test-adapter
  []
  (assoc (TestAdapter.) :queues (:queues test-config)))

(defn wrap-adapter
  [f]
  (with-redefs [backend-core/create (constantly (create-test-adapter))]
    (f)))

(use-fixtures :each clear-queue)
(use-fixtures :each wrap-adapter)

(deftest enqueue  
  (testing "enqueue"
    (config/configure test-config)
    (client/enqueue :test1 "tugboat.test-tasks.no-args")
    (is (= (count @test-item-queue) 1))))

(deftest get-next-item
  (testing "get-next-item"
    (config/configure test-config)
    (client/enqueue :test1 "tugboat.test-tasks.no-args")
    (let [backend-adapter (backend-core/create)
          queues (:queues test-config)]
      (.get-next backend-adapter queues))))
