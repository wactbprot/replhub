(ns repliclj.page
  ^{:author "Thomas Bock <wactbprot@gmail.com>"
    :doc "Simple replication state overview page delivered by server.clj."}
  (:require [hiccup.form :as hf]
            [hiccup.page :as hp]
            [repliclj.utils :as u]
            [clojure.string :as string]))

(defn not-found []
  (hp/html5
   [:h1 "404 Error!"]
   [:b "Page not found!"]
   [:p [:a {:href ".."} "Return to main page"]]))

;;........................................................................
;; nav
;;........................................................................
(defn nav [conf data]
  [:div.uk-navbar-container
   {:uk-navbar ""}
   [:div.uk-navbar-center
    [:ul.uk-navbar-nav
      [:li [:a {:uk-icon "icon: github-alt"
                :target "_blank"
                :href "https://github.com/wactbprot/repliclj"}]]
      [:li [:a {:target "_blank"
                :href "http://a75438:5601/app/discover#/view/6fde0090-06ff-11ec-a0ed-9fa5b8b37aed"} "elasticsearch"]]
     [:li [:a {:uk-icon "icon: file-text"
               :target "_blank"
                :href "https://docs.couchdb.org/en/main/replication/index.html"}]]]]])


;;........................................................................
;; graph
;;........................................................................
(defn graph [conf data] [:div.uk-container {:id "graph" :style "height:960px;"}])

;;........................................................................
;; table
;;........................................................................
(defn table-row [m]
  [:tr
   [:td (u/url->db (:source m))]
   [:td (u/url->db (:target m))]
   [:td (u/url->host-name (:target m))]
   [:td (:state m)]
   [:td (:changes_pending (:info m))]
   [:td (:error_count m)]
   [:td (u/nice-date (:start_time m))]
   [:td (u/nice-date (:last_updated m))]
   [:td [:a {:target "_blank"
             :uk-icon "link"
             :href (u/repli-doc-link m)}]]])

(defn table [v]
  [:table.uk-table.uk-table-hover.uk-table-striped
   [:thead [:tr
            [:th "source db"]
            [:th "target db"]
            [:th "target host"]
            [:th "status"]
            [:th {:uk-tooltip "pending changes"} "pc"]
            [:th {:uk-tooltip "error count"} "ec"]
            [:th "start time"]
            [:th "last updated"]
            [:th.uk-table-shrink "doc"]]]
   (into [:tbody] (map table-row v))])

(defn state-summary [v]
  (into [:span.uk-text-muted.uk-text-center]
        (mapv
         (fn [[state number]]  (str   state ":&nbsp;"number "&nbsp;&nbsp;&nbsp;"))
         (frequencies (mapv :state v)))))

(defn db-info [v]
  (into [:ul.uk-breadcrumb]
        (mapv (fn [db] [:li [:span (:key db) "&nbsp;"] [:span.uk-badge (:doc_count (:info db))]])
              v)))

(defn li [m]
  (let [data  (:docs m)
        sum   (state-summary data)]
    [:li
     [:div.uk-accordion-title {:uk-grid ""}
      [:div.uk-text-muted.uk-text-left (if (seq data) sum "no data")]
      [:div.uk-width-expand.uk-grid-column-medium.uk-text-right
       (:alias m) [:span.uk-text-muted (u/host->host-name (:server m))]]]
     (when (seq data)
       [:div.uk-accordion-content
        (db-info (:db-info m))
        (table data)])]))

(defn accord [conf data] (into [:ul {:uk-accordion ""}] (mapv li data)))

;;........................................................................
;; body
;;........................................................................
(defn body [conf data content libs]
  (into [:body#body
         (nav conf data)
         [:div.uk-container.uk-padding.uk-margin
          [:article.uk-article
           [:h4.uk-article-title.uk-text-uppercase.uk-heading-line.uk-text-center.uk-text-muted
            [:a.uk-link-reset {:href ""} "replication state"]]
           [:p.uk-article-meta (u/date)]
           [:p.uk-text-lead
            content]]]] libs))

;;........................................................................
;; head
;;........................................................................
(defn head [conf data]
  [:head [:title "repliclj"]
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (hp/include-css "/css/uikit.css")])

;;........................................................................
;; index
;;........................................................................
(defn index [conf data content]
  (hp/html5
   (head conf data)
   (body conf data
         ;; content
         (condp = content
           :table (accord conf data)
           :graph (graph conf data))
         ;; libs
         (condp = content
           :table [(hp/include-js "/js/uikit.js")
                   (hp/include-js "/js/uikit-icons.js")]

           :graph [(hp/include-js "/js/vis-network.min.js")
                   (hp/include-js "/js/graph.js")
                   (hp/include-js "/js/jquery.js")
                   (hp/include-js "/js/uikit.js")
                   (hp/include-js "/js/uikit-icons.js")]))))
