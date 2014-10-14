(ns qualityclj.views.layout
  (:require [environ.core :refer [env]]
            [hiccup.page :as page
            :refer [html5 include-css include-js]]))

(def is-dev? (env :is-dev))

(def devmode-html
  [(include-js "http://fb.me/react-0.11.2.js")
   (include-js "/js/out/goog/base.js" "js/app.js")
   [:script {:type "text/javascript"} "goog.require('qualityclj.core')"]])

(def nav
  "Basic navigation header."
  [:nav.navbar.navbar-default {:role "navigation"}
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand {:href "/"}
      "Quality Clojure"]]
    [:ul.nav.navbar-nav
     [:li
      [:a {:href "/about"} "About"]]
     [:li
      [:a {:href "/repo"} "Repos"]]]]])

(def header
  "Head definition for each page."
  [:head
   [:title "Quality Clojure"]
   (include-css
    "https://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
    "/css/main.css"
    "/css/code.css")])

(defn common
  "Common layout, to include site CSS, application JS, and dev-mode JS
  if in dev mode."
  [& body]
  (html5
   header
   [:body (when is-dev? {:class "is-dev"})
    nav
    body
    (if is-dev?
      (seq devmode-html)
      (include-js "js/app.js"))]))
