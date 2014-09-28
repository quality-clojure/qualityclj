(ns qualityclj.views.layout
  (:require[environ.core :refer [env]]
           [hiccup.page :refer [html5 include-css include-js]]))

(def is-dev? (env :is-dev))

(def devmode-html
  [(include-js "http://fb.me/react-0.11.2.js")
   (include-js "/out/goog/base.js" "app.js")
   [:script {:type "text/javascript"} "goog.require('qualityclj.core')"]])

(defn common [& body]
  (html5
   [:head
    [:title "Welcome to qualityclj"]]
   [:body (when is-dev? {:class "is-dev"})
    [:div#app]
    (if is-dev?
      (seq devmode-html)
      (include-js "app.js"))
    (include-css
     "https://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
     "/css/screen.css"
     "/css/code.css")
    body]))
