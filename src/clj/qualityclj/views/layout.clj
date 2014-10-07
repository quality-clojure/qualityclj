(ns qualityclj.views.layout
  (:require [environ.core :refer [env]]
            [hiccup.page :as page
            :refer [html5 include-css include-js]]))

(def is-dev? (env :is-dev))

(def devmode-html
  [(include-js "http://fb.me/react-0.11.2.js")
   (include-js "/js/out/goog/base.js" "js/app.js")
   [:script {:type "text/javascript"} "goog.require('qualityclj.core')"]])

(defn header []
  [:nav.navbar.navbar-default {:role "navigation"}
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand {:href "/"}
      "Quality Clojure"]]
    [:ul.nav.navbar-nav
     [:li
      [:a {:href "/about"} "About"]]]]])

(defn common [& body]
  (html5
   [:head
    [:title "Quality Clojure"]
    (header)
    (include-css
     "https://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
     "/css/main.css"
     "/css/code.css")]
   [:body (when is-dev? {:class "is-dev"})
    body
    (if is-dev?
      (seq devmode-html)
      (include-js "js/app.js"))]))
