(ns kee-frame-repro.web
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.http-fx]
    [ajax.core :as ajax]
    [kee-frame.core :as kf]))


;; >> The home page

(defn home-view []
  [:div 
   [:p "Scroll down to see the links"]
   [:div {:style {:height "2000px"}}]
   [:a {:href (kf/path-for [:broken])} "Link to broken view"]
   [:br]
   [:a {:href (kf/path-for [:working])} "Link to working view"]])



;; >> A view to inherit from
;; This is is to show it's the exact same view that has this problem
;; Just the distatch is different

(defn just-a-view [dispatch-fn]
  [:div
   (dispatch-fn)
   [:a {:href (kf/path-for [:home])} "Back"]
   [:div {:style {:height "2000px"}}]
   [:p "Scroll up to go back"]])



;; >> The working view
;; This view works because an ajax request is made

(rf/reg-event-fx ::instant-load!
  (fn [_ _]
    {:http-xhrio
     {:method :get
      :uri (str "/does-not-matter")
      :response-format (ajax/json-response-format {:keywords? true})}}))

(defn working-view []
  [just-a-view #(rf/dispatch [::instant-load!])])



;; >> The broken view

(rf/reg-event-fx ::wait-load!
  (fn [_ _]
    {:dispatch-later [{:ms 50
                       :dispatch [:instant-load!]}]}))

(defn broken-view []
  [just-a-view #(rf/dispatch [::wait-load!])])



;; >> Setup the router view

(rf/reg-sub ::active-view
  (fn [db _]
    (-> db :kee-frame/route :data :name)))

(defn view []
  (let [active-view @(rf/subscribe [::active-view])]
    (case active-view
      :working [working-view]
      :broken [broken-view]
      [home-view])))



;; >> Init the UI

(defn init! []
  (rf/clear-subscription-cache!)
  (kf/start!
    {:routes [["/" :home]
              ["/broken" :broken]
              ["/working" :working]]
     :root-component [view]}))
