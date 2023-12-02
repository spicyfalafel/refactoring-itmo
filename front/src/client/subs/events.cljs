(ns client.subs.events
  (:require [re-frame.core :as re-frame :refer [reg-sub]]))

(reg-sub
 ::events
 (fn [db _]
   (get-in db [:events])))

(reg-sub
 ::events-1
 (fn [db _]
   (vals (get-in db [:events]))))

(reg-sub
 ::event-by-id
 (fn [db [_ id]]
   (get-in db [:events id])))

(reg-sub
 ::event-form-prop
 (fn [db [_ prop-path]]
   (get-in db (into [:event-form] prop-path))))

(reg-sub
 ::events-form-valid?
 (fn [db [_]]
   (let [form (get db :event-form-valid)]
     (or (nil? form)
         (= :ok form)
         (and (or (vector? form) (seq? form)) (empty? form))))))

(reg-sub
 ::event-form-path-invalid-message
 (fn [db [_ prop-path]]
   (when (not= :ok (get db :event-form-valid))
     (first (vec (mapv :message
                       (filter
                        (fn [error-mp]
                          (= prop-path (into [] (:path error-mp))))
                        (get db :event-form-valid))))))))

(reg-sub
 ::event-to-delete-id
 (fn [db [_]]
   (get-in db [:event :to-delete])))

(reg-sub
 ::event-update-id
 (fn [db [_]]
   (get-in db [:event :update-id])))

(reg-sub
 ::count-events
 (fn [db _]
   (get db :count-events)))

(reg-sub
 ::event-types
 (fn [db _]
   (get db :event-types)))

