(defproject lein-skummet-sample "0.0.1-SNAPSHOT"
  :dependencies [[org.skummet/clojure "1.7.0-r1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :exclusions [org.clojure/clojure]
  :profiles {:default []}
  :plugins [[org.skummet/lein-skummet "0.2.2"]]
  :main sample.core
  :aot :all
  :skummet-skip-vars ["#'leantest.core/wont-be-lean"])
