(ns sample.core
  ;; (:require [clojure.core.async :as a])
  (:gen-class))

(defn wont-be-lean []
  (str "foo" "bar"))

(defn foo
  "I don't do a whole lot."
  [x]
  (println "Hello, World!" x)
  (println (wont-be-lean))
  ;; (let [c (a/chan)]
  ;;   (a/put! c x)
  ;;   (a/go (println "Ultimate answer is" (a/<! c))))
  )

(defn -main [& args]
  (foo (or (first args) 42)))
