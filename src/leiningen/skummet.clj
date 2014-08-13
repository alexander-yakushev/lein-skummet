(ns leiningen.skummet
  (:require [clojure.java.shell :as sh]
            [clojure.java.io :as jio]
            [leiningen.core.project :as pr]
            [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [leiningen.core.classpath :as cp]
            leiningen.clean
            leiningen.compile
            leiningen.run
            leiningen.uberjar))

(defn skummet
  [project & [subtask & args]]
  (cond
   (= subtask "compile")
   (do
     (leiningen.clean/clean project)
     (if-let [namespaces (cons 'clojure.core (seq (leiningen.compile/stale-namespaces project)))]
       (let [form `(let [lean-var?# (fn [var#]
                                      (not (#{~@(:skummet-skip-vars project)}
                                            (str var#))))]
                     (push-thread-bindings {#'clojure.core/*loaded-libs* (ref (sorted-set))})
                     (try
                       (binding [~'*lean-var?* lean-var?#
                                 ~'*lean-compile* true
                                 *compiler-options* {:elide-meta [:doc :file :line :added :arglists
                                                                  :column :static :author :added]}]
                         (doseq [namespace# '~namespaces]
                           (println "Compiling" namespace#)
                           (clojure.core/compile namespace#)))
                       (finally (pop-thread-bindings))))
             project (-> project
                         (update-in [:prep-tasks]
                                    (partial remove #{"compile"}))
                         (assoc :jvm-opts ["-Dclojure.compile.ignore-lean-classes=true"]))]
         (try (eval/eval-in-project project form)
              (catch Exception e
                (main/abort "Compilation failed:" (.getMessage e)))))
       (main/debug "All namespaces already AOT compiled.")))

   (= subtask "run")
   (let [deps-line (->> (cp/resolve-dependencies :dependencies project)
                        (cons (jio/file (:target-path project) "classes"))
                        (interpose ":") (apply str))]
     (apply eval/sh "java" "-cp" deps-line (str (:main project)) args))

   (= subtask "jar")
   (leiningen.uberjar/uberjar project)

   :else
   (main/abort "Wrong subtask:" subtask)))
