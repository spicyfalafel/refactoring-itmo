(ns client.events.common-events
  (:require
   [re-frame.core :as re-frame :refer [reg-event-fx]]
   [client.debounce]
   [re-frame-cljs-http.http-fx]))

(reg-event-fx
 ::toggle-delete-false
 (fn [{:keys [db]} [_]]
   {:db (assoc db :toggle-delete false)}))

(reg-event-fx
 ::toggle-new
 (fn [{:keys [db]} [_]]
   {:db (cond-> (update db :toggle-new not)
          (not (:toggle-new db))
          (dissoc :form :event-form))}))

(reg-event-fx
 ::toggle-delete
 (fn [{:keys [db]} [_ id]]
   {:db (assoc-in
         (update db :toggle-delete not)
         (if (= :tickets (:mode db))
           [:ticket :to-delete]
           [:event :to-delete]) id)}))

(re-frame/reg-event-db
 ::download-fail
 (fn [db [_ result]]
   (assoc db :http-result result)))
