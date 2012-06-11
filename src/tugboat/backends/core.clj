(ns tugboat.backends.core
  (:import (java.io FileNotFoundException)))

(defn load-backend
  [config backend-role]
  (if-let [backend-conf (backend-role (:backends config))]
    (let [backend-type (:type backend-conf)
          backend-ns (str "tugboat.backends." (name backend-type))]
      (try 
        (let [backend-sym (symbol backend-ns)]
          (do
            (require backend-sym)

            ;; FIXME call create-adapter on our adapter, this resolution code smells
            ((ns-resolve backend-sym (symbol "create-adapter")) config backend-role)))
        (catch FileNotFoundException e (throw (Exception. (format "Unable to load Tugboat backend: %s (%s)" backend-ns e))))))))

(defn create
  [config backend-role]
  (load-backend config backend-role))
