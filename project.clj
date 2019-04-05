(defproject littlescribe "0.1.0"
  :description "LittleScribe"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [cli-matic "0.3.6"]
                 [org.clojure/data.csv "0.1.4"]
                 [cljstache  "2.0.1"]]

  :plugins [[lein-eftest "0.5.1"]
            [lein-cljfmt "0.5.7"]]

  :main littlescribe.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
