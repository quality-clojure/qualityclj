(defproject qualityclj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"}}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [ring "1.3.1"]
                 [compojure "1.1.9"]
                 [enlive "1.1.5"]
                 [om "0.7.1"]
                 [racehub/om-bootstrap "0.3.0" :exclusions [org.clojure/clojure]]
                 [prismatic/om-tools "0.3.2" :exclusions [org.clojure/clojure]]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.3.0"]
                 [com.datomic/datomic-pro "0.9.4899"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [lein-ring "0.8.11"]]

  :min-lein-version "2.0.0"

  ;; Uncomment for uberjar builds
  ;;:auto-clean false

  :ring {:init qualityclj.server/init
         :handler qualityclj.server/http-handler
         :port 10555}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/app.js"
                                        :output-dir    "resources/public/out"
                                        :source-map    "resources/public/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]}

  :profiles {:production {:ring {:open-browser? false
                                 :stacktraces? false
                                 :auto-reload? false}}
             :dev {:repl-options {:init-ns qualityclj.server}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :figwheel {:http-server-root "public"
                              :port 3449 }
                   :env {:is-dev true}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
