(ns qualityclj.repl
  (:require [qualityclj.handler :refer [app init]]
            [cemerick.piggieback :as piggieback]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.file :refer [wrap-file]]
            [taoensso.timbre :as timbre :refer [info spy]]
            [weasel.repl.websocket :as weasel]))

(defonce server (atom nil))

(defn browser-repl []
  (piggieback/cljs-repl :repl-env (weasel/repl-env :ip "0.0.0.0" :port 9001)))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ;; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ;; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (init)
    (reset! server (run-server (get-handler) {:port port}))
    (info (str "Server started. View the site at http://localhost:" port))))

(defn stop-server []
  (spy :info "Server stopping." (@server :timeout 100))
  (reset! server nil))
