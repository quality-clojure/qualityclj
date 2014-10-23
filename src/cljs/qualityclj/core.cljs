(ns qualityclj.core
  (:require [figwheel.client :as figwheel :include-macros true]
            [reagent.core :as reagent :refer [atom]]
            [weasel.repl :as weasel]
            [ajax.core :refer [GET]]))

(def app-state (atom {}))

(defn note-source [source]
  (get {"note.source/kibit" "Kibit"
        "note.source/eastwood" "Eastwood"
        "note.source/bikeshed" "Bikeshed"} source))

(defn note-title [note]
  (str "Note - " (note-source (:note/source note))))

(defn note-bubble [note]
  [:div.panel.panel-default.note-bubble
   [:div.panel-heading
    [:h3.panel-title (note-title note)]]
   [:div.panel-body
    (:note/content note)]])

(defn handle-notes
  "Handler for ajax responses."
  [response]
  (reset! app-state {:notes response})
  (doseq [anote (:notes @app-state)]
    (let [elem (. js/document (getElementById
                               (str "line-" (:note/line-number anote))))
          new-span (.createElement js/document "span")
          p-elem (.-parentNode elem)]
      (.insertBefore p-elem new-span elem)
      (reagent/render-component (note-bubble anote) new-span))))

(when-let [elem (. js/document (getElementById "filepath"))]
  (GET (str "/note/" (.-innerHTML elem))
      {:handler handle-notes
       :response-format :json
       :keywords? true}))

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
