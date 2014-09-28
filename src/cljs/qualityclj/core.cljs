(ns qualityclj.core
  (:require [figwheel.client :as figwheel :include-macros true]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as d :include-macros true]
            [weasel.repl :as weasel]
            [om-bootstrap.button :as b]
            [om-bootstrap.random :as r]))

(def app-state
  (atom {:repo {:name "riemann"
                :url "https://github.com/aphyr/riemann"
                :file {:filename "core.clj"
                       :filepath "src/riemann"
                       :language "Clojure"
                       :notes [{:line 2
                                :type :note
                                :content "I'm a note!! And I'm also quite long."}
                               {:line 5
                                :type :comment
                                :content "I'm a comment!"}]}}}))

(defn note-title [{:keys [type]}]
  (get {:note "Note"
        :comment "Comment"} type))

(defn note-style [{:keys [type]}]
  (get {:note "default"
        :comment "primary"} type))

(defcomponent note [note owner]
  (render [_]
          (r/popover {:placement "right"
                      :position-left 500
                      :position-top (* 50 (:line note))
                      :title (note-title note)}
                     (:content note))))

(defcomponent file [app owner]
  (render [_ ]
          (d/h2 (get-in app [:repo :name]))
          (d/h3 (get-in app [:repo :file :filename]))
          (d/div {:style {:height 120}}
           (om/build-all note (get-in app [:repo :file :notes])))))

(defcomponent jumbo [app owner]
  (render [_]
          (d/div
           (r/jumbotron {}
                        (d/h1 "Quality Clojure")
                        (d/p (str "Assess a Clojure library's quality based "
                                  "on a few different metrics."))
                        (d/p "Coming soon!")))))


(om/root file app-state {:target (. js/document (getElementById "app"))})

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
