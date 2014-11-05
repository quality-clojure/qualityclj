(ns qualityclj.routes.import
  (:require [qualityclj.import :as import]
            [taoensso.sente :refer [make-channel-socket! start-chsk-router!]]
            [clojure.core.async :refer [<! >! go put! close! go-loop chan]]
            [compojure.core :refer [defroutes GET POST]]
            [taoensso.timbre :refer [warn info]]))

(defonce router_ (atom nil))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (make-channel-socket! {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (warn (format "Unhandled event: %s" event))
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod event-msg-handler :import/request
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)
        git-url (:url ?data)]
    (info (format "Importing repo: %s" git-url))
    (go (>! import/in-chan {:id :import :url git-url}))))

;;swallow a ping
(defmethod event-msg-handler :chsk/ws-ping [_])

(defn send-status
  "Send a status update to the browser to let the user know what's going on.

  type is a keyword, one of: #{:success :info :warning :danger}"
  [uid msg type]
  (chsk-send! uid [:import/status {:type type :message msg}]))

(defn stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (start-chsk-router! ch-chsk event-msg-handler)))

(start-router!)

(defroutes import-routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req)))

(go-loop [msg (<! import/out-chan)]
  (let [{:keys [message type]} msg]
    (send-status nil message type))
  (recur (<! import/out-chan)))
