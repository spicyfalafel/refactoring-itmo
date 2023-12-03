(ns client.views.event.one-event
(:require
   [re-frame.core :as re-frame :refer [dispatch]]
   [client.events.events-events :as e]
   [client.events.common-events :as ce]
   [client.debounce]
   [goog.string.format]
   [client.views.myclasses :as cls]
   [client.views.mycomponents :as components])
  (:require-macros [stylo.core :refer [c]]))

(defn edit-event-icon [id]
  [:span [:i.fa-solid.fa-pen-to-square
          {:class (c [:px 3])
           :on-click #(dispatch [::e/start-event-update id])}]])

(defn event-type-icon [event-type]
  (cond
    (= event-type "BASKETBALL")
    [:i.fa-solid.fa-basketball]
    (= event-type "CONCERT")
    [:i.fa-solid.fa-microphone]

    (= event-type "THEATRE_PERFORMANCE")
    [:i.fa-solid.fa-masks-theater]

    (= event-type "BASEBALL")
    [:i.fa-solid.fa-baseball]))

(defn one-event [{:keys [id name date minAge eventType]}]
  ^{:key id}
  [:div
   [:div
    [:div {:class cls/card}
     [:div
      [:div
       [:div "ID: " id]
       [:span {:class (c :text-sm)} date  " " (event-type-icon eventType) " " [:span eventType] " "]
       [:div.truncate {:class (c [:w-max 90] [:h-max 9])}
        [:span
         {:class (c :text-xl :text-bold [:h 9] [:h-max 9])}
         [:span.truncate {:class (c [:h 9] [:h-max 9])}
          [:div {:title name} name]]]]]
      [:span {:class (c :text-sm)} "Минимальный возраст: " minAge]]
     [:div
      {:class (c :text-xl [:pt 3])}]
     [:div {:class (c :flex :flex-col :justify-center [:gap 5])}
      [:div
       {:class [cls/base-class (c :cursor-pointer)]
        :on-click #(dispatch [::e/start-event-update id])}
       (edit-event-icon id)
       "Изменить"]

      [:div
       {:class [cls/base-class (c :cursor-pointer)]
        :on-click #(dispatch [::ce/toggle-delete id])}
       (components/delete-icon)
       "Удалить"]]]]])
