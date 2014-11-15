(defproject qualityclj "0.2.3-SNAPSHOT"
  :description "Assess Clojure libraries based on a number of different metrics."
  :url "https://github.com/quality-clojure/qualityclj"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.19"]
                 [compojure "1.2.1"]
                 [ring/ring-defaults "0.1.2"]
                 [hiccup "1.0.5"]
                 [reagent "0.4.3"]
                 [figwheel "0.1.5-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [com.datomic/datomic-free "0.9.4899"]
                 [weasel "0.4.2"]
                 [clj-jgit "0.8.2"]
                 [liberator "0.12.2"]
                 [cheshire "5.3.1"]
                 [cljs-ajax "0.3.3"]
                 [prismatic/dommy "1.0.0"]
                 [com.taoensso/timbre "3.3.1"]
                 [com.taoensso/encore "1.14.0"]
                 [com.taoensso/sente "1.2.0"]
                 [conrad "0.1.0"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [com.cemerick/clojurescript.test "0.3.1"]]

  :deploy-repositories [["releases" :clojars]]

  :main qualityclj.handler

  :cljsbuild {:test-commands {"test" ["phantomjs"
                                      :runner "resources/private/js/polyfill.js"
                                      "resources/private/js/test.js"]}
              :builds
              {:app {:source-paths ["src/cljs"]
                     :compiler {:output-to     "resources/public/js/app.js"
                                :output-dir    "resources/public/js/out"
                                :source-map    "resources/public/js/out.js.map"
                                :preamble      ["reagent/react.min.js"]
                                :externs       ["reagent/externs/react.js"]
                                :optimizations :none
                                :pretty-print  true}}
               :test {:source-paths ["src/cljs" "test/cljs"]
                      :compiler {:preamble      ["reagent/react.js"]
                                 :output-to     "resources/private/js/test.js"
                                 :externs       ["reagent/externs/react.js"]
                                 :optimizations :simple
                                 :pretty-print true}}}}

  :profiles {:dev {:repl-options {:init-ns qualityclj.repl}
                   :plugins [[lein-figwheel "0.1.5-SNAPSHOT"]]
                   :env {:is-dev true
                         :db-uri "datomic:mem://qualityclj"}
                   :dependencies [[com.cemerick/clojurescript.test "0.3.1"]]}

             :test {:env {:db-uri "datomic:mem://qualityclj-test"}
                    :dependencies [[com.cemerick/clojurescript.test "0.3.1"]]}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :omit-source true
                       :aot [qualityclj.handler]
                       :cljsbuild {:builds
                                   {:app
                                    {:compiler
                                     {:optimizations :advanced
                                      :pretty-print false}}}}}})
