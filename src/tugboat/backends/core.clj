(ns tugboat.backends.core
  (:import (java.io FileNotFoundException)))

(defn load-backend
  [config]
  (let [backend-type (:type (:backend config))
        backend-ns (str "tugboat.backends." (name backend-type))]
    (try 
      (let [backend-sym (symbol backend-ns)]
        (do
          (require backend-sym)

          ;; FIXME call create-adapter on our adapter, this resolution code smells
          ((ns-resolve backend-sym (symbol "create-adapter")) config)))
      (catch FileNotFoundException e (throw (Exception. (format "Unable to load Tugboat backend: %s (%s)" backend-ns e)))))))

(defn create
  [config]
  (load-backend config))
