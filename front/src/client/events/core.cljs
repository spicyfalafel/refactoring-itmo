(ns client.events.core
  (:require
    [re-frame.core :as re-frame :refer [reg-event-fx]]
    [client.events.ticket-events :as tickets]
    [client.events.events-events :as events]
    [client.debounce]
    [re-frame-cljs-http.http-fx]
    [client.db :as db]))

(reg-event-fx
 ::loading
 (fn [{:keys [db]} _]
   {:db (assoc db :reloading true)}))

(reg-event-fx
 ::loading-done
 (fn [{:keys [db]} _]
   {:db (assoc db :reloading false)}))

(defn download-any [db]
 (if (= :tickets (:mode db)) [::tickets/download-tickets]  [::events/download-events]))

(reg-event-fx
 ::reload-db
 (fn [_ _]
   {:fx [[:dispatch  [::loading]]
         [:dispatch  [::events/download-event-types]]
         [:dispatch  [::tickets/download-ticket-types]]
         [:dispatch  [::tickets/count-tickets]]
         [:dispatch  [::events/count-events]]
         [:dispatch  [::events/download-events]]
         [:dispatch  [::tickets/download-tickets]]
         [:dispatch-debounce
          {:delay 500
           :event [::loading-done]}]]}))

(reg-event-fx
 ::initialize-db
 (fn [_ _]
   {:db db/default-db
    :fx [[:dispatch  [::events/download-event-types]]
         [:dispatch  [::tickets/download-ticket-types]]
         [:dispatch  [::tickets/count-tickets]]
         [:dispatch  [::events/count-events]]
         [:dispatch  [::events/download-events]]
         [:dispatch  [::tickets/download-tickets]]]}))

(reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(reg-event-fx
 ::set-mode
 (fn [{:keys [db]} [_ mode]]
   {:db (-> db
            (assoc :mode mode)
            (assoc :filters db/default-filters))}))


(reg-event-fx
 ::change-page
 (fn [{:keys [db]} [_ value]]
   {:db (assoc-in db [:paging :current-page] value)
    :dispatch
    (download-any db)}))

(reg-event-fx
 ::change-filter-1
 (fn [{:keys [db]} [_ mode idx1 idx2 value]]
   {:db (assoc-in db [:filters mode idx1 idx2] value)}))

(reg-event-fx
 ::hide-filter
 (fn [{:keys [db]} [_ prop]]
   {:db
    (update-in db [:filters (:mode db) prop :shown] not)}))

(reg-event-fx
 ::change-page-size
 (fn [{:keys [db]} [_ size]]
   (let [parsed (parse-long size)]
     {:db
      (if (and parsed (number? parsed) (<= 1 parsed 100))
        (assoc-in db [:paging :page-size] parsed)
        db)
      :dispatch (download-any db)})))

(reg-event-fx
 ::change-filter
 (fn [{:keys [db]} [_ idx1 idx2 value]]
   {:fx [[:dispatch [::change-filter-1
                     (:mode db)
                     idx1 idx2 value]]
         [:dispatch (download-any db)]]}))

(reg-event-fx
 ::change-sort-nested
 (fn [{:keys [db]} [_ sort-id sort-opt value]]
   (let [mode (:mode db)]
     {:db (assoc-in db [(if (= mode :tickets) :ticket :event) :sorting sort-id sort-opt] value)})))

(reg-event-fx
 ::change-sort
 (fn [{:keys [db]} [_ sort-id sort-opt value]]
   {:fx [[:dispatch [::change-sort-nested sort-id sort-opt value]]
         [:dispatch (download-any db)]]}))

(reg-event-fx
 ::add-sorting
 (fn [{:keys [db]} [_ mode]]
   (let [part (if (= mode :tickets) :ticket :event)
         new-sort-id (if (< 0 (count (get-in db [part :sorting])))
                       (inc (key (apply max-key key (get-in db [part :sorting]))))
                       0)]
     {:db
      (assoc-in db [part :sorting new-sort-id] {:field nil :sort-order nil})})))

(reg-event-fx
 ::remove-sorting
 (fn [{:keys [db]} [_ mode id]]
   {:db (update-in db [(if (= mode :tickets) :ticket :event) :sorting] dissoc id
                   false)}))
