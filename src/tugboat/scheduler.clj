(ns tugboat.scheduler
  (:use [overtone.at-at :only [mk-pool every]]
        [clj-time.core :only [now interval plus in-msecs]]
        [clojure.tools.logging :only (debugf infof errorf)]
        [tugboat.client :only [enqueue]])
  (:import (org.joda.time Interval ReadablePeriod)))

(declare schedule-pool)

(defn determine-interval
  [#^ReadablePeriod period]
  (let [cur (now)
        delta (plus cur period)]
    (interval cur delta)))        

(defn scheduled-enqueue
  [queue task args]
  (let [task-id (enqueue queue task args)]
    (debugf "Scheduled task enqueued %s[%s]" task task-id)))

(defn schedule-task
  [scheduled-item]
  (let [task (:task scheduled-item)
        queue (:queue scheduled-item)
        schedule-exp (:schedule scheduled-item)
        args (:args scheduled-item)]
    (isa? (class schedule-exp) ReadablePeriod)
      (let [schedule-interval (determine-interval schedule-exp)]
        (debugf  "Adding task %s to schedule" task)
        (every (in-msecs schedule-interval) #(scheduled-enqueue queue task args) schedule-pool))))

(defn run-scheduler
  [config]
  (if-let [schedule-conf (:schedule config)]
    (do
      (def schedule-pool (mk-pool))
      (doseq [scheduled-item schedule-conf]
        (schedule-task scheduled-item)))))
