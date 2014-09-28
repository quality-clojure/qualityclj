(ns qualityclj.core
  (:require [figwheel.client :as figwheel :include-macros true]
            [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [om-tools.dom :as d :include-macros true]
            [weasel.repl :as weasel]
            [om-bootstrap.button :as b]
            [om-bootstrap.grid :as g]
            [om-bootstrap.random :as r]))

(def app-state
  (atom {:repo {:name "cljagents"
                :url "https://github.com/jcsims/cljagents"
                :file {:filename "prob5.clj"
                       :filepath "src/clj/cljagents"
                       :language "Clojure"
                       :notes [{:line 135
                                :type :note
                                :source :kibit
                                :content (str "Consider using: `(+ % 105 "
                                              "(rand-int 150)` instead.")}
                               {:line 5
                                :type :note
                                :source :eastwood
                                :content (str "Unlimited use of "
                                              "([cljagents.agent] "
                                              "[clojure.core.logic]) in "
                                              "cljagents.prob5")}
                               {:line 35
                                :type :note
                                :source :bikeshed
                                :content "Needs a docstring."}
                               {:line 53
                                :type :note
                                :source :bikeshed
                                :content "Needs a docstring."}
                               {:line 70
                                :type :note
                                :source :bikeshed
                                :content "Needs a docstring."}
                               {:line 79
                                :type :note
                                :source :bikeshed
                                :content "Needs a docstring."}
                               {:line 12
                                :type :comment
                                :content (str "Consider refactoring to pass "
                                              "state instead of using global "
                                              "atoms.")}]}}}))

(defn note-source [{:keys [source]}]
  (get {:kibit "Kibit"
        :eastwood "Eastwood"
        :bikeshed "Bikeshed"} source))

(defn note-title [note]
  (let [type (:type note)
        prefix (get {:note "Note" 
                     :comment "Comment"} type)]
    (if (= type :comment)
      prefix
      (str prefix " - " (note-source note)))))

;; This is obviously a bit hacky:
;; font-size is 13px, line-height is 1.42857,
;; and the -50 offset is to make sure the arrow is a the right line,
;; instead of the top of the popover
(defn note-position [line]
  (int (- (* 13 line 1.38) 45)))

(defcomponent note [note owner]
  (render [_]
          (r/popover {:placement "right"
                      :position-top (note-position (:line note))
                      :title (note-title note)}
                     (:content note))))

(defcomponent notes [app owner]
  (render [_]
          (d/div (om/build-all note
                               (get-in app [:repo :file :notes])))))

(defcomponent file-header [app owner]
  (render [_]
          (g/row
           {}
           (g/col {:xs 12}
                  (d/h3 (get-in app [:repo :name])
                        (d/small (str " | " (get-in app [:repo :file :filename]))))))))

(defcomponent jumbo [app owner]
  (render [_]
          (r/jumbotron {}
                       (d/h1 "Welcome to Quality Clojure!")
                       (d/p (str "This isn't actually Quality Clojure, "
                                 "this is simply a tribute. "
                                 "Look below for a sample."))
                       (d/p (d/a {:class "btn btn-primary btn-large active"
                                  :href "about"} "A tribute?")))))


(let [elem (. js/document (getElementById "jumbo"))]
  (when elem (om/root jumbo app-state {:target elem})))

(let [elem (. js/document (getElementById "file-header"))]
  (when elem (om/root file-header app-state {:target elem})))

(let [elem (. js/document (getElementById "notes"))]
  (when elem (om/root notes app-state {:target elem})))

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
