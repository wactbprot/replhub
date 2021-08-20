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

(defn nice-repl-doc-id [s] (string/replace s  #"\-\-" " &nbsp; &nbsp;⟶ &nbsp; &nbsp;"))

(defn nice-date [s] (string/replace s #"[TZ]" "&nbsp;&nbsp;&nbsp;"))

(defn table-row [m]
  [:tr
   [:td (nice-repl-doc-id (:doc_id m))]
   [:td (:state m)]
   [:td (:changes_pending (:info m))] 
   [:td (:error_count m)]
   [:td (nice-date (:start_time m))]
   [:td (nice-date (:last_updated m))]])


(defn table [v]
  [:table.uk-table.uk-table-hover.uk-table-striped
   [:thead [:tr
            [:th "replication"]
            [:th "status"]
            [:th {:uk-tooltip "pending changes"} "cp"]
            [:th {:uk-tooltip "error count"} "ec"]
            [:th "start time"]
            [:th "last updated"]]]
   (into [:tbody] (map table-row v))])

(defn state-summary [v]
  (into [:span.uk-text-muted.uk-text-center]
        (mapv
         (fn [[state number]]  (str state ": " number "  "))
         (frequencies (mapv :state v)))))

(defn li [m]
  (let [title (first (keys m))
        data  (first (vals m))
        sum   (state-summary data)]
    [:li
     [:div.uk-accordion-title {:uk-grid ""}
      [:div.uk-text-muted.uk-text-left sum]
      [:div.uk-width-expand.uk-grid-column-medium.uk-text-right title]]
     [:div.uk-accordion-content (table data)]]))
  
(defn accord [conf data]
  (into [:ul {:uk-accordion ""}] (mapv li data)))

(defn body [conf data content]
  [:body#body
   (nav conf data)
   [:div.uk-container.uk-padding
    [:article.uk-article
     [:h3.uk-article-title.uk-text-uppercase.uk-heading-line.uk-text-center [:a.uk-link-reset {:href ""} "replication state"]]
     [:p.uk-article-meta (.format (new java.text.SimpleDateFormat "yyyy-MM-dd HH:mm") (java.util.Date.))]
     [:p.uk-text-lead
      content]]]
    (hp/include-js "/js/jquery.js")
    (hp/include-js "/js/uikit.js")
   (hp/include-js "/js/uikit-icons.js")])

(defn head [conf data]
  [:head [:title "repliclj"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.css")])

(defn index [conf data content]
  (hp/html5
   (head conf data)
   (body conf data (condp = content
                     :table (accord conf data)))))
