(ns qualityclj.core
  (:require [figwheel.client :as figwheel :include-macros true]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as d :include-macros true]
            [weasel.repl :as weasel]
            [om-bootstrap.button :as b]
            [om-bootstrap.grid :as g]
            [om-bootstrap.random :as r]
            [ajax.core :refer [GET]]))

(def app-state (atom {}))

(defn update-state
  "Handler for ajax responses."
  [response]
  (reset! app-state {:notes response}))

(defn note-source [source]
  (get {"note.source/kibit" "Kibit"
        "note.source/eastwood" "Eastwood"
        "note.source/bikeshed" "Bikeshed"} source))

(defn note-title [note]
  (str "Note - " (note-source (:note/source note))))

;; This is obviously a bit hacky:
;; font-size is 13px, line-height is 1.42857,
;; and the -50 offset is to make sure the arrow is a the right line,
;; instead of the top of the popover
(defn note-position [line]
  (int (+ 100 (* 13 line 1.38))))

(defcomponent note [note owner]
  (render [_]
          (r/popover {:placement "right"
                      :position-top (note-position (:note/line-number note))
                      :position-left 700
                      :title (note-title note)}
                     (:note/content note))))

(defcomponent notes [app owner]
  (render [_]
          (d/div (om/build-all note
                               (get-in app [:notes])))))

(when-let [elem (. js/document (getElementById "notes"))]
  (GET (str "/note/" (.. js/document (getElementById "filepath") -innerHTML))
      {:handler update-state
       :response-format :json
       :keywords? true})
  (om/root notes app-state {:target elem}))

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
