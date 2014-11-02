(defproject qualityclj "0.1.0-SNAPSHOT"
  :description "Assess Clojure libraries based on a number of different metrics."
  :url "https://github.com/quality-clojure/qualityclj"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [http-kit "2.1.18"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [hiccup "1.0.5"]
                 [reagent "0.4.2"]
                 [figwheel "0.1.5-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.1"]
                 [com.datomic/datomic-free "0.9.4899"]
                 [clj-jgit "0.8.0" :exclusions [org.clojure/core.memoize]]
                 [com.taoensso/timbre "3.3.1"]
                 [lib-noir "0.9.4"]
                 [liberator "0.12.2"]
                 [cljs-ajax "0.3.3"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [com.cemerick/clojurescript.test "0.3.1"]]

  :main qualityclj.handler

  :hooks [leiningen.cljsbuild]

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

  :profiles {:production {:ring {:open-browser? false
                                 :stacktraces? false
                                 :auto-reload? false}
                          :cljsbuild {:builds
                                      {:app
                                       {:compiler
                                        {:optimizations :advanced
                                         :pretty-print false}}}}}
             :dev {:repl-options {:init-ns qualityclj.repl}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :env {:is-dev true
                         :db-uri "datomic:mem://qualityclj"}
                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.1"]
                                  [com.cemerick/clojurescript.test "0.3.1"]]}

             :test {:env {:db-uri "datomic:mem://qualityclj"}
                    :dependencies [[com.cemerick/clojurescript.test "0.3.1"]]}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot [qualityclj.handler]
                       :cljsbuild {:builds
                                   {:app
                                    {:compiler
                                     {:optimizations :advanced
                                      :pretty-print false}}}}}})
