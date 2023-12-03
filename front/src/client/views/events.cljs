(ns client.views.events
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.common-events :as ce]
   [client.debounce]
   [goog.string.format]
   [client.views.myclasses :as cls]
   [client.views.event.one-event :as one-event]
   [client.views.event.modal :as modal]
   [client.views.mycomponents :as components]
   [client.subs.core :as subs]
   [client.subs.events :as esubs])
  (:require-macros [stylo.core :refer [c]]))

(defn events-view []
  (let [events @(re-frame/subscribe [::esubs/events-1])
        modal-opened? @(subscribe [::subs/toggle-new])
        modal-delete-opened? @(subscribe [::subs/toggle-delete])

        modal-edit-opened? @(subscribe [::subs/toggle-change])]
    [:div {:class (c :w-full)}
     [components/paging]
     (when modal-opened?
       [modal/new])
     (when modal-edit-opened?
       [modal/edit])
     (when modal-delete-opened?
       [modal/delete])
     [:div {:class (c :grid [:grid-cols 3])}
      [:div {:class [cls/div-center]
             :on-click #(dispatch [::ce/toggle-new])}
       "НОВЫЙ"]
      (doall
       (for [event events]
         (one-event/one-event event)))]]))
