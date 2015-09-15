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

(defn eval-in-project
  ([project form init]
   (eval/prep project)
   (eval/eval-in project
                 `(do ~@(map (fn [[k v]] `(set! ~k ~v)) (:global-vars project))
                      ~init
                      ~@(:injections project)
                      ~form)))
  ([project form] (eval-in-project project form nil)))

(defn skummet
  [project & [subtask & args]]
  (let [project (-> project
                    (vary-meta assoc-in [:profiles ::skummet]
                               {:prep-tasks []
                                :auto-clean false
                                :jvm-opts ["-Dclojure.compile.ignore-lean-classes=true"]})
                    (pr/merge-profiles [::skummet]))]
    (cond
      (= subtask "compile")
      (do
        (leiningen.clean/clean project)
        (if-let [namespaces (seq (leiningen.compile/stale-namespaces project))]
          (let [form `(let [lean-var?# (fn [var#] (not (#{~@(:skummet-skip-vars project)}
                                                       (str var#))))]
                        (binding [~'clojure.core/*lean-var?* lean-var?#
                                  ~'clojure.core/*lean-compile* true

                                  ~'clojure.core/*compiler-options*
                                  {:elide-meta [:doc :file :line :added :arglists
                                                :column :static :author :added]}]
                          (doseq [namespace# '~namespaces]
                            (println "Compiling" namespace#)
                            (compile namespace#))))]
            (try (eval-in-project project form)
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
      (main/abort "Wrong subtask:" subtask))))
