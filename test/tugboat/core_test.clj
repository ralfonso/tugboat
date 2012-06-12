(ns tugboat.core-test
  (:use clojure.test
        tugboat.core
        [tugboat.tasks.core :only (generate-task-id)])
  (:require [tugboat.backends.core :as backend-core]
            [tugboat.config :as config]
            [tugboat.client :as client]
            [tugboat.worker :as worker]
            [tugboat.backends.base :as backend-base])
  (:import [tugboat.backends.base BaseBackendAdapter BaseResultAdapter]))

(def test-results (atom {}))  
(def test-config {:backend {:type :test} 
                  :test-namespaces ["tugboat.task-tasks"]
                  :queues [:test1 :test2 :test3]})

(def test-item-queue (reduce conj (for [queue (:queues test-config)] {queue (atom clojure.lang.PersistentQueue/EMPTY)})))

(defn pop-item-return
  [p-queue]
  (dosync
    (let [item (first @p-queue)]
      (swap! p-queue pop)
      item)))

(defrecord TestAdapter []
  BaseBackendAdapter
  (enqueue [this queue func args]
    (let [task-id (generate-task-id)
          p-queue (get test-item-queue queue)]
      (dosync
        (swap! p-queue conj (backend-base/encode-enqueue task-id func args)))))
  (get-next [this queues]
    (loop [item nil
           queue nil
           my-queues queues]
      (if (nil? item)
        (let [queue (first my-queues)
              p-queue (get test-item-queue queue)
              queue-item (pop-item-return p-queue) ]
          (recur queue-item queue (next my-queues)))
        {:queue queue :payload item})))

  BaseResultAdapter
  (set-result [this task-id result]
    (dosync
      (swap! test-results merge {task-id result})))
  (get-result [this task-id]
    (get test-results task-id)))

(defn create-test-adapter
  []
  (assoc (TestAdapter.) :queues (:queues test-config)))

(defn wrap-config
  [f]
  (config/configure test-config)
  (f))

(defn clear-queue [f]
  (doseq [p-queue (vals test-item-queue)]
    (reset! p-queue clojure.lang.PersistentQueue/EMPTY))
  f)

(defn wrap-adapter
  [f]
  (with-redefs [backend-core/create (constantly (create-test-adapter))]
    f))

(use-fixtures :once wrap-config)
(use-fixtures :each (fn [f] (-> (clear-queue f) wrap-adapter)))

(deftest enqueue  
  (testing "enqueue"
    (client/enqueue :test1 "tugboat.test-tasks/no-args")
    (is (= (count @(:test1 test-item-queue)) 1))))

(deftest get-next-item
  (testing "get-next-item"
    (client/enqueue :test1 "tugboat.test-tasks/no-args")
    (let [adapter (backend-core/create)
          queues (:queues test-config)]
      (.get-next adapter queues))))

(deftest test-result
  (testing "test result"
    (let [adapter (backend-core/create)
          task-id (client/enqueue :test1 "tugboat.test-tasks/sum" [5 10])]
      (worker/get-work-unit-and-execute adapter adapter (:queues test-config))
      (let [result (.get-result adapter task-id)]
        (is (= (:value result) 15))))))
