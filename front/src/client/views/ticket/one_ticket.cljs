(ns client.views.ticket.one-ticket
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.ticket-events :as t]
   [client.events.common-events :as ce]
   [client.debounce]
   [client.views.myclasses :as cls]
   [goog.string.format]
   [client.views.mycomponents :as components]
   [client.subs.tickets :as tsubs]
   [client.subs.events :as esubs])
  (:require-macros [stylo.core :refer [c]]))

(defn type-view [type]
  (cond
    (= type "VIP")
    [:i.fa-regular.fa-star]

    (= type "USUAL")
    [:i.fa-solid.fa-check]

    (= type "BUDGETARY")
    [:i.fa-solid.fa-wheelchair-move]

    (= type "CHEAP")
    [:i.fa-regular.fa-star]))

(defn edit-ticket-icon []
  [:span [:i.fa-solid.fa-pen-to-square
          {:class (c [:px 3])}]])

(defn one-ticket [id]
  (let [ticket @(subscribe [::tsubs/ticket-by-id id])
        event  @(subscribe [::esubs/event-by-id (:eventId ticket)])]
    ^{:key id}
    [:div
     [:div {:class cls/card}
      [:div {:class (c [:w-max 100])}
       [:div
        [:div "ID: " id]
        [:span
         (:creationDate ticket) " " (type-view (:type ticket)) " " [:span (:type ticket)] " "]
        [:div {:class (c [:w-max 100])}
         [:span {:class (c :text-xl :text-bold)}
          [:span {:class (c [:h 9] [:h-max 9]) :title (:name event)}
           (if (:name event) (:name event) "Неизвестное мероприятие")]]]]
       [:span {:class (c :text-sm [:h 9] [:h-max 9])
               :title (:name ticket)}
        (str "Название билета: " (:name ticket))]
       [:div {:class (c :text-sm)} (if (:refundable ticket) "Возвратный" "Невозвратный")]]
      [:div  {:class (c [:w-max 100])}
       [:div
        "СКИДКА: " (:discount ticket) "%"]
       [:div "ЦЕНА: " (:price ticket)]
       [:div (str "(" (:x (:coordinates ticket)) ","  (:y (:coordinates ticket)) ")")]]
      [:div {:class (c :flex :flex-col :justify-center [:gap 5])}
       [:div
        {:class [cls/base-class (c :cursor-pointer)]
         :on-click #(dispatch [::t/start-ticket-update id])}
        (edit-ticket-icon)
        "Изменить"]
       [:div
        {:class [cls/base-class (c :cursor-pointer)]
         :on-click #(dispatch [::ce/toggle-delete id])}
        [components/delete-icon]
        "Удалить"]]]]))


