(ns client.events.events-events
  (:require
   [re-frame.core :as re-frame :refer [reg-event-fx]]
   [client.debounce]
   [client.events.http-events :as http]
   [client.events.common-events :as ce]
   [client.validation.core :as validation]
   [re-frame-cljs-http.http-fx]))

(re-frame/reg-event-db
 ::events-downloaded
 (fn [db [_ events]]
   (let [events (:body events)
         events-id-map (update-vals (group-by :id events) first)]
     (-> db
         (assoc :events events-id-map)))))

(reg-event-fx
 ::download-events
 (fn [{:keys [db]} _]
   (http/http-get (http/make-url-with-sort-and-filter db "/events")
                  [::events-downloaded]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::events-counted
 (fn [db [_ events]]
   (-> db
       (assoc :count-events (:body events)))))

(reg-event-fx
 ::count-events
 (fn [_ _]
   (http/http-get (http/full-url "/events/count")
                  [::events-counted]
                  [::ce/download-fail])))

(re-frame/reg-event-fx
 ::event-deleted
 (fn [{:keys [db]} [_ event_id]]
   {:db (update db :events dissoc event_id)
    :dispatch [::ce/toggle-delete-false]}))

(reg-event-fx
 ::delete-event-http
 (fn [_ [_ event-id]]
   (http/http-delete (http/full-url (str "/events/" event-id))
                     [::event-deleted event-id]
                     [::ce/download-fail])))

(reg-event-fx
 ::delete-event
 (fn [{:keys [db]} [_ event-id]]
   {:db (-> (update-in db [:events] dissoc event-id))
    :dispatch [::delete-event-http event-id]}))

(re-frame/reg-event-fx
 ::event-added
 (fn [{:keys [db]} [_ event-resp]]
   (let [event (:body event-resp)]
     {:db (assoc-in db [:events (:id event)] event)
      :fx [[:dispatch [::count-events]]
           [:dispatch [::ce/toggle-new]]]})))

(reg-event-fx
 ::save-event-http
 (fn [_ [_ event]]
   (http/http-post (http/full-url "/events")
                   (update event :date (fn [date-str] (when date-str (str date-str ":00.000Z"))))
                   [::event-added]
                   [::ce/download-fail])))

(reg-event-fx
 ::save-event-from-form
 (fn [{:keys [db]} [_]]
   {:dispatch [::save-event-http (:event-form db)]}))

(re-frame/reg-event-db
 ::event-updated
 (fn [db [_ result]]
   (let [event (:body result)
         id (:id event)]
     (-> db
         (assoc-in [:events id] event)
         (assoc :toggle-change false)
         (assoc :form nil)
         (update-in [:event] dissoc :update-id)))))

(reg-event-fx
 ::update-event-http
 (fn [{:keys [db]} [_ event]]
   (http/http-put db
                  (http/full-url (str "/events/" (:id event)))
                  (dissoc event :event :id)
                  [::event-updated]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::event-types-downloaded
 (fn [db [_ event-types]]
   (let [event-types (:body event-types)]
     (assoc db :event-types event-types))))

(reg-event-fx
 ::download-event-types
 (fn [_ _]
   (http/http-get (http/full-url "/events/types")
                  [::event-types-downloaded]
                  [::ce/download-fail])))

(reg-event-fx
 ::change-event-and-validate
 (fn [{:keys [db]} [_ prop-path value]]
   {:db db
    :fx [[:dispatch [::save-form-event prop-path value]]
         [:dispatch [::validate-event-form]]]}))

(def parse-event
  {:id parse-long
   :eventType #(if (= "" %) nil %)
   :date #(if (= "" %) nil %)
   :minAge parse-long})

(reg-event-fx
 ::save-form-event
 (fn [{:keys [db]} [_ path value]]
   {:db (assoc-in db (into [:event-form] path)
                  (cond->
                   value
                    (get-in parse-event path)
                    (try
                      ((get-in parse-event path) value)
                      (catch js/Error _e
                        value))))}))

(reg-event-fx
 ::validate-event-form
 (fn [{:keys [db]} [_ _]]
   (let [validate-res (validation/validate-event (get db :event-form))]
     {:db (assoc db :event-form-valid validate-res)})))

(reg-event-fx
 ::update-event-from-form
 (fn [{:keys [db]} [_]]
   {:db db
    :dispatch [::update-event-http (:event-form db)]}))

(reg-event-fx
 ::start-event-update
 (fn [{:keys [db]} [_ event-id]]
   {:db
    (-> db
        (assoc :toggle-change true)
        (assoc :event-form (get-in db [:events event-id]))
        (assoc-in [:event :update-id] event-id))}))

(reg-event-fx
 ::end-event-update
 (fn [{:keys [db]} [_]]
   {:db
    (-> db
        (assoc :toggle-change false)
        (assoc :event-form nil)
        (update-in [:event] dissoc :update-id))}))
