(ns client.events.ticket-events
  (:require
   [re-frame.core :as re-frame :refer [reg-event-fx]]
   [client.debounce]
   [client.events.http-events :as http]
   [client.validation.core :as validation]
   [re-frame-cljs-http.http-fx]
   [client.events.common-events :as ce]))

(re-frame/reg-event-db
 ::tickets-downloaded
 (fn [db [_ tickets]]
   (let [tickets (:body tickets)
         tickets (mapv (fn [ticket]
                         (if (:event ticket)
                           (assoc (dissoc ticket :event)
                                  :eventId (-> ticket :event :id))
                           ticket))
                       tickets)
         tickets-id-map (update-vals (group-by :id tickets) first)]
     (-> db
         (assoc :tickets tickets-id-map)))))

(reg-event-fx
 ::download-tickets
 (fn [{:keys [db]} [_]]
   (http/http-get (http/make-url-with-sort-and-filter db "/tickets")
                  [::tickets-downloaded]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::tickets-counted
 (fn [db [_ tickets]]
   (-> db
       (assoc :count-tickets (:body tickets)))))

(reg-event-fx
 ::count-tickets
 (fn [_ _]
   (http/http-get (http/full-url "/tickets/count")
                  [::tickets-counted]
                  [::ce/download-fail])))

(re-frame/reg-event-fx
 ::ticket-deleted
 (fn [{:keys [db]} [_ ticket-id]]
   {:db (update db :tickets dissoc ticket-id)
    :dispatch [::ce/toggle-delete-false]}))

(reg-event-fx
 ::delete-ticket-http
 (fn [_ [_ ticket-id]]
   (http/http-delete (http/full-url (str "/tickets/" ticket-id))
                     [::ticket-deleted ticket-id]
                     [::ce/download-fail])))

(reg-event-fx
 ::delete-ticket
 (fn [{:keys [db]} [_ ticket-id]]
   {:db (-> (update-in db [:tickets] dissoc ticket-id))
    :dispatch [::delete-ticket-http ticket-id]}))

(re-frame/reg-event-fx
 ::ticket-added
 (fn [{:keys [db]} [_ ticket-resp]]
   (let [ticket (assoc (dissoc (:body ticket-resp) :event)
                       :eventId (-> ticket-resp :body :event :id))]
     {:db (assoc-in db [:tickets (:id ticket)] ticket)
      :fx [[:dispatch [::count-tickets]]
           [:dispatch [::ce/toggle-new]]]})))

(reg-event-fx
 ::save-ticket-http
 (fn [_ [_ ticket]]
   (http/http-post (http/full-url "/tickets")
                   (dissoc (assoc ticket :event {:id (:eventId ticket)}) :eventId)
                   [::ticket-added]
                   [::ce/download-fail])))

(reg-event-fx
 ::save-ticket-from-form
 (fn [{:keys [db]} [_]]
   {:dispatch [::save-ticket-http (:form db)]}))

(re-frame/reg-event-db
 ::ticket-updated
 (fn [db [_ result]]
   (let [ticket (assoc (dissoc (:body result) :event)
                       :eventId (-> result :body :event :id))
         id (:id ticket)]
     (-> db
         (assoc-in [:tickets id] ticket)
         (assoc :toggle-change false)
         (assoc :form nil)
         (update-in [:ticket] dissoc :update-id)))))

(reg-event-fx
 ::update-ticket-http
 (fn [{:keys [db]} [_ ticket]]
   (http/http-put db
                  (http/full-url (str "/tickets/" (:id ticket)))
                  (dissoc (assoc ticket :event {:id (:eventId ticket)}) :eventId)
                  [::ticket-updated]
                  [::ce/download-fail])))

(reg-event-fx
 ::update-ticket-from-form
 (fn [{:keys [db]} [_]]
   {:db db
    :dispatch [::update-ticket-http (:form db)]}))

(re-frame/reg-event-db
 ::ticket-types-downloaded
 (fn [db [_ event-types]]
   (let [event-types (:body event-types)]
     (assoc db :ticket-types event-types))))

(reg-event-fx
 ::download-ticket-types
 (fn [_ _]
   (http/http-get (http/full-url "/tickets/types")
                  [::ticket-types-downloaded]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::ticket-types-count-downloaded
 (fn [db [_ type-count]]
   (let [event-types (:body type-count)]
     (assoc db :tickets-types-count event-types))))

(reg-event-fx
 ::download-ticket-types-count
 (fn [{:keys [db]} [_]]
   (http/http-get (http/full-url (str "/tickets/type/count?type=" (get db :ticket-type)))
                  [::ticket-types-count-downloaded]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::ticket-discount-count-downloaded
 (fn [db [_ type-count]]
   (let [event-types (:body type-count)]
     (assoc db :tickets-discount-count event-types
            :ticket-discount-count-opened true))))

(reg-event-fx
 ::download-ticket-discount-count
 (fn [_ _]
   (http/http-get (http/full-url "/tickets/discount/count")
                  [::ticket-discount-count-downloaded]
                  [::ce/download-fail])))

(re-frame/reg-event-db
 ::ticket-discount-sum-downloaded
 (fn [db [_ type-count]]
   (let [event-types (:body type-count)]
     (assoc db :tickets-discount-sum event-types))))

(reg-event-fx
 ::download-ticket-discount-sum
 (fn [_ _]
   (http/http-get (http/full-url "/tickets/discount/sum")
                  [::ticket-discount-sum-downloaded]
                  [::ce/download-fail])))


(reg-event-fx
 ::change-ticket-and-validate
 (fn [{:keys [db]} [_ prop-path value]]
   {:db db
    :fx [[:dispatch [::save-form prop-path value]]
         [:dispatch [::validate-form]]]}))

(reg-event-fx
 ::start-ticket-update
 (fn [{:keys [db]} [_ ticket-id]]
   {:db
    (-> db
        (assoc :toggle-change true)
        (assoc :form (get-in db [:tickets ticket-id]))
        (assoc-in [:ticket :update-id] ticket-id))}))

(reg-event-fx
 ::end-ticket-update
 (fn [{:keys [db]} [_]]
   {:db
    (-> db
        (assoc :toggle-change false)
        (assoc :form nil)
        (update-in [:ticket] dissoc :update-id))}))

(def parse-ticket
  {:id parse-long
   :coordinates {:x parse-long
                 :y parse-long}
   :type #(if (= "" %) nil %)
   :eventId parse-long
   :refundable #(if (boolean? %) % (parse-boolean %))
   :price parse-double
   :discount parse-double})

(reg-event-fx
 ::save-form
 (fn [{:keys [db]} [_ path value]]
   {:db (assoc-in db
                  (into [:form] path)
                  (cond-> value
                    (get-in parse-ticket path)
                    (try
                      ((get-in parse-ticket path) value)
                      (catch js/Error _e
                        value))))}))

(reg-event-fx
 ::validate-form
 (fn [{:keys [db]} [_ _]]
   (let [validate-res (validation/validate-ticket (get db :form))]
     {:db (assoc db :form-valid validate-res)})))

(reg-event-fx
 ::close-ticket-discount-count
 (fn [{:keys [db]} [_]]
   {:db (assoc db :ticket-discount-count-opened false)}))

(reg-event-fx
 ::change-ticket-type
 (fn [{:keys [db]} [_ ticket-type]]
   {:db (assoc db :ticket-type ticket-type)}))
