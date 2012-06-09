(ns tugboat.config)

(def conf (ref {}))

(defn configure
  [conf-map]
  (dosync
    (alter conf merge conf-map)))
