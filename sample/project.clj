(defproject lein-skummet-sample "0.0.1-SNAPSHOT"
  :dependencies [[org.skummet/clojure "1.7.0-beta3-r1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha" :exclusions [org.clojure/clojure]]]
  :profiles {:default []}
  :plugins [[org.skummet/lein-skummet "0.2.1"]]
  :main sample.core
  :aot :all
  :skummet-skip-vars ["#'leantest.core/wont-be-lean"])
