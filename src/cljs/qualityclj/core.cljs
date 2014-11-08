(ns qualityclj.core
  (:require [ajax.core :refer [GET]]
            [cljs.core.async :refer [<! >! put! chan]]
            [figwheel.client :as figwheel :include-macros true]
            [reagent.core :as reagent :refer [atom]]
            [taoensso.sente :as sente]
            [dommy.core :as dommy :refer-macros [sel1]]
            [weasel.repl :as weasel])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def app-state (atom {}))
(def import-status (atom {}))
(def router_ (atom nil))

(defn note-source [source]
  (get {"note.source/kibit" "Kibit"
        "note.source/eastwood" "Eastwood"
        "note.source/conrad" "Conrad"} source))

(defn note-title [note]
  (str "Note - " (note-source (:note/source note))))

(defn note-bubble [note]
  [:div.panel.panel-info.note-bubble
   [:div.panel-heading
    [:h3.panel-title (note-title note)]]
   [:div.panel-body
    (:note/content note)]])

(defn handle-notes
  "Handler for ajax responses."
  [response]
  (swap! app-state
         (fn [old-state new-notes]
           (assoc old-state :notes new-notes))
         response)
  (doseq [anote (:notes @app-state)]
    (let [elem (sel1 (str "#line-" (dec (:note/line-number anote))))
          new-span (.createElement js/document "span")]
      (dommy/append! elem new-span)
      (reagent/render-component (note-bubble anote) new-span))))

(when-let [elem (sel1 :#filepath)]
  (GET (str "/note/" (.-innerHTML elem))
      {:handler handle-notes
       :response-format :json
       :keywords? true}))

(defn import-flash
  "An asynchronous alert flash to let the user know how an import is going."
  []
  [:div {:class (str "alert alert-" (:type @import-status)) :role "alert"}
   (:message @import-status)])

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys [event]}]
  (js/console.log (str "Unhandled event: " event)))

(defmethod event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (if (:first-open? ?data)
    (js/console.log "Channel socket successfully established!")))

(defmethod event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (let [{:keys [type message]} (second ?data)]
    (reset! import-status {:type (name type) :message message})))

(defn stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (sente/start-chsk-router! ch-chsk event-msg-handler)))

(when-let [elem (sel1 :#alert)]
  (reagent/render-component [import-flash] elem)
  (start-router!))

(when-let [elem (sel1 :#import-btn)]
  (dommy/listen!
   elem :click
   (fn [ev]
     (let [git-url (.-value (sel1 :#git-url))
           submit-btn (sel1 :#import-btn)]
       (when-not (empty? git-url)
         (dommy/set-attr! (sel1 :#import-btn) :disabled)
         (js/console.log (str "Importing git repo: " git-url))
         (chsk-send! [:import/request {:url git-url}]))))))

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
