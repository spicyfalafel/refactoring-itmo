(ns client.views.tickets
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.common-events :as ce]
   [client.debounce]
   [client.views.myclasses :as cls]
   [goog.string.format]
   [client.views.ticket.one-ticket :as ticket]
   [client.views.ticket.modal :as modal]
   [client.views.mycomponents :as components]
   [client.subs.tickets :as tsubs]
   [client.subs.core :as subs])
  (:require-macros [stylo.core :refer [c]]))

(defn tickets-view []
  (let [tickets @(re-frame/subscribe [::tsubs/tickets-1])
        modal-opened? @(subscribe [::subs/toggle-new])
        modal-delete-opened? @(subscribe [::subs/toggle-delete])
        modal-edit-opened?   @(subscribe [::subs/toggle-change])]
    [:div {:class (c :w-full)}
     [components/paging]
     (when modal-opened?
       [modal/new])
     (when modal-edit-opened?
       [modal/edit])
     (when modal-delete-opened?
       [modal/delete])
     [:div {:class (c :grid [:grid-cols 2])}
      [:div {:class cls/div-center
             :on-click #(dispatch [::ce/toggle-new])}
       "НОВЫЙ"]
      (doall
       (for [ticket tickets]
         ^{:key (:id ticket)}
         [ticket/one-ticket (:id ticket)]))]]))
