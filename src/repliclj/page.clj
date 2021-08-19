(ns repliclj.page
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [clojure.string :as string]))

(defn not-found []
  (hp/html5
   [:h1 "404 Error!"]
   [:b "Page not found!"]
   [:p [:a {:href ".."} "Return to main page"]]))


(defn nav [conf data]
  [:div.uk-navbar-container
   {:uk-navbar ""}
   [:div.uk-navbar-center
    [:ul.uk-navbar-nav
      [:li [:a {:uk-icon "icon: github-alt"
                :target "_blank"
                :href "https://github.com/wactbprot/repliclj"}]]
      [:li [:a {:target "_blank"
                :href "http://a75438:5601/app/discover"} "elasticsearch"]]]]])

(defn body [conf data]
  [:body#body
   (nav conf data)
   [:pre
   (str data)]
   
   (hp/include-js "/js/jquery.js")
   (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")])

(defn head [conf data]
  [:head [:title "repliclj"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.css")])

(defn index [conf data] (hp/html5 (head conf data) (body conf data)))
